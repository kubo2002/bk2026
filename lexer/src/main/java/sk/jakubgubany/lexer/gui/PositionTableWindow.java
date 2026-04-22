package sk.jakubgubany.lexer.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Okno zobrazujúce zdrojový kód ako Tabuľku pozícií —
 * každý znak je zobrazený spolu s číslom riadku a stĺpca.
 * Pomáha interpretovať správy stopy, ktoré odkazujú na pozície ako [3:7].
 */
public class PositionTableWindow {

    private static final String SPACE_DISPLAY = "␠";
    private static final String TAB_DISPLAY = "⇥";
    private static final String NEWLINE_DISPLAY = "↵";

    /**
     * Opens a new window showing the Position Table for the given input text.
     *
     * @param input the source code string (from the main analyzer text area)
     */
    public static void show(String input) {
        Stage stage = new Stage();
        stage.setTitle("Source Code Position Table");
        WindowIcon.apply(stage);

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label hint = new Label(
                "Each character of the input source code is displayed with its line and column position.");
        hint.setWrapText(true);

        List<PositionEntry> entries = buildPositionEntries(input);
        ObservableList<PositionEntry> data = FXCollections.observableArrayList(entries);

        TableView<PositionEntry> table = new TableView<>(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<PositionEntry, Number> lineCol = new TableColumn<>("Line");
        lineCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getLine()));

        TableColumn<PositionEntry, Number> columnCol = new TableColumn<>("Column");
        columnCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getColumn()));

        TableColumn<PositionEntry, String> charCol = new TableColumn<>("Character");
        charCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCharacter()));

        table.getColumns().addAll(lineCol, columnCol, charCol);

        root.getChildren().addAll(hint, table);
        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

        Scene scene = new Scene(root, 400, 500);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Converts the input string into a list of position entries.
     * Line and column are 1-based. Whitespace is shown in readable form.
     *
     * @param input the raw source code
     * @return one entry per character (or one placeholder row if input is empty)
     */
    public static List<PositionEntry> buildPositionEntries(String input) {
        List<PositionEntry> entries = new ArrayList<>();

        if (input == null || input.isEmpty()) {
            entries.add(new PositionEntry(0, 0, "No input text available."));
            return entries;
        }

        int line = 1;
        int column = 1;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            String display = toDisplayString(c);
            entries.add(new PositionEntry(line, column, display));

            if (c == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
        }

        return entries;
    }

    private static String toDisplayString(char c) {
        if (c == ' ') {
            return SPACE_DISPLAY;
        }
        if (c == '\t') {
            return TAB_DISPLAY;
        }
        if (c == '\n') {
            return NEWLINE_DISPLAY;
        }
        if (c == '\r') {
            return "\\r";
        }
        return String.valueOf(c);
    }
}
