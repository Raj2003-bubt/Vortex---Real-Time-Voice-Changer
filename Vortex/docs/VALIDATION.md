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
