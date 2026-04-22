package sk.jakubgubany.lexer.automata;

import java.util.ArrayList;
import java.util.List;

/**
 * Nedeterministický konečný automat (NFA) zostavený z regulárnych výrazov.
 * Uchováva:
 * - počiatočný stav,
 * - všetky stavy automatu,
 * - všetky akceptačné (koncové) stavy.
 */
public class NFA {

    private NFAState startState;
    private final List<NFAState> states;
    private final List<NFAState> acceptingStates;

    public NFA() {
        this.states = new ArrayList<>();
        this.acceptingStates = new ArrayList<>();
    }

    public NFAState getStartState() {
        return startState;
    }

    public void setStartState(NFAState startState) {
        this.startState = startState;
    }

    public List<NFAState> getStates() {
        return states;
    }

    public List<NFAState> getAcceptingStates() {
        return acceptingStates;
    }

    public void addState(NFAState state) {
        states.add(state);
        if (state.isAccepting() && !acceptingStates.contains(state)) {
            acceptingStates.add(state);
        }
    }

    public void addAcceptingState(NFAState state) {
        if (!acceptingStates.contains(state)) {
            acceptingStates.add(state);
        }
    }

    /**
     * Vypíše jednoduchú textovú reprezentáciu NFA do konzoly.
     * Používam to na ladenie a na overenie, že automat vyzerá správne.
     */
    public void print() {
        System.out.println("Start state: " + (startState != null ? startState.getId() : "none"));

        System.out.print("Accepting states: ");
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

        System.out.println("Transitions:");
        for (NFAState state : states) {
            for (NFATransition transition : state.getTransitions()) {
                Character symbol = transition.getSymbol();
                String label = (symbol == null) ? "ε" : symbol.toString(); // null = epsilon prechod
                System.out.println(
                        "State " + transition.getFromState().getId() +
                                " --" + label + "--> State " + transition.getToState().getId()
                );
            }
        }
    }
}


