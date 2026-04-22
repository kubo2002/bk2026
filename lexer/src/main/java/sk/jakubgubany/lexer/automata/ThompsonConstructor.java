package sk.jakubgubany.lexer.automata;

import sk.jakubgubany.lexer.regex.RegexToken;
import sk.jakubgubany.lexer.regex.RegexTokenType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Zostaví NFA z regulárneho výrazu vyjadreného v postfixovej forme pomocou Thompsonovej konštrukcie.
 * Implementácia je zámerne jednoduchá, aby sa dala ľahko vysvetliť na obhajobe.
 */
public class ThompsonConstructor {

    private int nextStateId = 0;

    /**
     * Zostaví NFA zo zoznamu tokenov regulárneho výrazu v postfixovom poradí.
     *
     * @param postfixTokens tokeny v postfixovej forme
     * @return hotový NFA, alebo prázdny NFA ak je vstup neplatný
     */
    public NFA build(List<RegexToken> postfixTokens) {
        NFA nfa = new NFA();
        if (postfixTokens == null || postfixTokens.isEmpty()) {
            return nfa;
        }

        Deque<NFAFragment> stack = new ArrayDeque<>();

        for (RegexToken token : postfixTokens) {
            switch (token.getType()) {
                case CHARACTER:
                    stack.push(buildCharacterFragment(token.getValue().charAt(0), nfa));
                    break;
                case CONCAT:
                    if (stack.size() < 2) {
                        return nfa;
                    }
                    NFAFragment rightConcat = stack.pop();
                    NFAFragment leftConcat = stack.pop();
                    stack.push(buildConcatFragment(leftConcat, rightConcat, nfa));
                    break;
                case ALTERNATION:
                    if (stack.size() < 2) {
                        return nfa;
                    }
                    NFAFragment rightAlt = stack.pop();
                    NFAFragment leftAlt = stack.pop();
                    stack.push(buildAlternationFragment(leftAlt, rightAlt, nfa));
                    break;
                case KLEENE_STAR:
                    if (stack.isEmpty()) {
                        return nfa;
                    }
                    stack.push(buildStarFragment(stack.pop(), nfa));
                    break;
                case PLUS:
                    if (stack.isEmpty()) {
                        return nfa;
                    }
                    stack.push(buildPlusFragment(stack.pop(), nfa));
                    break;
                case OPTIONAL:
                    if (stack.isEmpty()) {
                        return nfa;
                    }
                    stack.push(buildOptionalFragment(stack.pop(), nfa));
                    break;
                default:
                    // Nepodporované typy tokenov jednoducho preskočíme.
                    break;
            }
        }

        if (stack.isEmpty()) {
            return nfa;
        }

        NFAFragment result = stack.pop();

        // Označíme akceptačný stav a zozbierame všetky stavy do NFA.
        result.getEndState().setAccepting(true);

        List<NFAState> allStates = collectStates(result.getStartState());
        for (NFAState state : allStates) {
            nfa.addState(state);
            if (state.isAccepting()) {
                nfa.addAcceptingState(state);
            }
        }

        nfa.setStartState(result.getStartState());

        return nfa;
    }

    /**
     * Zozbiera všetky NFA stavy dosiahnuteľné z daného počiatočného stavu.
     * Namiesto rekurzie používam jednoduchú slučku: udržiavam zoznam stavov
     * na návštevu a zoznam už navštívených stavov, aby som sa nevracal
     * do tých istých stavov donekonečna (automat môže mať cykly, napr. zo hviezdičky).
     */
    private List<NFAState> collectStates(NFAState startState) {
        List<NFAState> visited = new ArrayList<>();
        List<NFAState> toVisit = new ArrayList<>();
        toVisit.add(startState);

        while (!toVisit.isEmpty()) {
            // Vezmeme ďalší stav zo zoznamu.
            NFAState current = toVisit.remove(toVisit.size() - 1);

            // Ak sme tento stav už navštívili, preskočíme ho — inak by sme sa zacyklili.
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);

            // Naplánujeme návštevu všetkých susedných stavov.
            for (NFATransition transition : current.getTransitions()) {
                toVisit.add(transition.getToState());
            }
        }

        return visited;
    }

    private NFAState newState() {
        return new NFAState(nextStateId++);
    }

    private NFAFragment buildCharacterFragment(char c, NFA nfa) {
        NFAState start = newState();
        NFAState end = newState();
        start.addTransition(end, c);
        return new NFAFragment(start, end);
    }

    private NFAFragment buildConcatFragment(NFAFragment left, NFAFragment right, NFA nfa) {
        // Koniec ľavého fragmentu prepojíme epsilon prechodom s začiatkom pravého.
        left.getEndState().addTransition(right.getStartState(), null);
        return new NFAFragment(left.getStartState(), right.getEndState());
    }

    private NFAFragment buildAlternationFragment(NFAFragment left, NFAFragment right, NFA nfa) {
        NFAState start = newState();
        NFAState end = newState();

        // Epsilon z nového startu do začiatkov oboch fragmentov.
        start.addTransition(left.getStartState(), null);
        start.addTransition(right.getStartState(), null);

        // Epsilon z koncov oboch fragmentov do nového konca.
        left.getEndState().addTransition(end, null);
        right.getEndState().addTransition(end, null);

        return new NFAFragment(start, end);
    }

    private NFAFragment buildStarFragment(NFAFragment fragment, NFA nfa) {
        NFAState start = newState();
        NFAState end = newState();

        // Z nového startu do starého startu (vstup do fragmentu) a rovno do konca (preskočenie).
        start.addTransition(fragment.getStartState(), null);
        start.addTransition(end, null);

        // Z konca fragmentu späť na začiatok (opakovanie) alebo do nového konca (ukončenie).
        fragment.getEndState().addTransition(fragment.getStartState(), null);
        fragment.getEndState().addTransition(end, null);

        return new NFAFragment(start, end);
    }

    private NFAFragment buildPlusFragment(NFAFragment fragment, NFA nfa) {
        // Jedno alebo viac: ako hviezdička, ale musíme prejsť cez fragment aspoň raz.
        NFAState start = newState();
        NFAState end = newState();

        // Najprv musíme vojsť do fragmentu.
        start.addTransition(fragment.getStartState(), null);

        // Potom sa môžeme opakovať alebo skončiť — rovnako ako pri hviezdičke.
        fragment.getEndState().addTransition(fragment.getStartState(), null);
        fragment.getEndState().addTransition(end, null);

        return new NFAFragment(start, end);
    }

    private NFAFragment buildOptionalFragment(NFAFragment fragment, NFA nfa) {
        NFAState start = newState();
        NFAState end = newState();

        // Buď fragment preskočíme, alebo ním prejdeme práve raz.
        start.addTransition(fragment.getStartState(), null);
        start.addTransition(end, null);
        fragment.getEndState().addTransition(end, null);

        return new NFAFragment(start, end);
    }
}

