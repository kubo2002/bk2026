package sk.jakubgubany.lexer.automata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Jeden stav v deterministickom konečnom automate (DFA).
 * Každý DFA stav zodpovedá množine NFA stavov — to je základ konštrukcie podmnožín.
 */
public class DFAState {

    private final int id;
    private final Set<NFAState> nfaStates;
    private final Map<Character, DFAState> transitions;
    private boolean accepting;

    public DFAState(int id, Set<NFAState> nfaStates) {
        this.id = id;
        this.nfaStates = new HashSet<>(nfaStates);
        this.transitions = new HashMap<>();
    }

    public int getId() {
        return id;
    }

    public Set<NFAState> getNfaStates() {
        return nfaStates;
    }

    public Map<Character, DFAState> getTransitions() {
        return transitions;
    }

    public boolean isAccepting() {
        return accepting;
    }

    public void setAccepting(boolean accepting) {
        this.accepting = accepting;
    }

    public void addTransition(char symbol, DFAState toState) {
        transitions.put(symbol, toState);
    }
}


