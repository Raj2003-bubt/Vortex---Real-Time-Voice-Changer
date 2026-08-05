package com.bubt.voicechanger.ui;

import com.bubt.voicechanger.audio.AudioDevice;
import com.bubt.voicechanger.audio.AudioDeviceService;
import com.bubt.voicechanger.audio.AudioEngine;
import com.bubt.voicechanger.dsp.EffectSettings;
import com.bubt.voicechanger.model.VoicePreset;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.sound.sampled.LineUnavailableException;
import java.util.EnumMap;
import java.util.Map;

public final class VoiceChangerApp extends Application {
    private final AudioEngine audioEngine = new AudioEngine();
    private final Map<VoicePreset, ToggleButton> presetButtons = new EnumMap<>(VoicePreset.class);

    private ComboBox<AudioDevice> inputDeviceBox;
    private ComboBox<AudioDevice> outputDeviceBox;
    private Slider pitchSlider;
    private Slider strengthSlider;
    private Slider echoSlider;
    private Slider reverbSlider;
    private Slider volumeSlider;
    private CheckBox bypassCheck;
    private CheckBox muteCheck;
    private ProgressBar inputMeter;
    private ProgressBar outputMeter;
    private WaveformView waveformView;
    private Label statusLabel;
    private Label presetDescription;
    private Button startStopButton;
    private VoicePreset selectedPreset = VoicePreset.DEEP;
    private Timeline uiTimeline;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setTop(createHeader());
        root.setCenter(createContent());
        root.setBottom(createFooter());

        Scene scene = new Scene(root, 1120, 760);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        stage.setTitle("Vortex \u2014 Real-Time Voice Changer");
        stage.setMinWidth(960);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> shutdown());
        stage.show();

        audioEngine.setStatusListener(message -> Platform.runLater(() -> setStatus(message)));
        refreshAudioDevices();
        selectPreset(VoicePreset.DEEP);
        pushSettings();
        startUiTimeline();
    }

    private HBox createHeader() {
        Label title = new Label("VORTEX");
        title.getStyleClass().add("app-title");
        Label subtitle = new Label("Real-Time Voice Changer");
        subtitle.getStyleClass().add("app-subtitle");
        Label tagline = new Label("Java • Real-Time DSP • 10 Voice Presets");
        tagline.getStyleClass().add("app-tagline");
        VBox titles = new VBox(4, title, subtitle, tagline);

        Button refreshButton = new Button("Refresh Devices");
        refreshButton.getStyleClass().add("secondary-button");
        refreshButton.setOnAction(event -> refreshAudioDevices());

        Button helpButton = new Button("Routing Help");
        helpButton.getStyleClass().add("secondary-button");
        helpButton.setOnAction(event -> showHelp());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(12, titles, spacer, refreshButton, helpButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(22, 28, 18, 28));
        header.getStyleClass().add("header");
        return header;
    }

    private ScrollPane createContent() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(22, 28, 22, 28));

        HBox topRow = new HBox(18, createDeviceCard(), createPresetCard());
        HBox.setHgrow(topRow.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(topRow.getChildren().get(1), Priority.ALWAYS);

        HBox middleRow = new HBox(18, createControlsCard(), createMonitorCard());
        HBox.setHgrow(middleRow.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(middleRow.getChildren().get(1), Priority.ALWAYS);

        content.getChildren().addAll(topRow, middleRow);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("content-scroll");
        return scrollPane;
    }

    private VBox createDeviceCard() {
        Label heading = cardHeading("AUDIO DEVICES");
        inputDeviceBox = new ComboBox<>();
        inputDeviceBox.setMaxWidth(Double.MAX_VALUE);
        outputDeviceBox = new ComboBox<>();
        outputDeviceBox.setMaxWidth(Double.MAX_VALUE);

        VBox card = new VBox(11,
                heading,
                fieldLabel("Input microphone"), inputDeviceBox,
                fieldLabel("Processed output / virtual cable"), outputDeviceBox,
                infoLabel("For VB-CABLE, choose “CABLE Input” here. In Discord, Meet or Zoom, choose “CABLE Output” as the microphone."));
        card.getStyleClass().add("card");
        card.setPrefWidth(430);
        return card;
    }

    private VBox createPresetCard() {
        Label heading = cardHeading("VOICE PRESETS");
        ToggleGroup group = new ToggleGroup();
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        ColumnConstraints first = new ColumnConstraints();
        ColumnConstraints second = new ColumnConstraints();
        first.setPercentWidth(50);
        second.setPercentWidth(50);
        grid.getColumnConstraints().addAll(first, second);

        int index = 0;
        for (VoicePreset preset : VoicePreset.values()) {
            ToggleButton button = new ToggleButton(preset.displayName());
            button.setToggleGroup(group);
            button.setMaxWidth(Double.MAX_VALUE);
            button.getStyleClass().add("preset-button");
            button.setTooltip(new Tooltip(preset.description()));
            button.setOnAction(event -> {
                if (button.isSelected()) {
                    selectPreset(preset);
                } else {
                    button.setSelected(true);
                }
            });
            presetButtons.put(preset, button);
            grid.add(button, index % 2, index / 2);
            GridPane.setHgrow(button, Priority.ALWAYS);
            index++;
        }

        presetDescription = new Label();
        presetDescription.setWrapText(true);
        presetDescription.getStyleClass().add("preset-description");

        VBox card = new VBox(12, heading, grid, presetDescription);
        card.getStyleClass().add("card");
        card.setPrefWidth(570);
        return card;
    }

    private VBox createControlsCard() {
        Label heading = cardHeading("EFFECT CONTROLS");
        pitchSlider = slider(-12, 12, 0, 1);
        strengthSlider = slider(0, 100, 78, 10);
        echoSlider = slider(0, 100, 25, 10);
        reverbSlider = slider(0, 100, 20, 10);
        volumeSlider = slider(0, 150, 100, 10);

        VBox card = new VBox(9,
                heading,
                sliderRow("Pitch", pitchSlider, "semitones"),
                sliderRow("Effect strength", strengthSlider, "%"),
                sliderRow("Echo", echoSlider, "%"),
                sliderRow("Reverb", reverbSlider, "%"),
                sliderRow("Output volume", volumeSlider, "%"));
        card.getStyleClass().add("card");
        card.setPrefWidth(560);

        pitchSlider.valueProperty().addListener((obs, oldValue, newValue) -> pushSettings());
        strengthSlider.valueProperty().addListener((obs, oldValue, newValue) -> pushSettings());
        echoSlider.valueProperty().addListener((obs, oldValue, newValue) -> pushSettings());
        reverbSlider.valueProperty().addListener((obs, oldValue, newValue) -> pushSettings());
        volumeSlider.valueProperty().addListener((obs, oldValue, newValue) -> pushSettings());
        return card;
    }

    private VBox createMonitorCard() {
        Label heading = cardHeading("LIVE MONITOR");
        inputMeter = new ProgressBar(0);
        inputMeter.setMaxWidth(Double.MAX_VALUE);
        outputMeter = new ProgressBar(0);
        outputMeter.setMaxWidth(Double.MAX_VALUE);
        waveformView = new WaveformView();

        bypassCheck = new CheckBox("Bypass effects");
        muteCheck = new CheckBox("Mute output");
        bypassCheck.setOnAction(event -> pushSettings());
        muteCheck.setOnAction(event -> pushSettings());
        HBox switches = new HBox(22, bypassCheck, muteCheck);

        VBox card = new VBox(9,
                heading,
                fieldLabel("Microphone level"), inputMeter,
                fieldLabel("Processed output level"), outputMeter,
                waveformView,
                switches);
        card.getStyleClass().add("card");
        card.setPrefWidth(440);
        return card;
    }

    private HBox createFooter() {
        statusLabel = new Label("Ready — select devices and press Start");
        statusLabel.getStyleClass().add("status-label");

        startStopButton = new Button("START PROCESSING");
        startStopButton.getStyleClass().add("start-button");
        startStopButton.setOnAction(event -> toggleProcessing());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox footer = new HBox(18, statusLabel, spacer, startStopButton);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(15, 28, 18, 28));
        footer.getStyleClass().add("footer");
        return footer;
    }

    private void toggleProcessing() {
        if (audioEngine.isRunning()) {
            audioEngine.stop();
            updateRunningState(false);
            return;
        }

        AudioDevice input = inputDeviceBox.getValue();
        AudioDevice output = outputDeviceBox.getValue();
        if (input == null || output == null) {
            showError("Select both an input microphone and an output device.");
            return;
        }

        try {
            pushSettings();
            audioEngine.start(input, output);
            updateRunningState(true);
        } catch (LineUnavailableException | IllegalArgumentException ex) {
            showError("The selected audio format or device is unavailable.\n\n" + ex.getMessage());
            updateRunningState(false);
        }
    }

    private void updateRunningState(boolean running) {
        inputDeviceBox.setDisable(running);
        outputDeviceBox.setDisable(running);
        startStopButton.setText(running ? "STOP PROCESSING" : "START PROCESSING");
        startStopButton.getStyleClass().removeAll("start-button", "stop-button");
        startStopButton.getStyleClass().add(running ? "stop-button" : "start-button");
        if (!running) {
            inputMeter.setProgress(0);
            outputMeter.setProgress(0);
            waveformView.setSamples(new float[0]);
        }
    }

    private void refreshAudioDevices() {
        if (audioEngine.isRunning()) {
            setStatus("Stop processing before refreshing devices");
            return;
        }
        AudioDevice previousInput = inputDeviceBox == null ? null : inputDeviceBox.getValue();
        AudioDevice previousOutput = outputDeviceBox == null ? null : outputDeviceBox.getValue();
        if (inputDeviceBox == null || outputDeviceBox == null) {
            return;
        }

        inputDeviceBox.setItems(FXCollections.observableArrayList(AudioDeviceService.inputDevices(AudioEngine.FORMAT)));
        outputDeviceBox.setItems(FXCollections.observableArrayList(AudioDeviceService.outputDevices(AudioEngine.FORMAT)));
        inputDeviceBox.getSelectionModel().select(findMatching(inputDeviceBox, previousInput));
        outputDeviceBox.getSelectionModel().select(findMatching(outputDeviceBox, previousOutput));
        setStatus("Audio device list refreshed");
    }

    private int findMatching(ComboBox<AudioDevice> box, AudioDevice previous) {
        if (previous != null) {
            for (int i = 0; i < box.getItems().size(); i++) {
                if (box.getItems().get(i).toString().equals(previous.toString())) {
                    return i;
                }
            }
        }
        return box.getItems().isEmpty() ? -1 : 0;
    }

    private void selectPreset(VoicePreset preset) {
        selectedPreset = preset;
        ToggleButton button = presetButtons.get(preset);
        if (button != null) {
            button.setSelected(true);
        }
        presetDescription.setText(preset.displayName() + " — " + preset.description());

        switch (preset) {
            case ECHO -> echoSlider.setValue(68);
            case CAVE -> reverbSlider.setValue(72);
            case MONSTER -> {
                echoSlider.setValue(36);
                reverbSlider.setValue(42);
            }
            case DEMON -> {
                echoSlider.setValue(48);
                reverbSlider.setValue(56);
            }
            default -> {
                echoSlider.setValue(25);
                reverbSlider.setValue(20);
            }
        }
        pushSettings();
    }

    private void pushSettings() {
        if (pitchSlider == null) {
            return;
        }
        EffectSettings settings = new EffectSettings(
                selectedPreset,
                pitchSlider.getValue(),
                strengthSlider.getValue() / 100.0,
                echoSlider.getValue() / 100.0,
                reverbSlider.getValue() / 100.0,
                volumeSlider.getValue() / 100.0,
                bypassCheck.isSelected(),
                muteCheck.isSelected());
        audioEngine.setSettings(settings);
    }

    private void startUiTimeline() {
        uiTimeline = new Timeline(new KeyFrame(Duration.millis(50), event -> {
            inputMeter.setProgress(audioEngine.getInputLevel());
            outputMeter.setProgress(audioEngine.getOutputLevel());
            waveformView.setSamples(audioEngine.getWaveform());
        }));
        uiTimeline.setCycleCount(Timeline.INDEFINITE);
        uiTimeline.play();
    }

    private HBox sliderRow(String name, Slider slider, String unit) {
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("field-label");
        nameLabel.setMinWidth(120);
        Label valueLabel = new Label(formatSliderValue(slider.getValue(), unit));
        valueLabel.getStyleClass().add("slider-value");
        valueLabel.setMinWidth(74);
        valueLabel.setAlignment(Pos.CENTER_RIGHT);
        slider.valueProperty().addListener((obs, oldValue, newValue) ->
                valueLabel.setText(formatSliderValue(newValue.doubleValue(), unit)));
        HBox.setHgrow(slider, Priority.ALWAYS);
        HBox row = new HBox(10, nameLabel, slider, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static String formatSliderValue(double value, String unit) {
        if ("semitones".equals(unit)) {
            return String.format("%+.1f st", value);
        }
        return String.format("%.0f%s", value, unit);
    }

    private static Slider slider(double min, double max, double value, double blockIncrement) {
        Slider slider = new Slider(min, max, value);
        slider.setBlockIncrement(blockIncrement);
        slider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(slider, Priority.ALWAYS);
        return slider;
    }

    private static Label cardHeading(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("card-heading");
        return label;
    }

    private static Label fieldLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return label;
    }

    private static Label infoLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().add("info-label");
        return label;
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void showHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Virtual Microphone Routing Help");
        alert.setHeaderText("Route the processed voice into a calling application");
        alert.setContentText("1. Install VB-CABLE or another virtual audio cable.\n\n"
                + "2. In this application, select your physical microphone as Input.\n\n"
                + "3. Select CABLE Input as the Processed Output.\n\n"
                + "4. Press Start Processing.\n\n"
                + "5. In Discord, Google Meet, Zoom or a game, select CABLE Output as the microphone.\n\n"
                + "Use headphones to prevent feedback. If audio breaks up, close other applications that are using the same device or try the System Default option.");
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Vortex \u2014 Real-Time Voice Changer");
        alert.setHeaderText("Unable to start audio processing");
        alert.setContentText(message);
        alert.showAndWait();
        setStatus("Ready — check the selected devices and try again");
    }

    private void shutdown() {
        if (uiTimeline != null) {
            uiTimeline.stop();
        }
        audioEngine.close();
    }
}
