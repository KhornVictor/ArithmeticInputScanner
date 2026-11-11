package com.compiler.gui.ui;

import javafx.scene.control.Alert;
import javafx.stage.Window;

/**
 * Utility helper for displaying JavaFX modal alert dialogs.
 */
public final class AlertHelper {

    private AlertHelper() {}

    public static void showAlert(Window owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
