#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
tmpdir="$(mktemp -d)"
trap 'rm -rf "$tmpdir"' EXIT
javac --release 17 -d "$tmpdir" \
  src/main/java/com/bubt/voicechanger/model/VoicePreset.java \
  src/main/java/com/bubt/voicechanger/dsp/EffectSettings.java \
  src/main/java/com/bubt/voicechanger/dsp/AudioEffectProcessor.java \
  src/main/java/com/bubt/voicechanger/audio/PcmCodec.java \
  tests/DspSmokeTest.java
java -cp "$tmpdir" DspSmokeTest
