package sk.jakubgubany.lexer.engine;

import sk.jakubgubany.lexer.analysis.TraceStep;
import sk.jakubgubany.lexer.token.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Výsledok jedného behu lexikálnej analýzy nad vstupným reťazcom.
 * Obsahuje:
 * - rozpoznané tokeny,
 * - zistené lexikálne chyby,
 * - kroky stopy analýzy na ladenie a vysvetlenie.
 */
public class LexerResult {

    private final List<Token> tokens;
    private final List<LexerError> errors;
    private final List<TraceStep> traceSteps;

    public LexerResult(List<Token> tokens, List<LexerError> errors, List<TraceStep> traceSteps) {
        this.tokens = new ArrayList<>();
        if (tokens != null) {
            this.tokens.addAll(tokens);
        }

        this.errors = new ArrayList<>();
        if (errors != null) {
            this.errors.addAll(errors);
        }

        this.traceSteps = new ArrayList<>();
        if (traceSteps != null) {
            this.traceSteps.addAll(traceSteps);
        }
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public List<LexerError> getErrors() {
        return errors;
    }

    public List<TraceStep> getTraceSteps() {
        return traceSteps;
    }
}

