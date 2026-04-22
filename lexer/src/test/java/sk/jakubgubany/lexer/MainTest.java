package sk.jakubgubany.lexer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Basic smoke test to ensure that the project compiles and the main method can be invoked.
 */
class MainTest {

    @Test
    void mainRunsWithoutErrors() {
        assertDoesNotThrow(() -> Main.main(new String[0]));
    }
}

