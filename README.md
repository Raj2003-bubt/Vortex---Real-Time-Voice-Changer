# Vortex — Real-Time Voice Changer

**Explore live audio processing through an interactive Java desktop application.**

[![Java build and DSP smoke test](https://github.com/Raj2003-bubt/Vortex---Real-Time-Voice-Changer/actions/workflows/java-build.yml/badge.svg)](https://github.com/Raj2003-bubt/Vortex---Real-Time-Voice-Changer/actions/workflows/java-build.yml)

Vortex is an academic programming project at Bangladesh University of Business and Technology (BUBT). It captures microphone audio, applies lightweight digital signal processing (DSP), and plays the transformed signal through a selected output device. JavaFX controls and a live waveform make the signal path visible while the application runs.

## Purpose

Real-time audio brings together device discovery, continuous streams, numerical processing, concurrency, and responsive interfaces. Vortex explores these engineering problems through an application where effect changes produce audible and visible results.

## Features

- Local microphone input and output selection through the Java Sound API.
- Ten presets with pitch, strength, echo, reverb, and volume controls.
- Bypass, mute, input/output level meters, and waveform monitoring.
- 48 kHz, 16-bit, signed little-endian mono PCM; 512-sample processing blocks.
- A dedicated audio thread with atomically exchanged immutable effect settings.
- Output to headphones, speakers, or a separately installed virtual audio cable.
- Local processing: microphone audio is not uploaded.

## Screenshots

### Presets and audio devices

![Vortex Interface](screen%20shots/vortex-interface.png)

### Live microphone processing

![Vortex Processing](screen%20shots/vortex-processing.png)

Actual application captures supplied with the project. They demonstrate the Windows interface, without establishing latency or cross-platform compatibility.

## Technology and architecture

| Layer | Implementation |
|---|---|
| Interface | JavaFX 21.0.2, CSS, `VoiceChangerApp`, `WaveformView` |
| Capture/playback | Java Sound `TargetDataLine` and `SourceDataLine` |
| Processing | `AudioEffectProcessor`, `EffectSettings`, `PcmCodec` |
| Device discovery | `AudioDeviceService`, Java Sound mixers |
| Build | Java 17 target, Maven, JavaFX Maven plugin |

Microphone PCM bytes are decoded to floating-point samples, transformed by the effect processor, and encoded back to PCM for playback. The interface reads level and waveform snapshots while the audio thread reads the latest settings. See [architecture](Vortex/docs/ARCHITECTURE.md).

## DSP and voice effects

The engine combines interpolated delay-line pitch shifting, low/high-pass filters, ring modulation, tremolo, soft clipping, feedback delay, and multi-tap reverb.

| Presets | Main treatment |
|---|---|
| Deep, Bright / Female-Style, Child | Pitch and tonal changes; Child adds modulation |
| Robot | Ring modulation and saturation |
| Monster, Demon | Lower pitch, distortion, delay, and reverb |
| Alien | Pitch movement, tremolo, ring modulation, and short delay |
| Radio | Band limiting, compression-style saturation, and deterministic noise |
| Echo, Cave | Repeated reflections or spacious reverb |

Names describe effects, not learned voice cloning or verified identity/gender conversion. A processing block spans about 10.7 ms at 48 kHz; end-to-end latency also includes drivers, device buffers, and processing and has not been benchmarked here.

## Run on Windows

Install **JDK 17 or 21**, **Maven 3.8+**, and connect a microphone and headphones. Maven downloads JavaFX on the first build.

```bash
git clone https://github.com/Raj2003-bubt/Vortex---Real-Time-Voice-Changer.git
cd Vortex---Real-Time-Voice-Changer/Vortex
mvn clean javafx:run
```

Alternatively, double-click `Vortex/run-windows.bat`. Choose devices and a preset, then press **Start Processing**. Headphones prevent acoustic feedback.

For virtual microphone routing, install a compatible virtual cable separately. Select **CABLE Input** as Vortex output and **CABLE Output** in the receiving application. Vortex does not install the driver.

## Build and verification

From the inner `Vortex` directory:

```bash
mvn -B --no-transfer-progress package
bash tests/run-smoke-test.sh
```

The Bash smoke script checks PCM sizes, all ten presets, finite bounded output, and mute using synthetic audio without hardware. GitHub Actions also builds the JavaFX application. See [validation notes](Vortex/docs/VALIDATION.md).

Windows scripts: `build-windows.bat` builds the project; `package-windows.bat` uses `javafx:jlink` to create `target\vortex\bin\vortex.bat`. Runtime packaging must run on the target platform.

## Engineering outcomes and limitations

The project demonstrates separation of UI/audio work, PCM conversion, stateful DSP, device management, and hardware-independent verification. Mixers may reject the requested format, occupied devices may fail to open, and live monitoring can introduce feedback.

Windows launch and microphone use were reported as manually tested by the project owner. Synthetic tests and automated builds are separate evidence. Virtual routing, audio quality, long-session behavior, and other operating systems require device-specific checks. No studio-quality or measured low-latency guarantee is made.

Potential extensions: latency measurements, stronger audio lifecycle tests, configurable sample formats, and platform-specific packaging.

## Documentation and provenance

[User guide](Vortex/docs/USER_GUIDE.md) · [Architecture](Vortex/docs/ARCHITECTURE.md) · [Test plan](Vortex/docs/TEST_PLAN.md) · [Historical contribution records](Vortex/docs/TEAM_TASKS.md)

This repository retains its fork relationship and original history from [Sakib-183/Vortex---Real-Time-Voice-Changer](https://github.com/Sakib-183/Vortex---Real-Time-Voice-Changer). Attribution remains intact; no sole-authorship claim is made. No new license is asserted. Check applicable rights and dependency licenses before redistribution.
