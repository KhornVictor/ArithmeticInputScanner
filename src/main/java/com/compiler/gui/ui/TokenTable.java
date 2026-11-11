package com.compiler.gui.ui;

import com.compiler.model.Token;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

/**
 * Custom TableView for displaying recognized tokens with color-coded category badges
 * and real-time query filtering.
 */
public class TokenTable extends TableView<Token> {

    private final ObservableList<Token> masterData = FXCollections.observableArrayList();
    private final FilteredList<Token> filteredData;

    public TokenTable() {
        getStyleClass().add("table-view");

        TableColumn<Token, String> colType = new TableColumn<>("TOKEN TYPE");
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTypeName()));
        colType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Token token = getTableView().getItems().get(getIndex());
                    Label pill = new Label(item);
                    pill.getStyleClass().add("token-badge");
                    String color = token.getCategoryColor();
                    pill.setStyle(String.format(
                            "-fx-background-color: %s18; -fx-text-fill: %s; -fx-border-color: %s; -fx-border-radius: 20px;",
                            color, color, color));
                    setGraphic(pill);
                    setText(null);
                }
            }
        });
        colType.setPrefWidth(140);

        TableColumn<Token, String> colLexeme = new TableColumn<>("LEXEME");
        colLexeme.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLexeme()));
        colLexeme.setStyle("-fx-font-family: monospace; -fx-font-weight: 800; -fx-font-size: 13px;");
        colLexeme.setPrefWidth(130);

        TableColumn<Token, String> colPos = new TableColumn<>("LINE : COL");
        colPos.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLine() + " : " + c.getValue().getColumn()));
        colPos.setPrefWidth(90);

        TableColumn<Token, String> colCategory = new TableColumn<>("CATEGORY");
        colCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        colCategory.setPrefWidth(120);

        getColumns().addAll(colType, colLexeme, colPos, colCategory);

        filteredData = new FilteredList<>(masterData, p -> true);
        setItems(filteredData);
    }

    public void setTokens(List<Token> tokens) {
        masterData.clear();
        if (tokens != null) {
            masterData.addAll(tokens);
        }
    }

    public void filter(String query) {
        filteredData.setPredicate(token -> {
            if (query == null || query.isBlank()) return true;
            String lower = query.toLowerCase();
            return token.getTypeName().toLowerCase().contains(lower)
                    || token.getLexeme().toLowerCase().contains(lower)
                    || token.getCategory().toLowerCase().contains(lower);
        });
    }

    public ObservableList<Token> getMasterData() {
        return masterData;
    }
}
