package sk.jakubgubany.lexer;

import sk.jakubgubany.lexer.analysis.TraceStep;
import sk.jakubgubany.lexer.engine.Lexer;
import sk.jakubgubany.lexer.engine.LexerError;
import sk.jakubgubany.lexer.engine.LexerResult;
import sk.jakubgubany.lexer.token.Token;

import java.util.Arrays;
import java.util.List;

/**
 * Main application entry point for the lexical analyzer project.
 * <p>
 * this class demonstrates lexing real input strings into tokens,
 * including error reporting and trace output.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("====================================================");
        System.out.println();

        List<String> inputs = Arrays.asList(
                "int a = 5;",
                "double value = 12.5;",
                "if (a <= 10) {\n    return a;\n}",
                "int x = @;"
        );

        Lexer lexer = new Lexer();

        for (String input : inputs) {
            System.out.println("Input:");
            System.out.println(input);
            System.out.println();

            LexerResult result = lexer.scan(input);

            System.out.println("Tokens:");
            for (Token token : result.getTokens()) {
                System.out.println("  " + token);
            }

            System.out.println();
            System.out.println("Errors:");
            if (result.getErrors().isEmpty()) {
                System.out.println("  (none)");
            } else {
                for (LexerError error : result.getErrors()) {
                    System.out.println("  " + error);
                }
            }

            System.out.println();
            System.out.println("Trace (first 25 steps):");
            int limit = Math.min(25, result.getTraceSteps().size());
            for (int i = 0; i < limit; i++) {
                TraceStep step = result.getTraceSteps().get(i);
                System.out.println("  " + step);
            }
            if (result.getTraceSteps().size() > limit) {
                System.out.println("  ... (" + (result.getTraceSteps().size() - limit) + " more steps)");
            }
            System.out.println();
        }
    }
}
