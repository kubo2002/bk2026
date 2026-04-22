package sk.jakubgubany.lexer.gui;

/**
 * Jeden riadok v Tabuľke pozícií — jeden znak zo zdrojového kódu
 * spolu s jeho pozíciou (číslo riadku a stĺpca, číslované od 1).
 */
public class PositionEntry {

    private final int line;
    private final int column;
    private final String character;

    public PositionEntry(int line, int column, String character) {
        this.line = line;
        this.column = column;
        this.character = character;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getCharacter() {
        return character;
    }
}
