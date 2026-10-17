# Vortex — Real-Time Voice Changer

**Java 17 · JavaFX · Digital Signal Processing · Desktop App**

Academic **team project** for BUBT's Advanced Programming Lab. The application uses the Java Sound API to process a local microphone signal and apply ten audio effects. It is **not AI-based voice cloning**.

> Forked from [Sakib-183/Vortex---Real-Time-Voice-Changer](https://github.com/Sakib-183/Vortex---Real-Time-Voice-Changer). Team credit and documented contributions are retained.

## What's included

- [Application documentation and screenshots guidance](Vortex/README.md)
- [Team contributions](Vortex/docs/TEAM_TASKS.md)
- [Architecture](Vortex/docs/ARCHITECTURE.md)
- [Validation notes](Vortex/docs/VALIDATION.md)
- [Portfolio and attribution notes](Vortex/docs/PORTFOLIO_NOTES.md)

## Run on Windows

Install JDK 17+ and Maven 3.8+, connect microphone and headphones, then:

```bash
cd Vortex
mvn clean javafx:run
```

The original project also includes `Vortex/run-windows.bat` and packaging scripts.

## DSP smoke testing

In a Java 17+ Unix-like shell:

```bash
cd Vortex
bash tests/run-smoke-test.sh
```

This test checks DSP output and PCM conversion without hardware. **The UI and live audio routing need Windows/device-specific testing.**

## Team contributions

Per the original team documentation: Habib — integration; Sadman — audio processing; Pranto — UI/UX; Fahim — testing and documentation. No claim of sole authorship is made.


## Application Screenshots

### Voice Presets & Audio Devices
![Vortex Interface](screenshots/vortex-interface.png)

### Real-Time Voice Processing
![Vortex Processing](screenshots/vortex-processing.png)