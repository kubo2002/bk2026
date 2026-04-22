package sk.jakubgubany.lexer.regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Regulárny výraz použitý na definíciu tokenu.
 * Uchováva pôvodný reťazec regexu a po spracovaní aj
 * zoznam tokenov vytvorených z neho.
 */
public class RegexPattern {

    /**
     * Pôvodný reťazec regulárneho výrazu, tak ako ho zadal používateľ.
     */
    private final String pattern;

    /**
     * Tokeny vzniknuté rozložením vzoru. Vypĺňa ich trieda RegexTokenizer.
     */
    private List<RegexToken> tokens;

    public RegexPattern(String pattern) {
        this.pattern = pattern;
        this.tokens = new ArrayList<>();
    }

    /**
     * Vráti pôvodný reťazec regulárneho výrazu.
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Vráti tokeny vytvorené z tohto vzoru.
     * Zoznam je z vonka len na čítanie.
     */
    public List<RegexToken> getTokens() {
        return Collections.unmodifiableList(tokens);
    }

    /**
     * Nastaví tokeny vytvorené z tohto vzoru.
     * Volá to tokenizér po rozložení vzoru.
     */
    public void setTokens(List<RegexToken> tokens) {
        if (tokens == null) {
            this.tokens = new ArrayList<>();
        } else {
            this.tokens = new ArrayList<>(tokens);
        }
    }

    /**
     * Základná validácia vzoru — overí, že reťazec nie je prázdny
     * a že závorky sú vyvážené.
     *
     * @return true ak vzor vyzerá byť v poriadku
     */
    public boolean validate() {
        if (pattern == null || pattern.isEmpty()) {
            return false;
        }

        int balance = 0;
        for (char c : pattern.toCharArray()) {
            if (c == '(') {
                balance++;
            } else if (c == ')') {
                balance--;
                if (balance < 0) {
                    // Viac zatvárjúcich ako otvárajúcich závoriek.
                    return false;
                }
            }
        }

        // Závorky sú vyvážené, ak skončíme na nule.
        return balance == 0;
    }
}

