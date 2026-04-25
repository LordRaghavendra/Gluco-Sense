package com.eldercare.healthtracker.data.repo

import com.eldercare.healthtracker.data.db.AppDatabase
import com.eldercare.healthtracker.data.db.entities.BloodPressureEntry
import com.eldercare.healthtracker.data.db.entities.EmergencyInfo
import com.eldercare.healthtracker.data.db.entities.GlucoseEntry
import com.eldercare.healthtracker.data.db.entities.MedicineEntry
import com.eldercare.healthtracker.data.db.entities.MedicineLogEntry
import com.eldercare.healthtracker.data.db.entities.SymptomEntry

/**
 * Facade over the Room DAOs. ViewModels talk to the repository so data
 * source swaps (e.g. a fake in tests) stay invisible to the UI layer.
 */
class HealthRepository(db: AppDatabase) {
    private val glucoseDao = db.glucoseDao()
    private val bpDao = db.bpDao()
    private val medicineDao = db.medicineDao()
    private val symptomDao = db.symptomDao()
    private val emergencyDao = db.emergencyDao()

    // Glucose
    fun observeGlucose() = glucoseDao.observeAll()
    suspend fun addGlucose(entry: GlucoseEntry) = glucoseDao.upsert(entry)
    suspend fun getGlucoseBetween(start: Long, end: Long) = glucoseDao.getBetween(start, end)
    suspend fun allGlucose() = glucoseDao.getAll()
    suspend fun deleteGlucose(id: String) = glucoseDao.delete(id)

    // BP
    fun observeBp() = bpDao.observeAll()
    suspend fun addBp(entry: BloodPressureEntry) = bpDao.upsert(entry)
    suspend fun getBpBetween(start: Long, end: Long) = bpDao.getBetween(start, end)
    suspend fun allBp() = bpDao.getAll()
    suspend fun deleteBp(id: String) = bpDao.delete(id)

    // Medicines
    fun observeMedicines() = medicineDao.observeActive()
    suspend fun addMedicine(entry: MedicineEntry) = medicineDao.upsert(entry)
    suspend fun allMedicines() = medicineDao.getAll()
    suspend fun activeMedicines() = medicineDao.getActive()
    suspend fun deactivateMedicine(id: String) = medicineDao.deactivate(id)

    suspend fun logMedicineTaken(log: MedicineLogEntry) = medicineDao.upsertLog(log)
    fun observeMedicineLogs() = medicineDao.observeLogs()
    suspend fun getMedicineLogsBetween(start: Long, end: Long) =
        medicineDao.getLogsBetween(start, end)

    // Symptoms
    suspend fun addSymptoms(entry: SymptomEntry) = symptomDao.upsert(entry)
    suspend fun getSymptomsBetween(start: Long, end: Long) =
        symptomDao.getBetween(start, end)

    // Emergency
    fun observeEmergency() = emergencyDao.observe()
    suspend fun saveEmergency(info: EmergencyInfo) = emergencyDao.upsert(info)
}
