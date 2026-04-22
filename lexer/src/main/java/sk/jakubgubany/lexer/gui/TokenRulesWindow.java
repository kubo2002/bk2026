package sk.jakubgubany.lexer.gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import sk.jakubgubany.lexer.config.TokenDefinitionConfig;
import sk.jakubgubany.lexer.token.TokenDefinition;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * JavaFX window for managing custom token rules.
 * <p>
 * The window shows a fully editable {@code TableView} where each row
 * represents one token rule. The user can:
 * <ul>
 *   <li>Add / remove rows</li>
 *   <li>Save the rule list to a JSON file and reload it later</li>
 *   <li>Apply the rules to the running lexer via the {@code onApply} callback</li>
 * </ul>
 *
 * All data operations are delegated to {@link TokenRulesController}.
 * This class only builds and wires up the scene graph.
 */
public class TokenRulesWindow {

    private final Stage                          stage;
    private final TokenRulesController           controller;
    private final Consumer<List<TokenDefinition>> onApply;

    private TableView<TokenDefinitionConfig> table;

    /**
     * @param onApply called with the converted {@link TokenDefinition} list
     *                when the user confirms with "Apply Rules"
     */
    public TokenRulesWindow(Consumer<List<TokenDefinition>> onApply) {
        this.onApply     = onApply;
        this.controller  = new TokenRulesController();
        this.stage       = new Stage();
        stage.setTitle("Token Rules Configuration");
        WindowIcon.apply(stage);
        buildScene();
    }

    /** Shows the window and brings it to the foreground. */
    public void show() {
        stage.show();
        stage.toFront();
    }

    // =========================================================================
    // Scene construction
    // =========================================================================

    private void buildScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        VBox center = new VBox(6);
        Label hint = new Label(
                "Double-click a cell to edit it. "
                + "Priority: lower number = rule is tried first.");
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        center.getChildren().addAll(hint, table);

        root.setCenter(center);
        root.setBottom(buildButtonBar());

        stage.setScene(new Scene(root, 880, 460));
    }

    // -------------------------------------------------------------------------
    // Table
    // -------------------------------------------------------------------------

    private TableView<TokenDefinitionConfig> buildTable() {
        TableView<TokenDefinitionConfig> tv = new TableView<>(controller.getRules());
        tv.setEditable(true);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setPlaceholder(
                new Label("No rules defined. Click \"Add Rule\" to add one."));

        tv.getColumns().addAll(
                nameColumn(),
                regexColumn(),
                priorityColumn(),
                skipColumn(),
                noteColumn()
        );
        return tv;
    }

    private TableColumn<TokenDefinitionConfig, String> nameColumn() {
        TableColumn<TokenDefinitionConfig, String> col =
                new TableColumn<>("Token Name");
        col.setCellValueFactory(data -> data.getValue().nameProperty());
        col.setCellFactory(TextFieldTableCell.forTableColumn());
        col.setOnEditCommit(e -> e.getRowValue().setName(e.getNewValue()));
        col.setPrefWidth(150);
        return col;
    }

    private TableColumn<TokenDefinitionConfig, String> regexColumn() {
        TableColumn<TokenDefinitionConfig, String> col =
                new TableColumn<>("Regex Pattern");
        col.setCellValueFactory(data -> data.getValue().regexProperty());
        col.setCellFactory(TextFieldTableCell.forTableColumn());
        col.setOnEditCommit(e -> {
            String newRegex = e.getNewValue();
            try {
                // Validate immediately on edit so the user gets instant feedback.
                java.util.regex.Pattern.compile(newRegex);
                e.getRowValue().setRegex(newRegex);
            } catch (java.util.regex.PatternSyntaxException ex) {
                showError("Invalid Regex Pattern",
                        "Pattern \"" + newRegex + "\" is not valid:\n"
                                + ex.getDescription());
                // Revert the cell to the previous value.
                table.refresh();
            }
        });
        col.setPrefWidth(230);
        return col;
    }

    private TableColumn<TokenDefinitionConfig, Integer> priorityColumn() {
        TableColumn<TokenDefinitionConfig, Integer> col =
                new TableColumn<>("Priority");
        col.setCellValueFactory(
                data -> data.getValue().priorityProperty().asObject());
        col.setCellFactory(
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        col.setOnEditCommit(e -> {
            Integer val = e.getNewValue();
            if (val != null) {
                e.getRowValue().setPriority(val);
            }
        });
        col.setPrefWidth(70);
        return col;
    }

    private TableColumn<TokenDefinitionConfig, Boolean> skipColumn() {
        TableColumn<TokenDefinitionConfig, Boolean> col =
                new TableColumn<>("Skip");
        col.setCellValueFactory(data -> data.getValue().skipProperty());
        // CheckBoxTableCell binds directly to the BooleanProperty –
        // clicking the checkbox updates the model with no extra handler needed.
        col.setCellFactory(CheckBoxTableCell.forTableColumn(col));
        col.setEditable(true);
        col.setPrefWidth(55);
        return col;
    }

    private TableColumn<TokenDefinitionConfig, String> noteColumn() {
        TableColumn<TokenDefinitionConfig, String> col =
                new TableColumn<>("Note");
        col.setCellValueFactory(data -> data.getValue().noteProperty());
        col.setCellFactory(TextFieldTableCell.forTableColumn());
        col.setOnEditCommit(e -> e.getRowValue().setNote(e.getNewValue()));
        col.setPrefWidth(220);
        return col;
    }

    // -------------------------------------------------------------------------
    // Button bar
    // -------------------------------------------------------------------------

    private HBox buildButtonBar() {
        Button addBtn    = new Button("Add Rule");
        Button removeBtn = new Button("Remove Rule");
        Button saveBtn   = new Button("Save Configuration");
        Button loadBtn   = new Button("Load Configuration");
        Button applyBtn  = new Button("Apply Rules");
        applyBtn.setStyle("-fx-font-weight: bold;");

        addBtn   .setOnAction(e -> onAdd());
        removeBtn.setOnAction(e -> onRemove());
        saveBtn  .setOnAction(e -> onSave());
        loadBtn  .setOnAction(e -> onLoad());
        applyBtn .setOnAction(e -> onApplyRules());

        HBox bar = new HBox(8,
                addBtn, removeBtn, saveBtn, loadBtn, applyBtn);
        bar.setPadding(new Insets(10, 0, 0, 0));
        return bar;
    }

    // =========================================================================
    // Button handlers
    // =========================================================================

    private void onAdd() {
        controller.addRule();
        int last = controller.getRules().size() - 1;
        table.scrollTo(last);
        table.getSelectionModel().select(last);
        // Start editing the name cell immediately for a smoother UX.
        table.edit(last, (TableColumn<TokenDefinitionConfig, ?>) table.getColumns().get(0));
    }

    private void onRemove() {
        TokenDefinitionConfig selected =
                table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("No Row Selected",
                    "Click a row to select it, then click Remove Rule.");
            return;
        }
        controller.removeRule(selected);
    }

    private void onSave() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Token Rules Configuration");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));
        chooser.setInitialFileName("token_rules.json");
        File file = chooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        try {
            controller.saveToFile(file);
            showInfo("Saved",
                    "Configuration saved to:\n" + file.getAbsolutePath());
        } catch (IOException ex) {
            showError("Save Error", ex.getMessage());
        }
    }

    private void onLoad() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load Token Rules Configuration");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));
        File file = chooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }
        try {
            controller.loadFromFile(file);
        } catch (IOException ex) {
            showError("Load Error", ex.getMessage());
        }
    }

    private void onApplyRules() {
        String error = controller.validate();
        if (error != null) {
            showError("Invalid Configuration", error);
            return;
        }
        if (onApply != null) {
            onApply.accept(controller.toTokenDefinitions());
        }
        stage.close();
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
