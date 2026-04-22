package sk.jakubgubany.lexer.regex;

/**
 * Jeden token vo vnútri regulárneho výrazu.
 * Napríklad zo vzoru "a|b*" vzniknú tokeny pre znaky 'a' a 'b'
 * a pre operátory '|' a '*'.
 */
public class RegexToken {

    private final RegexTokenType type;

    /**
     * Hodnota tokenu — používa sa hlavne pri tokenoch typu CHARACTER,
     * kde nesie konkrétny znak, napríklad "a" alebo "+".
     * Pri operátorových tokenoch je táto hodnota null.
     */
    private final String value;

    /**
     * Vytvorí nový token regulárneho výrazu.
     *
     * @param type  typ tokenu
     * @param value hodnota; pre CHARACTER obsahuje znak, pre operátory môže byť null
     */
    public RegexToken(RegexTokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public RegexTokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (value == null) {
            return type.name();
        }
        return type.name() + "(" + value + ")";
    }
}

