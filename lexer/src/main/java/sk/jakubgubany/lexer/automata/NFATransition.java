package sk.jakubgubany.lexer.automata;

/**
 * Prechod medzi dvoma stavmi NFA.
 * Symbol môže byť:
 * - normálny znak, ktorý sa musí prečítať zo vstupu,
 * - null, čo znamená epsilon prechod (presun bez čítania znaku).
 */
public class NFATransition {

    private final NFAState fromState;
    private final NFAState toState;
    private final Character symbol;

    public NFATransition(NFAState fromState, NFAState toState, Character symbol) {
        this.fromState = fromState;
        this.toState = toState;
        this.symbol = symbol;
    }

    public NFAState getFromState() {
        return fromState;
    }

    public NFAState getToState() {
        return toState;
    }

    /**
     * Vráti symbol tohto prechodu.
     * Ak je null, ide o epsilon prechod.
     */
    public Character getSymbol() {
        return symbol;
    }
}

