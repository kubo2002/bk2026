package sk.jakubgubany.lexer.analysis;

/**
 * Jeden krok v stope lexikálnej analýzy.
 * Zaznamenáva, čo analyzátor práve robí — slúži na ladenie
 * a na vysvetlenie postupu analýzy v rámci bakalárskej práce.
 */
public class TraceStep {

    private final int currentIndex;
    private final int currentLine;
    private final int currentColumn;
    private final Character currentCharacter;
    private final String currentLexeme;
    private final Integer currentStateId;
    private final Integer nextStateId;
    private final String message;

    public TraceStep(int currentIndex,
                     int currentLine,
                     int currentColumn,
                     Character currentCharacter,
                     String currentLexeme,
                     Integer currentStateId,
                     Integer nextStateId,
                     String message) {
        this.currentIndex = currentIndex;
        this.currentLine = currentLine;
        this.currentColumn = currentColumn;
        this.currentCharacter = currentCharacter;
        this.currentLexeme = currentLexeme;
        this.currentStateId = currentStateId;
        this.nextStateId = nextStateId;
        this.message = message;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getCurrentLine() {
        return currentLine;
    }

    public int getCurrentColumn() {
        return currentColumn;
    }

    public Character getCurrentCharacter() {
        return currentCharacter;
    }

    public String getCurrentLexeme() {
        return currentLexeme;
    }

    public Integer getCurrentStateId() {
        return currentStateId;
    }

    public Integer getNextStateId() {
        return nextStateId;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        String ch = (currentCharacter == null) ? "EOF" : "'" + currentCharacter + "'";
        return "TraceStep{" +
                "index=" + currentIndex +
                ", line=" + currentLine +
                ", column=" + currentColumn +
                ", char=" + ch +
                ", lexeme='" + (currentLexeme == null ? "" : currentLexeme) + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
