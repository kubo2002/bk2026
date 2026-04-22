package sk.jakubgubany.lexer.analysis;

import sk.jakubgubany.lexer.automata.NFA;
import sk.jakubgubany.lexer.automata.ThompsonConstructor;
import sk.jakubgubany.lexer.regex.RegexToPostfixConverter;
import sk.jakubgubany.lexer.regex.RegexToken;
import sk.jakubgubany.lexer.regex.RegexTokenizer;
import sk.jakubgubany.lexer.token.TokenDefinition;

import java.util.List;

/**
 * Odhadne celkový počet stavov automatov pre aktuálne definície tokenov.
 * Pre každú definíciu sa pokúsim zostaviť NFA z jej regexu pomocou Thompsonovej konštrukcie.
 * Ak sa regex podarí previesť, spočítam stavy NFA. Pre vzory, ktoré náš jednoduchý
 * tokenizér nepodporuje (napr. triedy znakov [0-9]), pridám malý náhradný odhad,
 * aby celkový výsledok mal zmysel.
 * Výsledok je len odhad, nie presný počet DFA stavov.
 */
public final class EstimatedAutomatonStateCounter {

    /** Náhradný počet stavov na definíciu, keď sa NFA nepodarí zostaviť. */
    private static final int FALLBACK_STATES_PER_DEFINITION = 2;

    private EstimatedAutomatonStateCounter() {
        // Pomocná trieda bez inštancií
    }

    /**
     * Vráti odhad celkového počtu stavov automatov pre dané definície.
     * Pre každú definíciu, ktorú sa podarí previesť, sčíta stavy NFA;
     * pre ostatné použije náhradnú hodnotu.
     *
     * @param definitions definície tokenov v analyzátore
     * @return odhadovaný celkový počet stavov (vždy >= 0)
     */
    public static int estimate(List<TokenDefinition> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            return 0;
        }

        RegexTokenizer tokenizer = new RegexTokenizer();
        RegexToPostfixConverter toPostfix = new RegexToPostfixConverter();
        ThompsonConstructor thompson = new ThompsonConstructor();

        int total = 0;
        for (TokenDefinition def : definitions) {
            if (def == null || def.getPattern() == null) {
                total += FALLBACK_STATES_PER_DEFINITION;
                continue;
            }
            String pattern = def.getPattern().getPattern();
            if (pattern == null || pattern.isEmpty()) {
                total += FALLBACK_STATES_PER_DEFINITION;
                continue;
            }

            try {
                List<RegexToken> tokens = tokenizer.tokenize(pattern);
                if (tokens.isEmpty()) {
                    total += FALLBACK_STATES_PER_DEFINITION;
                    continue;
                }
                List<RegexToken> postfix = toPostfix.toPostfix(tokens);
                NFA nfa = thompson.build(postfix);
                if (nfa != null && nfa.getStates() != null && !nfa.getStates().isEmpty()) {
                    total += nfa.getStates().size();
                } else {
                    total += FALLBACK_STATES_PER_DEFINITION;
                }
            } catch (Exception e) {
                // Vzor nie je v podporovanej podmnožine (napr. triedy znakov); použijeme náhradnú hodnotu.
                total += FALLBACK_STATES_PER_DEFINITION;
            }
        }
        return total;
    }
}
