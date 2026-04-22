package sk.jakubgubany.lexer.gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sk.jakubgubany.lexer.engine.Lexer;
import sk.jakubgubany.lexer.language.LanguageAlphabet;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Configuration window for the language alphabet.
 * <p>
 * The alphabet defines which characters are accepted as valid input.
 * Operators and delimiters are intentionally absent here: their meaning
 * is determined by token rules (regular expressions), not by the alphabet.
 * If you need {@code +}, {@code (}, etc. to be valid input, list them in
 * the "Additional characters" field or rely on the default alphabet which
 * already includes all common operator and delimiter characters.
 */
public class AlphabetSettingsWindow {

    private final Stage    stage;
    private final Lexer    lexer;

    private CheckBox  allowLetters;
    private CheckBox  allowDigits;
    private CheckBox  allowWhitespace;
    private TextField additionalCharsField;

    public AlphabetSettingsWindow(Lexer lexer) {
        this.lexer  = lexer;
        this.stage  = new Stage();
        stage.setTitle("Alphabet Settings");
        WindowIcon.apply(stage);
        buildContent();
    }

    // -------------------------------------------------------------------------
    // UI construction
    // -------------------------------------------------------------------------

    private void buildContent() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(16));

        Label header = new Label(
                "Configure which characters are accepted as valid input.\n"
                + "Operators and delimiters are handled by token rules, not here.");
        header.setWrapText(true);
        header.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");

        allowLetters    = new CheckBox("Allow letters  (a-z, A-Z and Unicode)");
        allowDigits     = new CheckBox("Allow digits   (0-9 and Unicode)");
        allowWhitespace = new CheckBox("Allow whitespace  (space, tab, newline)");

        additionalCharsField = new TextField();
        additionalCharsField.setPromptText("e.g.  + - * / = < > ( ) { } , ; _");
        additionalCharsField.setTooltip(new Tooltip(
                "Every character you type here is added to the alphabet.\n"
                + "The default set already includes common operator and delimiter characters."));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(6);
        grid.add(new Label("Additional characters:"), 0, 0);
        grid.add(additionalCharsField, 1, 0);

        syncFromAlphabet(lexer.getAlphabet());

        Button applyBtn  = new Button("Apply");
        Button resetBtn  = new Button("Reset to default");
        Button cancelBtn = new Button("Cancel");

        applyBtn .setOnAction(e -> onApply());
        resetBtn .setOnAction(e -> onReset());
        cancelBtn.setOnAction(e -> stage.close());

        HBox buttons = new HBox(8, applyBtn, resetBtn, cancelBtn);

        root.getChildren().addAll(
                header,
                allowLetters,
                allowDigits,
                allowWhitespace,
                grid,
                buttons
        );

        stage.setScene(new Scene(root, 460, 250));
    }

    // -------------------------------------------------------------------------
    // Button handlers
    // -------------------------------------------------------------------------

    private void onApply() {
        LanguageAlphabet alphabet = new LanguageAlphabet(
                allowLetters.isSelected(),
                allowDigits.isSelected(),
                allowWhitespace.isSelected(),
                parseAdditionalCharacters(additionalCharsField.getText())
        );
        lexer.setAlphabet(alphabet);
        stage.close();
    }

    private void onReset() {
        LanguageAlphabet def = LanguageAlphabet.defaultAlphabet();
        lexer.setAlphabet(def);
        syncFromAlphabet(def);
    }

    // -------------------------------------------------------------------------
    // Sync helpers
    // -------------------------------------------------------------------------

    /**
     * Loads the current alphabet state into the UI controls.
     * Called on open and after a reset.
     */
    private void syncFromAlphabet(LanguageAlphabet a) {
        allowLetters   .setSelected(a.isAllowLetters());
        allowDigits    .setSelected(a.isAllowDigits());
        allowWhitespace.setSelected(a.isAllowWhitespace());

        // Build a compact string from the additional-character set.
        // LinkedHashSet preserves insertion order → stable, readable display.
        StringBuilder sb = new StringBuilder();
        for (Character c : a.getAdditionalCharacters()) {
            sb.append(c);
        }
        additionalCharsField.setText(sb.toString());
    }

    /**
     * Converts the raw text-field content to a character set.
     * Duplicate characters are silently removed (Set semantics).
     * Every character in the string is added as-is; no splitting by space or comma.
     */
    private static Set<Character> parseAdditionalCharacters(String text) {
        Set<Character> result = new LinkedHashSet<>();
        if (text == null) {
            return result;
        }
        for (int i = 0; i < text.length(); i++) {
            result.add(text.charAt(i));
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Shows the window and synchronises it with the lexer's current alphabet. */
    public void show() {
        syncFromAlphabet(lexer.getAlphabet());
        stage.show();
        stage.toFront();
    }
}
