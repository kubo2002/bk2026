package sk.jakubgubany.lexer.engine;

import sk.jakubgubany.lexer.analysis.TraceStep;
import sk.jakubgubany.lexer.language.LanguageAlphabet;
import sk.jakubgubany.lexer.token.LanguageTokenDefinitions;
import sk.jakubgubany.lexer.token.Token;
import sk.jakubgubany.lexer.token.TokenDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Jadro lexikálneho analyzátora.
 * Typy tokenov sú dynamické — typ každého tokenu je rovnaký ako názov pravidla,
 * ktoré sa zhodovalo, takže pri pridávaní nových pravidiel sa nič iné meniť nemusí.
 * Jediné špeciálne spracovanie je povýšenie IDENTIFIER na KEYWORD: ak pravidlo
 * IDENTIFIER zodpovedá slovu zo zoznamu kľúčových slov, token dostane typ KEYWORD.
 */
public class Lexer {

    private final List<TokenDefinition> definitions;
    private final Set<String>           keywords;
    private final Map<TokenDefinition, Pattern> compiledPatterns;
    private LanguageAlphabet alphabet;

    public Lexer(List<TokenDefinition> definitions, Set<String> keywords) {
        this.definitions      = definitions;
        this.keywords         = keywords;
        this.compiledPatterns = new HashMap<>();
        this.alphabet         = LanguageAlphabet.defaultAlphabet();

        for (TokenDefinition def : definitions) {
            compiledPatterns.put(def, Pattern.compile(def.getPattern().getPattern()));
        }
    }

    /** Konštruktor, ktorý použije predvolené definície tokenov. */
    public Lexer() {
        this(LanguageTokenDefinitions.getDefaultTokenDefinitions(),
             LanguageTokenDefinitions.getKeywords());
    }

    public LanguageAlphabet       getAlphabet()           { return alphabet; }
    public int                    getTokenDefinitionCount(){ return definitions.size(); }
    public List<TokenDefinition>  getDefinitions()        { return Collections.unmodifiableList(definitions); }

    public void setAlphabet(LanguageAlphabet alphabet) {
        this.alphabet = alphabet != null ? alphabet : LanguageAlphabet.defaultAlphabet();
    }

    // -------------------------------------------------------------------------
    // Main scan
    // -------------------------------------------------------------------------

    /**
     * Prechádza celý vstupný reťazec a vrátí LexerResult
     * s tokenmi, chybami a stopou analýzy.
     */
    public LexerResult scan(String input) {
        List<Token>     tokens = new ArrayList<>();
        List<LexerError> errors = new ArrayList<>();
        List<TraceStep>  trace  = new ArrayList<>();

        if (input == null) {
            errors.add(new LexerError("Input is null", 1, 1, null));
            trace.add(new TraceStep(0, 1, 1, null, null, null, null,
                    "Lexical error: input is null"));
            return new LexerResult(tokens, errors, trace);
        }

        int index  = 0;
        int line   = 1;
        int column = 1;

        while (index < input.length()) {
            char currentChar = input.charAt(index);
            trace.add(new TraceStep(index, line, column, currentChar, null, null, null,
                    "Starting token recognition at this position"));

            if (!alphabet.isAllowed(currentChar)) {
                String fragment = String.valueOf(currentChar);
                String message  = "Character '" + currentChar
                        + "' is not allowed by the language alphabet";
                errors.add(new LexerError(message, line, column, fragment));
                trace.add(new TraceStep(index, line, column, currentChar,
                        fragment, null, null, "Lexical error: " + message));
                Position next = advancePosition(currentChar, line, column);
                index++;
                line   = next.line;
                column = next.column;
                continue;
            }

            Match bestMatch = findBestMatch(input, index, line, column, trace);

            if (bestMatch == null) {
                String fragment = String.valueOf(currentChar);
                String message  = "Unknown symbol '" + currentChar + "'";
                errors.add(new LexerError(message, line, column, fragment));
                trace.add(new TraceStep(index, line, column, currentChar,
                        fragment, null, null, "Lexical error: " + message));
                Position next = advancePosition(currentChar, line, column);
                index++;
                line   = next.line;
                column = next.column;
                continue;
            }

            String lexeme   = bestMatch.lexeme;
            String tokenType = bestMatch.definition.getName();

            // Ak IDENTIFIER zodpovedá kľúčovému slovu, povýšime ho na KEYWORD.
            if ("IDENTIFIER".equals(tokenType) && keywords.contains(lexeme)) {
                tokenType = "KEYWORD";
                trace.add(new TraceStep(index, line, column, currentChar, lexeme,
                        null, null, "Keyword recognized: '" + lexeme + "'"));
            }

            if (bestMatch.definition.isSkippable()) {
                trace.add(new TraceStep(index, line, column, currentChar, lexeme,
                        null, null, "Skipping token: " + tokenType));
            } else {
                Token token = new Token(tokenType, lexeme, line, column,
                        index, index + lexeme.length());
                tokens.add(token);
                trace.add(new TraceStep(index, line, column, currentChar, lexeme,
                        null, null,
                        "Final token selected: " + tokenType + " -> '" + lexeme + "'"));
            }

            for (int i = 0; i < lexeme.length(); i++) {
                Position next = advancePosition(lexeme.charAt(i), line, column);
                line   = next.line;
                column = next.column;
            }
            index += lexeme.length();
        }

        trace.add(new TraceStep(index, line, column, null, null, null, null,
                "Reached end of input"));
        return new LexerResult(tokens, errors, trace);
    }

    // -------------------------------------------------------------------------
    // Matching
    // -------------------------------------------------------------------------

    private Match findBestMatch(String input, int startIndex,
                                int line, int column, List<TraceStep> trace) {
        Match best = null;
        // Vezmeme len časť vstupu od aktuálnej pozície.
        // Tak každý vzor hľadáme od začiatku zostávajúceho textu.
        String remainingInput = input.substring(startIndex);

        for (TokenDefinition def : definitions) {
            Pattern p = compiledPatterns.get(def);
            if (p == null) continue;

            Matcher matcher = p.matcher(remainingInput);
            // lookingAt() overí, či vzor zodpovedá práve od začiatku reťazca.
            // Na rozdiel od matches() nevyžaduje zhodu celého reťazca
            // a na rozdiel od find() nehľadá zhodu kdekoľvek v reťazci.
            if (!matcher.lookingAt()) continue;

            String lexeme = matcher.group();
            trace.add(new TraceStep(startIndex, line, column,
                    input.charAt(startIndex), lexeme, null, null,
                    "Candidate match: " + def.getName() + " -> '" + lexeme + "'"));

            if (best == null || lexeme.length() > best.lexeme.length()) {
                best = new Match(def, lexeme);
            }
        }
        return best;
    }

    private Position advancePosition(char c, int line, int column) {
        return c == '\n' ? new Position(line + 1, 1)
                         : new Position(line, column + 1);
    }

    // -------------------------------------------------------------------------
    // Inner helpers
    // -------------------------------------------------------------------------

    private static class Match {
        final TokenDefinition definition;
        final String          lexeme;
        Match(TokenDefinition definition, String lexeme) {
            this.definition = definition;
            this.lexeme     = lexeme;
        }
    }

    private static class Position {
        final int line;
        final int column;
        Position(int line, int column) {
            this.line   = line;
            this.column = column;
        }
    }
}
