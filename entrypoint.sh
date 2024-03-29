#!/bin/bash
mkdir -p $CHECKPOINT_RESTORE_FILES_DIR
export SERVICE_BINDING_ROOT=${SERVICE_BINDING_ROOT:-/bindings}
export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS} -Dorg.springframework.cloud.bindings.boot.enable=true -XX:+ExitOnOutOfMemoryError"

if [ ! -f "$CHECKPOINT_RESTORE_FILES_DIR/files.img" ]; then
  echo "Save checkpoint to $CHECKPOINT_RESTORE_FILES_DIR" 1>&2
  java -XX:CRaCCheckpointTo=$CHECKPOINT_RESTORE_FILES_DIR org.springframework.boot.loader.launch.JarLauncher &
  sleep ${SLEEP_BEFORE_CHECKPOINT:-10}
  jcmd org.springframework.boot.loader.launch.JarLauncher JDK.checkpoint
  sleep ${SLEEP_AFTER_CHECKPOINT:-3}
else
  echo "Restore checkpoint from $CHECKPOINT_RESTORE_FILES_DIR" 1>&2
fi

(echo 128 > /proc/sys/kernel/ns_last_pid) 2>/dev/null || while [ $(cat /proc/sys/kernel/ns_last_pid) -lt 128 ]; do :; done
java -XX:CRaCRestoreFrom=$CHECKPOINT_RESTORE_FILES_DIR &
JAVA_PID=$!

stop_java_app() {
    kill -SIGTERM $JAVA_PID
}

trap stop_java_app SIGINT
wait $JAVA_PID