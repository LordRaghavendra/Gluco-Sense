#!/bin/sh
# Minimal Gradle wrapper launcher (POSIX)
set -e
DIR="$(cd "$(dirname "$0")" && pwd)"
JAR="$DIR/gradle/wrapper/gradle-wrapper.jar"
if [ -z "$JAVA_HOME" ]; then
    JAVA_CMD=java
else
    JAVA_CMD="$JAVA_HOME/bin/java"
fi
exec "$JAVA_CMD" -Xmx2g -Dorg.gradle.appname=gradlew -classpath "$JAR" org.gradle.wrapper.GradleWrapperMain "$@"
