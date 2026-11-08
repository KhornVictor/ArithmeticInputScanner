package com.compiler.gui;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class DfaCanvas extends Canvas {

    private String simulatedInput = "12.5";
    private final List<Integer> activeStates = new ArrayList<>();
    private boolean darkMode = false;
    
    public DfaCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
        draw();
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setSimulatedInput(String input) {
        this.simulatedInput = input != null ? input.trim() : "";
        computeActiveStates();
        draw();
    }

    private void computeActiveStates() {
        activeStates.clear();
        int current = 0; // S0
        activeStates.add(current);

        for (int i = 0; i < simulatedInput.length(); i++) {
            char c = simulatedInput.charAt(i);
            int next = -1;
            switch (current) {
                case 0 -> {
                    if (c == '0') next = 1;
                    else if (c >= '1' && c <= '9') next = 2;
                }
                case 1 -> {
                    if (c == '.') next = 3;
                }
                case 2 -> {
                    if (c >= '0' && c <= '9') next = 2;
                    else if (c == '.') next = 3;
                }
                case 3 -> {
                    if (c >= '0' && c <= '9') next = 4;
                }
                case 4 -> {
                    if (c >= '0' && c <= '9') next = 4;
                }
            }
            if (next == -1) break;
            current = next;
            activeStates.add(current);
        }
    }

    public final void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        // 1. Background
        gc.setFill(darkMode ? Color.web("#1e1e2e") : Color.web("#ffffff"));
        gc.fillRect(0, 0, w, h);

        // Border around canvas in light mode
        if (!darkMode) {
            gc.setStroke(Color.web("#e2e8f0"));
            gc.setLineWidth(1);
            gc.strokeRect(0, 0, w, h);
        }

        // State coordinates
        double y = h / 2.0;
        double x0 = 80;
        double x1 = 200;
        double x2 = 320;
        double x3 = 460;
        double x4 = 600;
        double r = 26;

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        // Draw Start Arrow into S0
        drawArrow(gc, 30, y, x0 - r, y, "start", 0, -14);

        // Transition S0 -> S1 ('0')
        drawCurvedArrow(gc, x0, y - r, x1, y - r, "'0'", -35);

        // Transition S0 -> S2 ('1'-'9')
        drawCurvedArrow(gc, x0, y + r, x2, y + r, "'1'-'9'", 45);

        // Loop S2 -> S2 ('0'-'9')
        drawSelfLoop(gc, x2, y - r, "'0'-'9'");

        // Transition S1 -> S3 ('.')
        drawCurvedArrow(gc, x1, y - r, x3, y - r, "'.'", -50);

        // Transition S2 -> S3 ('.')
        drawArrow(gc, x2 + r, y, x3 - r, y, "'.'", 0, -14);

        // Transition S3 -> S4 ('0'-'9')
        drawArrow(gc, x3 + r, y, x4 - r, y, "'0'-'9'", 0, -14);

        // Loop S4 -> S4 ('0'-'9')
        drawSelfLoop(gc, x4, y - r, "'0'-'9'");

        // Draw States
        drawState(gc, 0, x0, y, r, "S0\nStart", false);
        drawState(gc, 1, x1, y - 40, r, "S1\n'0'", false);
        drawState(gc, 2, x2, y, r, "S2\nDigits", false);
        drawState(gc, 3, x3, y, r, "S3\nDot", false);
        drawState(gc, 4, x4, y, r, "S4\nDECIMAL", true);

        // Legend & Notes at bottom
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Segoe UI", 12));
        gc.setFill(darkMode ? Color.web("#a6adc8") : Color.web("#64748b"));
        gc.fillText("DFA Token Pattern: (0 | [1-9][0-9]*)\\.[0-9]+   |   S4 is the accepting state (double ring).", 30, h - 25);
    }

    private void drawState(GraphicsContext gc, int stateId, double x, double y, double r, String label, boolean isAccepting) {
        boolean isActive = activeStates.contains(stateId);
        boolean isCurrent = !activeStates.isEmpty() && activeStates.get(activeStates.size() - 1) == stateId;

        // Outer ring
        if (isActive) {
            Color activeColor = darkMode ? Color.web("#89b4fa") : Color.web("#0284c7");
            Color ringColor = isCurrent ? (darkMode ? Color.web("#a6e3a1") : Color.web("#16a34a")) : activeColor;
            gc.setFill(activeColor);
            gc.setStroke(ringColor);
        } else {
            gc.setFill(darkMode ? Color.web("#313244") : Color.web("#f1f5f9"));
            gc.setStroke(darkMode ? Color.web("#585b70") : Color.web("#cbd5e1"));
        }
        gc.setLineWidth(isActive ? 3 : 2);
        gc.fillOval(x - r, y - r, r * 2, r * 2);
        gc.strokeOval(x - r, y - r, r * 2, r * 2);

        // Double ring for accepting state
        if (isAccepting) {
            Color acceptColor = isActive
                    ? (darkMode ? Color.web("#a6e3a1") : Color.web("#ffffff"))
                    : (darkMode ? Color.web("#a6adc8") : Color.web("#0284c7"));
            gc.setStroke(acceptColor);
            gc.strokeOval(x - r + 5, y - r + 5, (r - 5) * 2, (r - 5) * 2);
        }

        // Label
        if (isActive) {
            gc.setFill(darkMode ? Color.web("#11111b") : Color.web("#ffffff"));
        } else {
            gc.setFill(darkMode ? Color.web("#cdd6f4") : Color.web("#0f172a"));
        }
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        String[] lines = label.split("\n");
        if (lines.length > 1) {
            gc.fillText(lines[0], x, y - 5);
            gc.setFont(Font.font("Segoe UI", 9));
            gc.fillText(lines[1], x, y + 7);
        } else {
            gc.fillText(label, x, y);
        }
    }

    private void drawArrow(GraphicsContext gc, double x1, double y1, double x2, double y2, String label, double lx, double ly) {
        Color arrowColor = darkMode ? Color.web("#89b4fa") : Color.web("#0284c7");
        gc.setStroke(arrowColor);
        gc.setLineWidth(1.8);
        gc.strokeLine(x1, y1, x2, y2);

        // Arrow head
        drawArrowHead(gc, x1, y1, x2, y2);

        // Label
        gc.setFill(darkMode ? Color.web("#fab387") : Color.web("#d97706"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        gc.fillText(label, (x1 + x2) / 2.0 + lx, (y1 + y2) / 2.0 + ly);
    }

    private void drawCurvedArrow(GraphicsContext gc, double x1, double y1, double x2, double y2, String label, double curveOffset) {
        Color arrowColor = darkMode ? Color.web("#89b4fa") : Color.web("#0284c7");
        gc.setStroke(arrowColor);
        gc.setLineWidth(1.8);

        double midX = (x1 + x2) / 2.0;
        double midY = (y1 + y2) / 2.0 + curveOffset;

        gc.beginPath();
        gc.moveTo(x1, y1);
        gc.quadraticCurveTo(midX, midY, x2, y2);
        gc.stroke();

        drawArrowHead(gc, midX, midY, x2, y2);

        gc.setFill(darkMode ? Color.web("#fab387") : Color.web("#d97706"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        gc.fillText(label, midX, midY + (curveOffset < 0 ? -8 : 16));
    }

    private void drawSelfLoop(GraphicsContext gc, double x, double y, String label) {
        Color arrowColor = darkMode ? Color.web("#89b4fa") : Color.web("#0284c7");
        gc.setStroke(arrowColor);
        gc.setLineWidth(1.8);
        gc.strokeOval(x - 18, y - 36, 36, 36);

        drawArrowHead(gc, x + 10, y - 20, x + 10, y);

        gc.setFill(darkMode ? Color.web("#fab387") : Color.web("#d97706"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        gc.fillText(label, x, y - 42);
    }

    private void drawArrowHead(GraphicsContext gc, double fromX, double fromY, double toX, double toY) {
        double angle = Math.atan2(toY - fromY, toX - fromX);
        double len = 9;
        double a1 = angle - Math.PI / 6.0;
        double a2 = angle + Math.PI / 6.0;

        gc.setFill(darkMode ? Color.web("#89b4fa") : Color.web("#0284c7"));
        double[] xs = {toX, toX - len * Math.cos(a1), toX - len * Math.cos(a2)};
        double[] ys = {toY, toY - len * Math.sin(a1), toY - len * Math.sin(a2)};
        gc.fillPolygon(xs, ys, 3);
    }
}
