package sk.jakubgubany.lexer.regex;

import java.util.ArrayList;
import java.util.List;

/**
 * Rozloží reťazec regulárneho výrazu na zoznam tokenov typu RegexToken.
 * Rozumie len základným prvkom:
 * - normálne znaky
 * - '|' (alternatíva)
 * - '*' (nula alebo viac)
 * - '+' (jedno alebo viac)
 * - '?' (voliteľné)
 * - '(' a ')' (skupiny)
 *
 * Nejde o úplný parser regulárnych výrazov — slúži len ako prvý krok
 * pred zostavením NFA a DFA.
 */
public class RegexTokenizer {

    /**
     * Prevedie reťazec regulárneho výrazu na zoznam jednoduchých tokenov.
     *
     * @param regex reťazec regulárneho výrazu
     * @return zoznam tokenov
     */
    public List<RegexToken> tokenize(String regex) {
        List<RegexToken> result = new ArrayList<>();
        if (regex == null || regex.isEmpty()) {
            return result;
        }

        // Prvý prechod: vytvoríme tokeny pre znaky a operátory.
        for (int i = 0; i < regex.length(); i++) {
            char c = regex.charAt(i);

            switch (c) {
                case '|':
                    result.add(new RegexToken(RegexTokenType.ALTERNATION, null));
                    break;
                case '*':
                    result.add(new RegexToken(RegexTokenType.KLEENE_STAR, null));
                    break;
                case '+':
                    result.add(new RegexToken(RegexTokenType.PLUS, null));
                    break;
                case '?':
                    result.add(new RegexToken(RegexTokenType.OPTIONAL, null));
                    break;
                case '(':
                    result.add(new RegexToken(RegexTokenType.GROUP_START, null));
                    break;
                case ')':
                    result.add(new RegexToken(RegexTokenType.GROUP_END, null));
                    break;
                default:
                    // Všetko ostatné berieme ako normálny znak, vrátane závoriek a znakov tried.
                    result.add(new RegexToken(RegexTokenType.CHARACTER, String.valueOf(c)));
                    break;
            }
        }

        // Druhý prechod: vložíme explicitné CONCAT tokeny tam, kde sú dve časti vedľa seba
        // a mali by byť zreťazené.
        List<RegexToken> withConcat = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            RegexToken current = result.get(i);
            withConcat.add(current);

            if (i + 1 < result.size()) {
                RegexToken next = result.get(i + 1);

                if (shouldInsertConcat(current, next)) {
                    withConcat.add(new RegexToken(RegexTokenType.CONCAT, null));
                }
            }
        }

        return withConcat;
    }

    /**
     * Pravidlo pre to, kedy vložiť CONCAT token medzi dva tokeny.
     */
    private boolean shouldInsertConcat(RegexToken left, RegexToken right) {
        RegexTokenType lt = left.getType();
        RegexTokenType rt = right.getType();

        boolean leftCanEnd = lt == RegexTokenType.CHARACTER
                || lt == RegexTokenType.GROUP_END
                || lt == RegexTokenType.KLEENE_STAR
                || lt == RegexTokenType.PLUS
                || lt == RegexTokenType.OPTIONAL;

        boolean rightCanStart = rt == RegexTokenType.CHARACTER
                || rt == RegexTokenType.GROUP_START;

        return leftCanEnd && rightCanStart;
    }
}

