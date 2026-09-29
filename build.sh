#!/usr/bin/env bash
# Builds build/PipRoadBuddy.apk from docs/ (the app UI) and android/ (the native shell).
# Uses the Android SDK from $ANDROID_HOME, or tools given as AAPT2 / D8 / APKSIGNER / ANDROID_JAR.
set -euo pipefail
cd "$(dirname "$0")"

SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [ -z "${AAPT2:-}" ]; then
  BT=$(ls -d "$SDK"/build-tools/* | sort -V | tail -1)
  AAPT2="$BT/aapt2"; D8="$BT/d8"; APKSIGNER="$BT/apksigner"
fi
ANDROID_JAR="${ANDROID_JAR:-$(ls -d "$SDK"/platforms/android-* | sort -V | tail -1)/android.jar}"
KEYSTORE="${KEYSTORE:-pip.keystore}"; KS_PASS="${KS_PASS:-pipbuddy}"; KS_ALIAS="${KS_ALIAS:-pip}"

OUT=build
rm -rf "$OUT"; mkdir -p "$OUT/assets" "$OUT/classes" "$OUT/dex"
cp -r docs/. "$OUT/assets/"

"$AAPT2" compile --dir android/res -o "$OUT/res.zip"
"$AAPT2" link -o "$OUT/base.apk" -I "$ANDROID_JAR" --manifest android/AndroidManifest.xml \
  -A "$OUT/assets" "$OUT/res.zip" --min-sdk-version 24 --target-sdk-version 34
javac -nowarn -g -source 8 -target 8 -classpath "$ANDROID_JAR" -d "$OUT/classes" $(find android/src -name '*.java')
$D8 --release --lib "$ANDROID_JAR" --min-api 24 --output "$OUT/dex" $(find "$OUT/classes" -name '*.class')

cp "$OUT/base.apk" "$OUT/unaligned.apk"
(cd "$OUT/dex" && zip -q ../unaligned.apk classes.dex)
python3 tools/align.py "$OUT/unaligned.apk" "$OUT/aligned.apk"

if [ ! -f "$KEYSTORE" ]; then
  echo "No signing key found. Creating $KEYSTORE. Keep it safe: every update must be signed with it."
  keytool -genkeypair -keystore "$KEYSTORE" -storepass "$KS_PASS" -keypass "$KS_PASS" -alias "$KS_ALIAS" \
    -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Pip Road Buddy, C=PH"
fi
$APKSIGNER sign --ks "$KEYSTORE" --ks-pass "pass:$KS_PASS" --ks-key-alias "$KS_ALIAS" \
  --out "$OUT/PipRoadBuddy.apk" "$OUT/aligned.apk"
$APKSIGNER verify "$OUT/PipRoadBuddy.apk"
echo "Built $OUT/PipRoadBuddy.apk"
