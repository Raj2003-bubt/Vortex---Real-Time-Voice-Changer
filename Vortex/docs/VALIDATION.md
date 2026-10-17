# Validation Performed


The core audio and DSP source files were compiled with OpenJDK 21 using Java 17 compatibility (`--release 17`).


A synthetic 220 Hz input signal was processed repeatedly through every preset. The test verified that each preset completed without exceptions and produced finite, clipped output samples within the valid PCM range.


Validated presets:


- Deep Voice
- Bright / Female-Style Voice
- Child Voice
- Robot Voice
- Monster Voice
- Alien Voice
- Radio Voice
- Echo Voice
- Cave Voice
- Demon Voice


The full JavaFX interface requires Maven to download the JavaFX modules during the first build. Device routing and real microphone behavior must be tested on the target Windows computer because available mixers and virtual-audio devices are system-specific.



## Independent portfolio audit — 17 October 2026

Fresh checks with OpenJDK 17.0.20 on Linux:

- Maven 3.9.11: `mvn -B --no-transfer-progress package` — **BUILD SUCCESS**; all 11 source files compiled and `target/vortex.jar` built. No JUnit tests are configured; the separate smoke script is the DSP verification.
- `bash tests/run-smoke-test.sh` — **PASS: PCM, all 10 presets, finite output and mute**.
- Both existing PNGs and the corrected README paths were checked; no duplicate images were added.

Windows launch and real microphone use were reported by the project owner. Physical-device routing, GUI behavior, Windows packaging, audio quality, and end-to-end latency were not independently repeated in this audit.
