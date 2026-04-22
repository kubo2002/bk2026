package sk.jakubgubany.lexer.token;

/**
 * Jeden token vytvorený lexikálnym analyzátorom.
 * Typ tokenu je reťazec prevzatý priamo z názvu pravidla, ktoré sa zhodovalo.
 * Vďaka tomu môžeme pridávať nové typy tokenov bez zmeny zdrojového kódu.
 *
 * <p>Príklady:
 * <ul>
 *   <li>Pravidlo {@code name="IDENTIFIER"} → {@code type="IDENTIFIER"}</li>
 *   <li>Pravidlo {@code name="PLAIN_VALUE"} → {@code type="PLAIN_VALUE"}</li>
 *   <li>Pravidlo {@code name="DATE_LITERAL"} → {@code type="DATE_LITERAL"}</li>
 * </ul>
 */
public class Token {

    /** Typ tokenu — názov pravidla, ktoré ho vytvorilo, napr. "IDENTIFIER" alebo "COMMA". */
    private final String type;

    /** Presný text zo zdrojového kódu, ktorý zodpovedá tomuto pravidlu. */
    private final String lexeme;

    /** Číslo riadku, kde token začína (číslované od 1). */
    private final int line;

    /** Číslo stĺpca, kde token začína (číslované od 1). */
    private final int column;

    /** Pozícia začiatku tokenu v pôvodnom vstupnom reťazci (indexovaná od 0). */
    private final int startIndex;

    /** Pozícia konca tokenu — exkluzívna (startIndex + lexeme.length()). */
    private final int endIndex;

    /**
     * Kompletný konštruktor.
     *
     * @param type       typ tokenu (názov pravidla)
     * @param lexeme     zhodujúci sa text
     * @param line       číslo riadku (od 1)
     * @param column     číslo stĺpca (od 1)
     * @param startIndex začiatok v reťazci (od 0)
     * @param endIndex   koniec v reťazci — exkluzívny (startIndex + lexeme.length())
     */
    public Token(String type, String lexeme, int line, int column,
                 int startIndex, int endIndex) {
        this.type       = type;
        this.lexeme     = lexeme;
        this.line       = line;
        this.column     = column;
        this.startIndex = startIndex;
        this.endIndex   = endIndex;
    }

    public String getType()       { return type; }
    public String getLexeme()     { return lexeme; }
    public int    getLine()       { return line; }
    public int    getColumn()     { return column; }
    public int    getStartIndex() { return startIndex; }
    public int    getEndIndex()   { return endIndex; }

    /**
     * Čitateľná textová reprezentácia vhodná na zobrazenie v GUI.
     * Príklad: {@code IDENTIFIER "counter" line 2, column 5}
     */
    @Override
    public String toString() {
        return type + " \"" + lexeme + "\" line " + line + ", column " + column;
    }
}
