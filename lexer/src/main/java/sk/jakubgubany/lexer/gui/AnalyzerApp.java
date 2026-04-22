package sk.jakubgubany.lexer.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sk.jakubgubany.lexer.analysis.PerformanceMetrics;
import sk.jakubgubany.lexer.analysis.PerformanceMetricsBuilder;
import sk.jakubgubany.lexer.analysis.EstimatedAutomatonStateCounter;
import sk.jakubgubany.lexer.analysis.TraceStep;
import sk.jakubgubany.lexer.engine.Lexer;
import sk.jakubgubany.lexer.engine.LexerError;
import sk.jakubgubany.lexer.engine.LexerResult;
import sk.jakubgubany.lexer.engine.TokenExporter;
import sk.jakubgubany.lexer.token.LanguageTokenDefinitions;
import sk.jakubgubany.lexer.token.Token;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * Simple JavaFX desktop GUI for the lexical analyzer.
 * <p>
 * Allows the user to enter source code, run the lexer, and view
 * tokens, errors, and analysis trace in a clear layout.
 */
public class AnalyzerApp extends Application {

    private static final String DEFAULT_EXAMPLE =
            "int a = 5;\n" +
            "double value = 12.5;\n" +
            "if (a <= 10) {\n" +
            "    return a;\n" +
            "}";

    private TextArea sourceArea;
    private TextArea tokensArea;
    private TextArea errorsArea;
    private TextArea traceArea;

    private Lexer lexer = new Lexer(java.util.Collections.emptyList(), java.util.Collections.emptySet());
    /** Latest metrics from the last successful analysis; null if none run yet. */
    private PerformanceMetrics lastMetrics;
    /** Latest scan result; null until the first successful analysis. */
    private LexerResult lastResult;
    /** Single instance reused across openings so rules are preserved. */
    private TokenRulesWindow tokenRulesWindow;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // ── Top bar: icon + title + buttons ──────────────────────────────────
        VBox top = new VBox(6);
        top.setAlignment(Pos.CENTER_LEFT);

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        try (java.io.InputStream iconStream = getClass().getResourceAsStream("/icon.png")) {
            if (iconStream != null) {
                ImageView iconView = new ImageView(new Image(iconStream));
                iconView.setFitWidth(115);
                iconView.setFitHeight(115);
                iconView.setPreserveRatio(true);
                titleRow.getChildren().add(iconView);
            }
        } catch (Exception ignored) {}

        Label title = new Label("Lexeur");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");
        titleRow.getChildren().add(title);
        top.getChildren().add(titleRow);
        top.getChildren().add(createButtons());
        root.setTop(top);

        // ── Left panel: source code ───────────────────────────────────────────
        sourceArea = new TextArea();
        sourceArea.setPromptText("Enter source code here...");
        sourceArea.setWrapText(false);

        Label sourceLabel = new Label("Source code");
        sourceLabel.setStyle("-fx-font-weight: bold;");
        VBox leftPanel = new VBox(4, sourceLabel, sourceArea);
        VBox.setVgrow(sourceArea, Priority.ALWAYS);

        // ── Right-top panel: tokens ───────────────────────────────────────────
        tokensArea = new TextArea();
        tokensArea.setEditable(false);
        tokensArea.setPromptText("Tokens will appear here after analysis.");
        tokensArea.setWrapText(false);

        Label tokensLabel = new Label("Tokens");
        tokensLabel.setStyle("-fx-font-weight: bold;");
        VBox tokensPanel = new VBox(4, tokensLabel, tokensArea);
        VBox.setVgrow(tokensArea, Priority.ALWAYS);

        // ── Right-bottom panel: errors + trace side by side ───────────────────
        errorsArea = new TextArea();
        errorsArea.setEditable(false);
        errorsArea.setPromptText("Lexical errors (if any) will appear here.");
        errorsArea.setWrapText(true);

        traceArea = new TextArea();
        traceArea.setEditable(false);
        traceArea.setPromptText("Analysis trace will appear here.");
        traceArea.setWrapText(false);

        Label errorsLabel = new Label("Lexical errors");
        errorsLabel.setStyle("-fx-font-weight: bold;");
        VBox errorsPanel = new VBox(4, errorsLabel, errorsArea);
        VBox.setVgrow(errorsArea, Priority.ALWAYS);

        Label traceLabel = new Label("Analysis trace");
        traceLabel.setStyle("-fx-font-weight: bold;");
        VBox tracePanel = new VBox(4, traceLabel, traceArea);
        VBox.setVgrow(traceArea, Priority.ALWAYS);

        SplitPane bottomSplit = new SplitPane(errorsPanel, tracePanel);
        bottomSplit.setDividerPositions(0.4);

        // ── Right column: tokens (top) + errors/trace (bottom) ───────────────
        SplitPane rightSplit = new SplitPane();
        rightSplit.setOrientation(javafx.geometry.Orientation.VERTICAL);
        rightSplit.getItems().addAll(tokensPanel, bottomSplit);
        rightSplit.setDividerPositions(0.55);

        // ── Main horizontal split ─────────────────────────────────────────────
        SplitPane mainSplit = new SplitPane(leftPanel, rightSplit);
        mainSplit.setDividerPositions(0.42);
        root.setCenter(mainSplit);

        Scene scene = new Scene(root, 1100, 720);
        stage.setTitle("Lexeur");
        stage.setScene(scene);

        WindowIcon.apply(stage);

        stage.show();
    }

    private HBox createButtons() {
        Button analyzeBtn = new Button("Analyze");
        analyzeBtn.setOnAction(e -> onAnalyze());

        Button loadExampleBtn = new Button("Load example");
        loadExampleBtn.setOnAction(e -> onLoadExample());

        Button loadFileBtn = new Button("Load File");
        loadFileBtn.setOnAction(e -> onLoadFile());

        Button positionTableBtn = new Button("Show Position Table");
        positionTableBtn.setOnAction(e -> onShowPositionTable());

        Button alphabetSettingsBtn = new Button("Alphabet Settings");
        alphabetSettingsBtn.setOnAction(e -> onAlphabetSettings());

        Button performanceMetricsBtn = new Button("Show Performance Metrics");
        performanceMetricsBtn.setOnAction(e -> onShowPerformanceMetrics());

        Button tokenRulesBtn = new Button("Token Rules");
        tokenRulesBtn.setOnAction(e -> onTokenRules());

        Button exportJsonBtn = new Button("Export to JSON");
        exportJsonBtn.setOnAction(e -> onExportJson());

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(10, 0, 0, 0));
        box.getChildren().addAll(analyzeBtn, loadExampleBtn, loadFileBtn, positionTableBtn, alphabetSettingsBtn, performanceMetricsBtn, tokenRulesBtn, exportJsonBtn);
        return box;
    }

    private void onExportJson() {
        if (lastResult == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export to JSON");
            alert.setHeaderText("No analysis data yet");
            alert.setContentText("Run Analyze first, then export.");
            alert.showAndWait();
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Tokens to JSON");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));
        chooser.setInitialFileName("tokens.json");
        File file = chooser.showSaveDialog(null);
        if (file == null) {
            return;
        }

        try {
            String json = TokenExporter.exportResultToJson(lastResult);
            Files.writeString(file.toPath(), json, StandardCharsets.UTF_8);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export to JSON");
            alert.setHeaderText("Export successful");
            alert.setContentText("Saved to:\n" + file.getAbsolutePath());
            alert.showAndWait();
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export to JSON");
            alert.setHeaderText("Export failed");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    private void onTokenRules() {
        if (tokenRulesWindow == null) {
            tokenRulesWindow = new TokenRulesWindow(definitions -> {
                lexer = new Lexer(definitions, LanguageTokenDefinitions.getKeywords());
            });
        }
        tokenRulesWindow.show();
    }

    private void onAlphabetSettings() {
        AlphabetSettingsWindow window = new AlphabetSettingsWindow(lexer);
        window.show();
    }

    private void onShowPositionTable() {
        String input = sourceArea.getText();
        if (input == null) {
            input = "";
        }
        PositionTableWindow.show(input);
    }

    private void onAnalyze() {
        String input = sourceArea.getText();
        if (input == null) {
            input = "";
        }

        if (input.isBlank()) {
            tokensArea.setText("");
            errorsArea.setText("No lexical errors.");
            traceArea.setText("Enter source code and click Analyze to see results.");
            lastMetrics = null;
            lastResult  = null;
            return;
        }

        try {
            long start = System.nanoTime();
            LexerResult result = lexer.scan(input);
            long end = System.nanoTime();
            double durationMs = (end - start) / 1000000.0;

            lastResult = result;

            tokensArea.setText(formatTokens(result.getTokens()));
            errorsArea.setText(formatErrors(result.getErrors()));
            traceArea.setText(formatTrace(result.getTraceSteps()));

            int tokenDefCount = lexer.getTokenDefinitionCount();
            int estimatedStates = EstimatedAutomatonStateCounter.estimate(lexer.getDefinitions());
            lastMetrics = PerformanceMetricsBuilder.build(
                    input, result, durationMs, tokenDefCount, estimatedStates);
        } catch (Exception ex) {
            errorsArea.setText("Error during analysis: " + ex.getMessage());
            traceArea.setText("An exception occurred. Check the errors area.");
            lastMetrics = null;
            lastResult  = null;
        }
    }

    private void onShowPerformanceMetrics() {
        if (lastMetrics == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Performance Metrics");
            alert.setHeaderText("No analysis data yet");
            alert.setContentText("Run Analyze first, then open this window to see performance metrics.");
            alert.showAndWait();
            return;
        }
        PerformanceMetricsWindow.show(lastMetrics);
    }


    private void onLoadExample() {
        sourceArea.setText(DEFAULT_EXAMPLE);
    }

    private void onLoadFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Source File");
        File file = chooser.showOpenDialog(null);
        if (file == null) {
            return;
        }
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            sourceArea.setText(content);
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Load File");
            alert.setHeaderText("Could not read file");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    private String formatTokens(List<Token> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return "(no tokens)";
        }
        StringBuilder sb = new StringBuilder();
        for (Token t : tokens) {
            sb.append(t.getType()).append("\t\"").append(t.getLexeme())
                    .append("\"\tline ").append(t.getLine())
                    .append(", column ").append(t.getColumn()).append("\n");
        }
        return sb.toString().trim();
    }

    private String formatErrors(List<LexerError> errors) {
        if (errors == null || errors.isEmpty()) {
            return "No lexical errors.";
        }
        StringBuilder sb = new StringBuilder();
        for (LexerError err : errors) {
            sb.append(err.getMessage()).append(" at line ")
                    .append(err.getLine()).append(", column ")
                    .append(err.getColumn()).append("\n");
        }
        return sb.toString().trim();
    }

    private String formatTrace(List<TraceStep> steps) {
        if (steps == null || steps.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (TraceStep step : steps) {
            sb.append("[").append(step.getCurrentLine()).append(":")
                    .append(step.getCurrentColumn()).append("] ")
                    .append(step.getMessage()).append("\n");
        }
        return sb.toString().trim();
    }

    /**
     * JavaFX entry point. Run this class to start the GUI.
     */
    public static void main(String[] args) {
        Application.launch(AnalyzerApp.class, args);
    }
}
