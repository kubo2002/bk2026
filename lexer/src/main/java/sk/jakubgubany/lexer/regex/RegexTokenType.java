package sk.jakubgubany.lexer.regex;

/**
 * Typy tokenov, ktoré sa môžu objaviť vo vnútri regulárneho výrazu.
 * Toto je základná stavebná jednotka, ktorá sa neskôr použije
 * na zostavenie NFA a DFA automatov.
 */
public enum RegexTokenType {

    /**
     * Normálny znak, ktorý sa hľadá doslova v texte.
     * Napríklad: 'a', 'b', '1', '[', ']'.
     */
    CHARACTER,

    /**
     * Zreťazenie dvoch častí regexu.
     * Napríklad v "ab" je medzi 'a' a 'b' implicitné zreťazenie.
     */
    CONCAT,

    /**
     * Operátor alternatívy '|' — buď ľavá alebo pravá strana.
     */
    ALTERNATION,

    /**
     * Kleeneho hviezdička '*' — nula alebo viac opakovaní.
     */
    KLEENE_STAR,

    /**
     * Operátor '+' — jedno alebo viac opakovaní.
     */
    PLUS,

    /**
     * Operátor '?' — znak je voliteľný, čiže nula alebo jeden výskyt.
     */
    OPTIONAL,

    /**
     * Otvárajúca zátvorkna '(' na zoskupenie časti výrazu.
     */
    GROUP_START,

    /**
     * Zatvárjúca zátvorkna ')' na zoskupenie časti výrazu.
     */
    GROUP_END
}

