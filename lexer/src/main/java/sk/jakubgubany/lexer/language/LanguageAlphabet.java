package sk.jakubgubany.lexer.language;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Definuje, ktoré znaky sú považované za platný vstup lexikálneho analyzátora.
 * Abeceda odpovedá na jedinú otázku: je tento znak vôbec povolený?
 * Nepriraduje znakom žiaden syntaktický význam — to robia tokenové pravidlá.
 * Napríklad '+' môže byť operátor v C, alebo časť iného konštruktu v inom jazyku;
 * abeceda len hovorí, či je '+' legálny vstup.
 *
 * <h3>Konfigurácia</h3>
 * <ul>
 *   <li>{@code allowLetters}        – Unicode písmená</li>
 *   <li>{@code allowDigits}         – Unicode číslice</li>
 *   <li>{@code allowWhitespace}     – medzery, tabulátory, nové riadky</li>
 *   <li>{@code additionalCharacters} – akékoľvek ďalšie znaky (operátory, oddeľovače...)</li>
 * </ul>
 */
public class LanguageAlphabet {

    private final boolean        allowLetters;
    private final boolean        allowDigits;
    private final boolean        allowWhitespace;
    /** Zoradená množina, aby sa znaky v UI zobrazovali vždy v rovnakom poradí. */
    private final Set<Character> additionalCharacters;

    public LanguageAlphabet(boolean allowLetters,
                            boolean allowDigits,
                            boolean allowWhitespace,
                            Set<Character> additionalCharacters) {
        this.allowLetters         = allowLetters;
        this.allowDigits          = allowDigits;
        this.allowWhitespace      = allowWhitespace;
        this.additionalCharacters = additionalCharacters == null
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(additionalCharacters);
    }

    // -------------------------------------------------------------------------
    // Core check
    // -------------------------------------------------------------------------

    /**
     * Vráti true ak znak c patrí do nakonfigurovanej abecedy.
     * Analyzátor to volá pred každým pokusom o zhodu tokenu.
     */
    public boolean isAllowed(char c) {
        if (additionalCharacters.contains(c))       return true;
        if (allowLetters    && Character.isLetter(c))     return true;
        if (allowDigits     && Character.isDigit(c))      return true;
        if (allowWhitespace && Character.isWhitespace(c)) return true;
        return false;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public boolean        isAllowLetters()        { return allowLetters; }
    public boolean        isAllowDigits()         { return allowDigits; }
    public boolean        isAllowWhitespace()     { return allowWhitespace; }
    /** Len na čítanie — ďalšie znaky v poradí vloženia. */
    public Set<Character> getAdditionalCharacters() {
        return Collections.unmodifiableSet(additionalCharacters);
    }

    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    /**
     * Krátke zhrnutie abecedy zobrazované v hlavnom okne analyzátora.
     */
    public String formatSummary() {
        StringBuilder sb = new StringBuilder("Alphabet: ");
        sb.append(allowLetters    ? "Letters \u2713  " : "Letters \u2717  ");
        sb.append(allowDigits     ? "Digits \u2713  "  : "Digits \u2717  ");
        sb.append(allowWhitespace ? "Whitespace \u2713" : "Whitespace \u2717");

        if (!additionalCharacters.isEmpty()) {
            StringBuilder chars = new StringBuilder();
            for (Character c : additionalCharacters) {
                chars.append(c);
            }
            if (chars.length() <= 24) {
                sb.append("  +[").append(chars).append("]");
            } else {
                sb.append("  +").append(additionalCharacters.size()).append(" extra");
            }
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Default configuration
    // -------------------------------------------------------------------------

    /**
     * Predvolená abeceda vhodná pre typický imperatívny jazyk.
     * Povolené sú písmená, číslice a biele znaky. Bežné operátorové
     * a oddeľovacie znaky sú pridané do additionalCharacters, aby
     * predvolené pravidlá tokenov fungovali hneď bez nutnej konfigurácie.
     */
    public static LanguageAlphabet defaultAlphabet() {
        // LinkedHashSet zachováva poradie vloženia — znaky sa v UI zobrazujú prehľadne.
        Set<Character> common = new LinkedHashSet<>();
        // Aritmetické, porovnávacie a logické operátory
        for (char c : new char[]{ '+', '-', '*', '/', '=', '<', '>', '!', '&', '|', '^', '~' }) {
            common.add(c);
        }
        // Oddeľovače
        for (char c : new char[]{ '(', ')', '{', '}', '[', ']', ',', ';', ':' }) {
            common.add(c);
        }
        // Bežná interpunkcia identifikátorov a literálov
        for (char c : new char[]{ '_', '.', '"', '\'' }) {
            common.add(c);
        }
        return new LanguageAlphabet(true, true, true, common);
    }
}
