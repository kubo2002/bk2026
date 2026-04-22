package sk.jakubgubany.lexer.automata;

/**
 * Pomocný objekt používaný pri Thompsonovej konštrukcii.
 * Fragment je neúplný NFA s jedným počiatočným a jedným koncovým stavom.
 * Tieto fragmenty sa postupne skladajú dohromady, až vznikne výsledný NFA.
 */
public class NFAFragment {

    private final NFAState startState;
    private final NFAState endState;

    public NFAFragment(NFAState startState, NFAState endState) {
        this.startState = startState;
        this.endState = endState;
    }

    public NFAState getStartState() {
        return startState;
    }

    public NFAState getEndState() {
        return endState;
    }
}

