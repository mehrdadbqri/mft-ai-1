#!/usr/bin/env bash
set -euo pipefail

apk="tested-apk/app-debug.apk"
adb install -r "$apk"
adb logcat -c || true
adb shell am force-stop com.hojrechi.app || true

# Collect evidence even when launch fails.
capture() {
  adb logcat -d -b crash > android-crash.log || true
  adb logcat -d -v threadtime > android-system.log || true
  adb shell screencap -p /sdcard/hojrechi-test.png || true
  adb pull /sdcard/hojrechi-test.png hojrechi-test.png || true
}
trap capture EXIT

adb shell am start -W -n com.hojrechi.app/.MainActivity
sleep 14
echo "Checking live app process..."
pid="$(adb shell pidof com.hojrechi.app | tr -d '\r' || true)"
if [[ -z "$pid" ]]; then
  echo "ERROR: App process exited after launch."
  exit 1
fi
echo "Live app PID: $pid"
adb logcat -d -b crash > android-crash.log || true
if grep -q -E 'FATAL EXCEPTION|Process: com.hojrechi.app' android-crash.log; then
  echo "ERROR: Android crash detected"
  cat android-crash.log
  exit 1
fi
echo "SUCCESS: APK installed, started, and stayed running without a fatal Android exception."
