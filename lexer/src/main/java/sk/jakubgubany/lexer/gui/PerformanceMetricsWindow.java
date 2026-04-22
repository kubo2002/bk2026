package sk.jakubgubany.lexer.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sk.jakubgubany.lexer.analysis.PerformanceMetrics;

/**
 * A window that displays performance and analysis statistics for the last lexer run.
 * <p>
 * Shows input statistics, lexer result counts, processing time, and language/automata
 * metrics for use in the thesis chapter on performance testing.
 */
public class PerformanceMetricsWindow {

    private static final int LABEL_WIDTH = 220;
    private static final int GAP = 10;

    /**
     * Opens a new window showing the given metrics.
     *
     * @param metrics the metrics from the last analysis (must not be null)
     */
    public static void show(PerformanceMetrics metrics) {
        if (metrics == null) {
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Performance Metrics");
        WindowIcon.apply(stage);

        VBox root = new VBox(15);
        root.setPadding(new Insets(15));

        Label title = new Label("Performance Metrics");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label hint = new Label("Statistics from the last lexical analysis run.");
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");

        GridPane inputGrid = createSectionGrid("Input statistics",
                "Input length:", metrics.getInputLength() + " characters",
                "Line count:", String.valueOf(metrics.getLineCount()));

        GridPane resultGrid = createSectionGrid("Lexer result",
                "Recognized tokens:", String.valueOf(metrics.getTokenCount()),
                "Lexical errors:", String.valueOf(metrics.getErrorCount()),
                "Trace steps:", String.valueOf(metrics.getTraceStepCount()));

        GridPane perfGrid = createSectionGrid("Performance",
                "Processing time:", String.format("%.2f ms", metrics.getProcessingTimeMs()),
                "Tokens per second:", String.format("%.2f", metrics.getTokensPerSecond()));

        GridPane langGrid = createSectionGrid("Language / automata",
                "Token definitions:", String.valueOf(metrics.getTokenDefinitionCount()),
                "Estimated automaton states:", String.valueOf(metrics.getEstimatedAutomatonStateCount()));

        root.getChildren().addAll(title, hint, inputGrid, resultGrid, perfGrid, langGrid);

        Scene scene = new Scene(root, 420, 420);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Creates a GridPane with a section title and two columns: metric name, value.
     * Pairs of (label, value) are added in order; the number of pairs is inferred.
     */
    private static GridPane createSectionGrid(String sectionTitle, String... labelValuePairs) {
        GridPane grid = new GridPane();
        grid.setHgap(GAP);
        grid.setVgap(5);
        grid.setAlignment(Pos.TOP_LEFT);

        Label section = new Label(sectionTitle);
        section.setStyle("-fx-font-weight: bold; -fx-underline: true;");
        grid.add(section, 0, 0, 2, 1);

        int row = 1;
        for (int i = 0; i < labelValuePairs.length; i += 2) {
            if (i + 1 >= labelValuePairs.length) {
                break;
            }
            Label name = new Label(labelValuePairs[i]);
            name.setMinWidth(LABEL_WIDTH);
            Label value = new Label(labelValuePairs[i + 1]);
            grid.add(name, 0, row);
            grid.add(value, 1, row);
            row++;
        }
        return grid;
    }
}
