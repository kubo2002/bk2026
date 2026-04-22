package sk.jakubgubany.lexer.regex;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Prevedie zoznam tokenov regulárneho výrazu z infixovej formy do postfixovej.
 * Postfix je jednoduchší na spracovanie pri Thompsonovej konštrukcii,
 * lebo operátory vždy nasledujú za svojimi operandmi.
 */
public class RegexToPostfixConverter {

    /**
     * Prevedie tokeny z infixového poradia do postfixového
     * pomocou jednoduchého algoritmu podobného shunting-yardu.
     *
     * @param tokens vstupné tokeny v infixovom poradí
     * @return tokeny v postfixovom poradí
     */
    public List<RegexToken> toPostfix(List<RegexToken> tokens) {
        List<RegexToken> output = new ArrayList<>();
        Deque<RegexToken> operatorStack = new ArrayDeque<>();

        if (tokens == null) {
            return output;
        }

        for (RegexToken token : tokens) {
            switch (token.getType()) {
                case CHARACTER:
                    // Znaky idú priamo do výstupu.
                    output.add(token);
                    break;
                case GROUP_START:
                    operatorStack.push(token);
                    break;
                case GROUP_END:
                    // Vysypeme operátory zo zásobníka, kým nenarazíme na '('.
                    while (!operatorStack.isEmpty()
                            && operatorStack.peek().getType() != RegexTokenType.GROUP_START) {
                        output.add(operatorStack.pop());
                    }
                    if (!operatorStack.isEmpty()
                            && operatorStack.peek().getType() == RegexTokenType.GROUP_START) {
                        operatorStack.pop(); // zahodíme '('
                    }
                    break;
                case KLEENE_STAR:
                case PLUS:
                case OPTIONAL:
                case CONCAT:
                case ALTERNATION:
                    // Pri operátoroch vysypeme zo zásobníka všetko s vyššou alebo rovnakou prioritou.
                    while (!operatorStack.isEmpty()
                            && isOperator(operatorStack.peek())
                            && precedence(operatorStack.peek()) >= precedence(token)) {
                        output.add(operatorStack.pop());
                    }
                    operatorStack.push(token);
                    break;
                default:
                    // Nepodporované typy jednoducho pridáme do výstupu.
                    output.add(token);
                    break;
            }
        }

        // Na konci vysypeme zostatok zásobníka do výstupu.
        while (!operatorStack.isEmpty()) {
            RegexToken top = operatorStack.pop();
            if (top.getType() != RegexTokenType.GROUP_START
                    && top.getType() != RegexTokenType.GROUP_END) {
                output.add(top);
            }
        }

        return output;
    }

    private boolean isOperator(RegexToken token) {
        RegexTokenType type = token.getType();
        return type == RegexTokenType.KLEENE_STAR
                || type == RegexTokenType.PLUS
                || type == RegexTokenType.OPTIONAL
                || type == RegexTokenType.CONCAT
                || type == RegexTokenType.ALTERNATION;
    }

    /**
     * Vráti prioritu operátora — vyššie číslo znamená vyššiu prioritu.
     */
    private int precedence(RegexToken token) {
        switch (token.getType()) {
            case KLEENE_STAR:
            case PLUS:
            case OPTIONAL:
                return 3;
            case CONCAT:
                return 2;
            case ALTERNATION:
                return 1;
            default:
                return 0;
        }
    }
}

