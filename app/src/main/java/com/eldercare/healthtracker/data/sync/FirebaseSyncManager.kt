package com.eldercare.healthtracker.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.eldercare.healthtracker.BuildConfig
import com.eldercare.healthtracker.data.db.AppDatabase
import com.eldercare.healthtracker.data.db.entities.BloodPressureEntry
import com.eldercare.healthtracker.data.db.entities.GlucoseEntry
import com.eldercare.healthtracker.data.db.entities.MedicineEntry
import com.eldercare.healthtracker.data.db.entities.MedicineLogEntry
import com.eldercare.healthtracker.data.db.entities.SymptomEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Offline-first sync with Firebase. Behaviour:
 *   • All writes go to Room first (see repository). This class *later* pushes
 *     unsynced rows to Firestore and pulls remote rows into Room.
 *   • If `BuildConfig.FIREBASE_ENABLED == false` (no google-services.json),
 *     every method here is a safe no-op. This lets the APK work standalone.
 *   • Requires an authenticated user. Callers should prompt sign-in before
 *     enabling sync.
 */
class FirebaseSyncManager(
    private val context: Context,
    private val db: AppDatabase
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val isFirebaseAvailable: Boolean get() = BuildConfig.FIREBASE_ENABLED

    val isSignedIn: Boolean
        get() = isFirebaseAvailable && FirebaseAuth.getInstance().currentUser != null

    val currentUserEmail: String?
        get() = if (isFirebaseAvailable) FirebaseAuth.getInstance().currentUser?.email else null

    fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /** Fire-and-forget sync (no UI feedback). */
    fun trySyncInBackground() {
        if (!isFirebaseAvailable) return
        scope.launch { runCatching { sync() }.onFailure { Log.w(TAG, "sync failed", it) } }
    }

    /** Performs a full bi-directional sync. Throws on any Firebase failure. */
    suspend fun sync(): SyncResult {
        if (!isFirebaseAvailable) return SyncResult(skipped = true)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return SyncResult(skipped = true, reason = "not_signed_in")
        if (!isOnline()) return SyncResult(skipped = true, reason = "offline")

        val firestore = FirebaseFirestore.getInstance()
        val userDoc = firestore.collection("users").document(uid)
        val now = System.currentTimeMillis()

        // ---- Push unsynced local rows ----
        val glucose = db.glucoseDao().getUnsynced()
        glucose.forEach { g ->
            userDoc.collection("glucose").document(g.id).set(g.toMap()).await()
        }
        if (glucose.isNotEmpty()) db.glucoseDao().markSynced(glucose.map { it.id }, now)

        val bp = db.bpDao().getUnsynced()
        bp.forEach { b ->
            userDoc.collection("bp").document(b.id).set(b.toMap()).await()
        }
        if (bp.isNotEmpty()) db.bpDao().markSynced(bp.map { it.id }, now)

        val meds = db.medicineDao().getUnsynced()
        meds.forEach { m ->
            userDoc.collection("medicines").document(m.id).set(m.toMap()).await()
        }
        if (meds.isNotEmpty()) db.medicineDao().markSynced(meds.map { it.id }, now)

        val medLogs = db.medicineDao().getUnsyncedLogs()
        medLogs.forEach { l ->
            userDoc.collection("medicine_logs").document(l.id).set(l.toMap()).await()
        }
        if (medLogs.isNotEmpty()) db.medicineDao().markLogsSynced(medLogs.map { it.id }, now)

        val symptoms = db.symptomDao().getUnsynced()
        symptoms.forEach { s ->
            userDoc.collection("symptoms").document(s.id).set(s.toMap()).await()
        }
        if (symptoms.isNotEmpty()) db.symptomDao().markSynced(symptoms.map { it.id }, now)

        // ---- Pull remote rows (everything; Firestore is the source of truth
        // for cloud, Room is source of truth locally). In a production app
        // you'd incremental-sync on updatedAt; for this app's scale this is
        // fine and simple. ----
        pullGlucose(userDoc)
        pullBp(userDoc)
        pullMedicines(userDoc)
        pullMedicineLogs(userDoc)

        return SyncResult(
            pushed = glucose.size + bp.size + meds.size + medLogs.size + symptoms.size,
            pulledAt = now
        )
    }

    private suspend fun pullGlucose(userDoc: com.google.firebase.firestore.DocumentReference) {
        val snap = userDoc.collection("glucose").get().await()
        val remote = snap.documents.mapNotNull { d ->
            GlucoseEntry(
                id = d.id,
                mgPerDl = (d.getLong("mgPerDl") ?: return@mapNotNull null).toInt(),
                contextTag = d.getString("contextTag") ?: "FASTING",
                takenAt = d.getLong("takenAt") ?: return@mapNotNull null,
                note = d.getString("note"),
                syncedAt = System.currentTimeMillis()
            )
        }
        if (remote.isNotEmpty()) db.glucoseDao().upsertAll(remote)
    }

    private suspend fun pullBp(userDoc: com.google.firebase.firestore.DocumentReference) {
        val snap = userDoc.collection("bp").get().await()
        val remote = snap.documents.mapNotNull { d ->
            BloodPressureEntry(
                id = d.id,
                systolic = (d.getLong("systolic") ?: return@mapNotNull null).toInt(),
                diastolic = (d.getLong("diastolic") ?: return@mapNotNull null).toInt(),
                pulse = d.getLong("pulse")?.toInt(),
                takenAt = d.getLong("takenAt") ?: return@mapNotNull null,
                note = d.getString("note"),
                syncedAt = System.currentTimeMillis()
            )
        }
        if (remote.isNotEmpty()) db.bpDao().upsertAll(remote)
    }

    private suspend fun pullMedicines(userDoc: com.google.firebase.firestore.DocumentReference) {
        val snap = userDoc.collection("medicines").get().await()
        snap.documents.forEach { d ->
            val m = MedicineEntry(
                id = d.id,
                name = d.getString("name") ?: return@forEach,
                dosage = d.getString("dosage") ?: "",
                timesOfDay = d.getString("timesOfDay") ?: "",
                foodTiming = d.getString("foodTiming") ?: "ANYTIME",
                active = d.getBoolean("active") ?: true,
                createdAt = d.getLong("createdAt") ?: System.currentTimeMillis(),
                syncedAt = System.currentTimeMillis()
            )
            db.medicineDao().upsert(m)
        }
    }

    private suspend fun pullMedicineLogs(userDoc: com.google.firebase.firestore.DocumentReference) {
        val snap = userDoc.collection("medicine_logs").get().await()
        snap.documents.forEach { d ->
            val l = MedicineLogEntry(
                id = d.id,
                medicineId = d.getString("medicineId") ?: return@forEach,
                medicineName = d.getString("medicineName") ?: "",
                takenAt = d.getLong("takenAt") ?: return@forEach,
                timeOfDay = d.getString("timeOfDay") ?: "",
                syncedAt = System.currentTimeMillis()
            )
            db.medicineDao().upsertLog(l)
        }
    }

    data class SyncResult(
        val skipped: Boolean = false,
        val reason: String? = null,
        val pushed: Int = 0,
        val pulledAt: Long? = null
    )

    companion object { private const val TAG = "FirebaseSyncManager" }
}

// ---- Mapping helpers kept here so sync code stays self-contained. ----

private fun GlucoseEntry.toMap() = mapOf(
    "mgPerDl" to mgPerDl,
    "contextTag" to contextTag,
    "takenAt" to takenAt,
    "note" to note
)

private fun BloodPressureEntry.toMap() = mapOf(
    "systolic" to systolic,
    "diastolic" to diastolic,
    "pulse" to pulse,
    "takenAt" to takenAt,
    "note" to note
)

private fun MedicineEntry.toMap() = mapOf(
    "name" to name,
    "dosage" to dosage,
    "timesOfDay" to timesOfDay,
    "foodTiming" to foodTiming,
    "active" to active,
    "createdAt" to createdAt
)

private fun MedicineLogEntry.toMap() = mapOf(
    "medicineId" to medicineId,
    "medicineName" to medicineName,
    "takenAt" to takenAt,
    "timeOfDay" to timeOfDay
)

private fun SymptomEntry.toMap() = mapOf(
    "takenAt" to takenAt,
    "dizziness" to dizziness,
    "headache" to headache,
    "sweating" to sweating,
    "fatigue" to fatigue,
    "note" to note
)
