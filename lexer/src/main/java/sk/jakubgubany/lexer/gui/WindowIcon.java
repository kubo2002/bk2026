package sk.jakubgubany.lexer.gui;

import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Pomocná trieda na nastavenie ikony aplikácie pre ľubovoľné okno (Stage).
 * Ikona sa načíta raz z classpath (/icon.png) a potom sa znovu používa.
 */
final class WindowIcon {

    private static Image icon;

    static {
        try (var stream = WindowIcon.class.getResourceAsStream("/icon.png")) {
            if (stream != null) {
                icon = new Image(stream);
            }
        } catch (Exception ignored) {}
    }

    private WindowIcon() {}

    /** Nastaví ikonu aplikácie na dané okno. Ak ikona chýba, neurobí nič. */
    static void apply(Stage stage) {
        if (icon != null) {
            stage.getIcons().add(icon);
        }
    }
}
