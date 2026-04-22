package sk.jakubgubany.lexer.automata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Zostaví DFA z NFA pomocou algoritmu konštrukcie podmnožín.
 * Základná myšlienka: každý DFA stav predstavuje množinu NFA stavov,
 * v ktorých môže NFA byť súčasne. Začíname od epsilon-uzáveru počiatočného stavu
 * a postupne pridávame nové stavy, kým žiadne nové nepribúdajú.
 */
public class SubsetConstruction {

    private int nextDfaId = 0;

    /**
     * Prevedie daný NFA na ekvivalentný DFA.
     *
     * @param nfa zdrojový NFA
     * @return zostavený DFA
     */
    public DFA buildDfa(NFA nfa) {
        DFA dfa = new DFA();
        if (nfa.getStartState() == null) {
            return dfa;
        }

        // Vypočítame epsilon-uzáver počiatočného stavu NFA.
        // To sú všetky NFA stavy, do ktorých sa dostaneme ešte pred čítaním akéhokoľvek vstupu.
        Set<NFAState> startSet = new HashSet<>();
        startSet.add(nfa.getStartState());
        Set<NFAState> startClosure = epsilonClosure(startSet);

        // Mapovanie z kľúča podmnožiny (napr. "0,2,4") na DFA stav, ktorý túto podmnožinu reprezentuje.
        Map<String, DFAState> subsetToDfaState = new HashMap<>();

        // Zoznam DFA stavov, ktoré ešte treba spracovať.
        List<DFAState> worklist = new ArrayList<>();

        // Množina stavov, ktoré boli kedy pridané do worlistu.
        // Bez toho by sme mohli ten istý stav pridávať donekonečna a dostali by sme nekonečnú slučku.
        Set<DFAState> alreadyQueued = new HashSet<>();

        DFAState startDfaState = createDfaState(startClosure, subsetToDfaState, dfa);
        dfa.setStartState(startDfaState);
        worklist.add(startDfaState);
        alreadyQueued.add(startDfaState);

        List<Character> alphabet = extractAlphabet(nfa);

        while (!worklist.isEmpty()) {
            // Take the first DFA state from the worklist.
            DFAState current = worklist.remove(0);

            for (char symbol : alphabet) {
                // Nájdeme všetky NFA stavy dosiahnuteľné prečítaním tohto symbolu.
                Set<NFAState> moveResult = move(current.getNfaStates(), symbol);
                if (moveResult.isEmpty()) {
                    continue;
                }

                // Rozšírime o epsilon prechody, aby sme mali úplnú množinu dosiahnuteľných stavov.
                Set<NFAState> closure = epsilonClosure(moveResult);
                if (closure.isEmpty()) {
                    continue;
                }

                // Získame alebo vytvoríme DFA stav pre túto podmnožinu.
                DFAState target = createDfaState(closure, subsetToDfaState, dfa);
                current.addTransition(symbol, target);

                // Do worlistu pridáme len stav, ktorý tam ešte nebol — inak by sme skončili v nekonečnej slučke.
                if (!alreadyQueued.contains(target)) {
                    worklist.add(target);
                    alreadyQueued.add(target);
                }
            }
        }

        return dfa;
    }

    private DFAState createDfaState(Set<NFAState> subset,
                                    Map<String, DFAState> subsetToDfaState,
                                    DFA dfa) {
        // Zoradený kľúč zabezpečuje, že rovnaká množina NFA stavov vždy dostane rovnaký DFA stav.
        String key = buildSubsetKey(subset);
        DFAState existing = subsetToDfaState.get(key);
        if (existing != null) {
            return existing;
        }

        DFAState state = new DFAState(nextDfaId++, subset);

        // DFA stav je akceptačný, ak aspoň jeden z jeho NFA stavov je akceptačný.
        for (NFAState nfaState : subset) {
            if (nfaState.isAccepting()) {
                state.setAccepting(true);
                dfa.addAcceptingState(state);
                break;
            }
        }

        subsetToDfaState.put(key, state);
        dfa.addState(state);
        return state;
    }

    /**
     * Vypočíta epsilon-uzáver množiny NFA stavov.
     * Epsilon-uzáver je množina všetkých stavov dosiahnuteľných
     * pomocou epsilon prechodov — teda prechodov, ktoré nečítajú žiadny znak.
     * Používam jednoduchú slučku: začnem s danými stavmi a stále pridávam
     * nové stavy cez epsilon prechody, kým sa už žiadne nové nenájdu.
     */
    public Set<NFAState> epsilonClosure(Set<NFAState> states) {
        Set<NFAState> closure = new HashSet<>(states);
        List<NFAState> toProcess = new ArrayList<>(states);

        while (!toProcess.isEmpty()) {
            // Berieme posledný stav zo zoznamu (poradie tu nehrá rolu).
            NFAState state = toProcess.remove(toProcess.size() - 1);
            for (NFATransition transition : state.getTransitions()) {
                // Symbol null znamená epsilon prechod.
                if (transition.getSymbol() == null) {
                    NFAState target = transition.getToState();
                    if (!closure.contains(target)) {
                        closure.add(target);
                        toProcess.add(target);
                    }
                }
            }
        }

        return closure;
    }

    /**
     * Vypočíta move(stavy, symbol): množinu všetkých NFA stavov dosiahnuteľných
     * z daných stavov prečítaním práve jedného znaku rovného symbol.
     */
    public Set<NFAState> move(Set<NFAState> states, char symbol) {
        Set<NFAState> result = new HashSet<>();
        for (NFAState state : states) {
            for (NFATransition transition : state.getTransitions()) {
                if (transition.getSymbol() != null && transition.getSymbol() == symbol) {
                    result.add(transition.getToState());
                }
            }
        }
        return result;
    }

    /**
     * Vráti zoznam všetkých rôznych vstupných symbolov, ktoré NFA používa.
     * Epsilon prechody (null symbol) sa nezahŕňajú.
     */
    public List<Character> extractAlphabet(NFA nfa) {
        Set<Character> symbols = new HashSet<>();
        for (NFAState state : nfa.getStates()) {
            for (NFATransition transition : state.getTransitions()) {
                if (transition.getSymbol() != null) {
                    symbols.add(transition.getSymbol());
                }
            }
        }
        return new ArrayList<>(symbols);
    }

    /**
     * Vytvorí textový kľúč, ktorý jednoznačne identifikuje množinu NFA stavov.
     * Zozbierame všetky ID stavov, zoradíme ich a spojíme čiarkami.
     * Zoradenie je nutné, pretože HashSet negarantuje poradie a bez toho
     * by rovnaká množina mohla dávať rôzne reťazce.
     * Príklad: stavy s ID {2, 0, 4} dajú kľúč "0,2,4".
     */
    private String buildSubsetKey(Set<NFAState> subset) {
        // Zozbierame všetky ID stavov.
        List<Integer> ids = new ArrayList<>();
        for (NFAState state : subset) {
            ids.add(state.getId());
        }

        // Zoradíme, aby kľúč bol vždy rovnaký bez ohľadu na poradie iterácie.
        ids.sort(Integer::compareTo);

        // Zostavíme kľúč, napr. "0,2,4".
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            key.append(ids.get(i));
            if (i < ids.size() - 1) {
                key.append(",");
            }
        }
        return key.toString();
    }
}
