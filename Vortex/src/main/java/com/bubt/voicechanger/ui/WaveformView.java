package com.bubt.voicechanger.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public final class WaveformView extends Region {
    private final Canvas canvas = new Canvas();
    private float[] samples = new float[0];

    public WaveformView() {
        getChildren().add(canvas);
        setMinHeight(110);
        setPrefHeight(145);
        setMaxHeight(180);
    }

    public void setSamples(float[] samples) {
        this.samples = samples == null ? new float[0] : samples;
        draw();
    }

    @Override
    protected void layoutChildren() {
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
        draw();
    }

    private void draw() {
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        graphics.setFill(Color.web("#0A1622"));
        graphics.fillRoundRect(0, 0, width, height, 14, 14);
        graphics.setStroke(Color.web("#173A4B"));
        graphics.strokeLine(0, height / 2.0, width, height / 2.0);

        if (samples.length < 2) {
            graphics.setFill(Color.web("#78909C"));
            graphics.fillText("Waveform appears while processing is active", 18, height / 2.0 - 10);
            return;
        }

        graphics.setStroke(Color.web("#21D4C2"));
        graphics.setLineWidth(2.0);
        double step = width / (samples.length - 1.0);
        graphics.beginPath();
        graphics.moveTo(0, height / 2.0 - samples[0] * height * 0.42);
        for (int i = 1; i < samples.length; i++) {
            double x = i * step;
            double y = height / 2.0 - samples[i] * height * 0.42;
            graphics.lineTo(x, y);
        }
        graphics.stroke();
    }
}
