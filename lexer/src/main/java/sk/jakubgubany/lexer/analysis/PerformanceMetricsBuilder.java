package sk.jakubgubany.lexer.analysis;

import sk.jakubgubany.lexer.engine.LexerResult;

/**
 * Zostaví objekt PerformanceMetrics zo vstupu, výsledku analýzy a merania času.
 * Počet riadkov: prázdny vstup = 0 riadkov, inak počet znakov '\n' + 1.
 */
public final class PerformanceMetricsBuilder {

    private PerformanceMetricsBuilder() {
        // Pomocná trieda bez inštancií
    }

    /**
     * Zostaví metriky zo zadaných vstupov.
     *
     * @param input                       analyzovaný zdrojový reťazec (môže byť prázdny)
     * @param result                      výsledok z lexer.scan(input)
     * @param processingTimeMs            trvanie analýzy v milisekundách
     * @param tokenDefinitionCount        počet definícií tokenov v analyzátore
     * @param estimatedAutomatonStateCount odhadovaný počet stavov automatu
     * @return nová inštancia PerformanceMetrics
     */
    public static PerformanceMetrics build(
            String input,
            LexerResult result,
            double processingTimeMs,
            int tokenDefinitionCount,
            int estimatedAutomatonStateCount) {

        if (input == null) {
            input = "";
        }

        int inputLength = input.length();
        int lineCount = countLines(input);

        int tokenCount = 0;
        int errorCount = 0;
        int traceStepCount = 0;
        if (result != null) {
            tokenCount     = result.getTokens().size();
            errorCount     = result.getErrors().size();
            traceStepCount = result.getTraceSteps().size();
        }

        double tokensPerSecond = 0.0;
        if (processingTimeMs > 0) {
            tokensPerSecond = tokenCount / (processingTimeMs / 1000.0);
        }

        return new PerformanceMetrics(
                inputLength,
                lineCount,
                tokenCount,
                errorCount,
                traceStepCount,
                processingTimeMs,
                tokenDefinitionCount,
                estimatedAutomatonStateCount,
                tokensPerSecond
        );
    }

    /**
     * Spočíta počet riadkov vstupu. Prázdny reťazec = 0 riadkov,
     * inak počet znakov nového riadku + 1.
     */
    private static int countLines(String input) {
        if (input == null || input.isEmpty()) {
            return 0;
        }
        int count = 1;
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '\n') {
                count++;
            }
        }
        return count;
    }
}
