package sk.jakubgubany.lexer.automata;

import java.util.ArrayList;
import java.util.List;

/**
 * Jeden stav v nedeterministickom konečnom automate (NFA).
 * Každý stav má:
 * - jedinečné číselné ID,
 * - príznak, či je to akceptačný (koncový) stav,
 * - zoznam odchádzajúcich prechodov.
 */
public class NFAState {

    private final int id;
    private boolean accepting;
    private final List<NFATransition> transitions;

    public NFAState(int id) {
        this.id = id;
        this.accepting = false;
        this.transitions = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public boolean isAccepting() {
        return accepting;
    }

    public void setAccepting(boolean accepting) {
        this.accepting = accepting;
    }

    public List<NFATransition> getTransitions() {
        return transitions;
    }

    /**
     * Pridá nový odchádzajúci prechod z tohto stavu do iného stavu.
     *
     * @param toState cieľový stav
     * @param symbol  vstupný symbol; null znamená epsilon prechod (bez čítania vstupu)
     */
    public void addTransition(NFAState toState, Character symbol) {
        transitions.add(new NFATransition(this, toState, symbol));
    }
}


