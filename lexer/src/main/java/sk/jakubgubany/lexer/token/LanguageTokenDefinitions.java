package sk.jakubgubany.lexer.token;

import sk.jakubgubany.lexer.regex.RegexPattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Vstavané definície tokenov pre jednoduchý imperatívny jazyk.
 * Konvencia názvov: každý token s názvom "IDENTIFIER", ktorého lexém
 * je v množine kľúčových slov, bude analyzátorom automaticky prevedený na "KEYWORD".
 */
public final class LanguageTokenDefinitions {

    private static final Set<String> KEYWORDS = Set.of(
            "int", "double", "if", "else", "while", "return"
    );

    private LanguageTokenDefinitions() {}

    public static Set<String> getKeywords() {
        return KEYWORDS;
    }

    /**
     * Vráti nemenný zoznam predvolených definícií tokenov zoradených podľa priority
     * — konkrétnejšie pravidlá sú pred všeobecnými.
     */
    public static List<TokenDefinition> getDefaultTokenDefinitions() {
        List<TokenDefinition> defs = new ArrayList<>();

        // Kľúčové slová — musia byť pred IDENTIFIER, aby princíp najdlhšej zhody fungoval správne.
        defs.add(new TokenDefinition("KEYWORD",
                new RegexPattern("(int|double|if|else|while|return)"), false));

        // Identifikátory
        defs.add(new TokenDefinition("IDENTIFIER",
                new RegexPattern("[\\p{L}_][\\p{L}\\p{N}_]*"), false));

        // Literály — DOUBLE musí byť pred INTEGER, aby 3.14 nebolo rozdelené na 3 a .14
        defs.add(new TokenDefinition("DOUBLE_LITERAL",
                new RegexPattern("[0-9]+\\.[0-9]+"), false));
        defs.add(new TokenDefinition("INTEGER_LITERAL",
                new RegexPattern("[0-9]+"), false));

        // Dvojznakové operátory musia byť pred jednoznakovými (princíp najdlhšej zhody)
        defs.add(new TokenDefinition("EQUALS",          new RegexPattern("=="),  false));
        defs.add(new TokenDefinition("NOT_EQUALS",      new RegexPattern("!="),  false));
        defs.add(new TokenDefinition("LESS_OR_EQUAL",   new RegexPattern("<="),  false));
        defs.add(new TokenDefinition("GREATER_OR_EQUAL",new RegexPattern(">="),  false));

        // Jednoznakové operátory
        defs.add(new TokenDefinition("PLUS",         new RegexPattern("\\+"), false));
        defs.add(new TokenDefinition("MINUS",        new RegexPattern("-"),   false));
        defs.add(new TokenDefinition("STAR",         new RegexPattern("\\*"), false));
        defs.add(new TokenDefinition("SLASH",        new RegexPattern("/"),   false));
        defs.add(new TokenDefinition("ASSIGN",       new RegexPattern("="),   false));
        defs.add(new TokenDefinition("LESS_THAN",    new RegexPattern("<"),   false));
        defs.add(new TokenDefinition("GREATER_THAN", new RegexPattern(">"),   false));

        // Oddeľovače
        defs.add(new TokenDefinition("LEFT_PAREN",   new RegexPattern("\\("), false));
        defs.add(new TokenDefinition("RIGHT_PAREN",  new RegexPattern("\\)"), false));
        defs.add(new TokenDefinition("LEFT_BRACE",   new RegexPattern("\\{"), false));
        defs.add(new TokenDefinition("RIGHT_BRACE",  new RegexPattern("\\}"), false));
        defs.add(new TokenDefinition("SEMICOLON",    new RegexPattern(";"),   false));
        defs.add(new TokenDefinition("COMMA",        new RegexPattern(","),   false));

        // Biele znaky — preskočiť (spotrebujú sa, ale nevypisujú)
        defs.add(new TokenDefinition("WHITESPACE",
                new RegexPattern("[ \\t\\r\\n]+"), true));

        return Collections.unmodifiableList(defs);
    }
}
