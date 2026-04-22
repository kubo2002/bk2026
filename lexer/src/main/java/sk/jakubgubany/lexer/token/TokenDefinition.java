package sk.jakubgubany.lexer.token;

import sk.jakubgubany.lexer.regex.RegexPattern;

/**
 * Popisuje, ako analyzátor rozpoznáva jeden typ tokenu.
 * Pole name slúži ako typ tokenu — čokoľvek tam napíšeme, to sa objaví
 * v Token.getType(). Nie je tu žiadne mapovanie na enum, takže môžeme
 * použiť ľubovolný názov bez zmeny zdrojového kódu.
 */
public class TokenDefinition {

    /**
     * Názov typu tokenu, napríklad "IDENTIFIER". Tento reťazec sa skopíruje do Token.getType().
     */
    private final String name;

    /**
     * Regulárny výraz, ktorý definuje tvar tohto tokenu.
     */
    private final RegexPattern pattern;

    /**
     * Ak je true, token sa rozpozná, ale nevloží sa do výsledného zoznamu — typicky pre biele znaky.
     */
    private final boolean skippable;

    /**
     * Vytvorí novú definíciu tokenu.
     *
     * @param name      názov typu tokenu
     * @param pattern   regulárny výraz
     * @param skippable true ak má byť token len spotrebovaný, ale nevypisovaný
     */
    public TokenDefinition(String name, RegexPattern pattern, boolean skippable) {
        this.name      = name;
        this.pattern   = pattern;
        this.skippable = skippable;
    }

    public String       getName()       { return name; }
    public RegexPattern getPattern()    { return pattern; }
    public boolean      isSkippable()   { return skippable; }

    @Override
    public String toString() {
        return "TokenDefinition{name='" + name
                + "', pattern='" + pattern.getPattern()
                + "', skippable=" + skippable + '}';
    }
}
