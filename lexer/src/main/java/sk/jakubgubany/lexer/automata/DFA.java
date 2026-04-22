package sk.jakubgubany.lexer.automata;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministický konečný automat (DFA) zostavený z NFA pomocou konštrukcie podmnožín.
 */
public class DFA {

    private DFAState startState;
    private final List<DFAState> states;
    private final List<DFAState> acceptingStates;

    public DFA() {
        this.states = new ArrayList<>();
        this.acceptingStates = new ArrayList<>();
    }

    public DFAState getStartState() {
        return startState;
    }

    public void setStartState(DFAState startState) {
        this.startState = startState;
    }

    public List<DFAState> getStates() {
        return states;
    }

    public List<DFAState> getAcceptingStates() {
        return acceptingStates;
    }

    public void addState(DFAState state) {
        states.add(state);
        if (state.isAccepting() && !acceptingStates.contains(state)) {
            acceptingStates.add(state);
        }
    }

    public void addAcceptingState(DFAState state) {
        if (!acceptingStates.contains(state)) {
            acceptingStates.add(state);
        }
    }

    /**
     * Vypíše jednoduchú textovú reprezentáciu DFA do konzoly — na ladenie.
     */
    public void print() {
        System.out.println("DFA Start state: " + (startState != null ? startState.getId() : "none"));

        System.out.print("DFA Accepting states: ");
        if (acceptingStates.isEmpty()) {
            System.out.println("(none)");
        } else {
            for (int i = 0; i < acceptingStates.size(); i++) {
                System.out.print(acceptingStates.get(i).getId());
                if (i < acceptingStates.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }

        System.out.println("DFA Transitions:");
        for (DFAState state : states) {
            StringBuilder nfaIds = new StringBuilder();
            boolean first = true;
            for (NFAState nfaState : state.getNfaStates()) {
                if (!first) {
                    nfaIds.append(",");
                }
                nfaIds.append(nfaState.getId());
                first = false;
            }

            for (var entry : state.getTransitions().entrySet()) {
                char symbol = entry.getKey();
                DFAState target = entry.getValue();
                System.out.println(
                        "State " + state.getId() + " [NFA: " + nfaIds +
                                "] --" + symbol + "--> State " + target.getId()
                );
            }
        }
    }
}


