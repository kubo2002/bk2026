package sk.jakubgubany.lexer.engine;

/**
 * Lexikálna chyba zistená počas analýzy vstupu.
 * Analyzátor pri chybe nespadne, ale zapíše ju sem s popisom a pozíciou,
 * a potom pokračuje ďalej.
 */
public class LexerError {

    private final String message;
    private final int line;
    private final int column;
    private final String fragment;

    public LexerError(String message, int line, int column, String fragment) {
        this.message = message;
        this.line = line;
        this.column = column;
        this.fragment = fragment;
    }

    public String getMessage() {
        return message;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    /**
     * Vráti neplatný znak alebo krátky neplatný fragment vstupu, ak je k dispozícii.
     */
    public String getFragment() {
        return fragment;
    }

    @Override
    public String toString() {
        return "LexerError{" +
                "message='" + message + '\'' +
                ", line=" + line +
                ", column=" + column +
                ", fragment='" + fragment + '\'' +
                '}';
    }
}

