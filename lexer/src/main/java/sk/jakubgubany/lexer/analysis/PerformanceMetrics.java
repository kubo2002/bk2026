package sk.jakubgubany.lexer.analysis;

/**
 * Uchováva výkonnostné a analytické štatistiky z jedného behu analyzátora.
 * Tieto údaje sa zobrazujú v GUI a využívajú sa v kapitole o výkonnosti v práci.
 */
public class PerformanceMetrics {

    private final int inputLength;
    private final int lineCount;
    private final int tokenCount;
    private final int errorCount;
    private final int traceStepCount;
    private final double processingTimeMs;
    private final int tokenDefinitionCount;
    private final int estimatedAutomatonStateCount;
    private final double tokensPerSecond;

    public PerformanceMetrics(
            int inputLength,
            int lineCount,
            int tokenCount,
            int errorCount,
            int traceStepCount,
            double processingTimeMs,
            int tokenDefinitionCount,
            int estimatedAutomatonStateCount,
            double tokensPerSecond) {
        this.inputLength = inputLength;
        this.lineCount = lineCount;
        this.tokenCount = tokenCount;
        this.errorCount = errorCount;
        this.traceStepCount = traceStepCount;
        this.processingTimeMs = processingTimeMs;
        this.tokenDefinitionCount = tokenDefinitionCount;
        this.estimatedAutomatonStateCount = estimatedAutomatonStateCount;
        this.tokensPerSecond = tokensPerSecond;
    }

    public int getInputLength() {
        return inputLength;
    }

    public int getLineCount() {
        return lineCount;
    }

    public int getTokenCount() {
        return tokenCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getTraceStepCount() {
        return traceStepCount;
    }

    public double getProcessingTimeMs() {
        return processingTimeMs;
    }

    public int getTokenDefinitionCount() {
        return tokenDefinitionCount;
    }

    public int getEstimatedAutomatonStateCount() {
        return estimatedAutomatonStateCount;
    }

    public double getTokensPerSecond() {
        return tokensPerSecond;
    }
}
