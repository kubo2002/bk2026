package sk.jakubgubany.lexer.config;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Pozorovateľný model jedného tokenového pravidla v konfiguračnom okne.
 * Používa JavaFX vlastnosti, aby sa zmeny v TableView okamžite premietli
 * do dát bez potreby manuálneho obnovovania.
 *
 * <ul>
 *   <li>{@code name}     – názov kategórie tokenu, napr. IDENTIFIER</li>
 *   <li>{@code regex}    – regulárny výraz kompatibilný s Java</li>
 *   <li>{@code priority} – nižšie číslo = pravidlo sa vyskúša skôr</li>
 *   <li>{@code skip}     – token sa rozpozná, ale nevloží do výsledku</li>
 *   <li>{@code note}     – voľná poznámka; analyzátor ju ignoruje</li>
 * </ul>
 */
public class TokenDefinitionConfig {

    private final StringProperty  name;
    private final StringProperty  regex;
    private final IntegerProperty priority;
    private final BooleanProperty skip;
    private final StringProperty  note;

    public TokenDefinitionConfig(String name, String regex, int priority, boolean skip, String note) {
        this.name     = new SimpleStringProperty(name);
        this.regex    = new SimpleStringProperty(regex);
        this.priority = new SimpleIntegerProperty(priority);
        this.skip     = new SimpleBooleanProperty(skip);
        this.note     = new SimpleStringProperty(note);
    }

    /** Vytvorí prázdne pravidlo, ktoré používateľ môže vyplniť. */
    public TokenDefinitionConfig() {
        this("NEW_TOKEN", "", 0, false, "");
    }

    // -------------------------------------------------------------------------
    // Prístupové metódy k vlastnostiam — vyžaduje ich TableView na väzbu buniek
    // -------------------------------------------------------------------------

    public StringProperty  nameProperty()     { return name; }
    public StringProperty  regexProperty()    { return regex; }
    public IntegerProperty priorityProperty() { return priority; }
    public BooleanProperty skipProperty()     { return skip; }
    public StringProperty  noteProperty()     { return note; }

    // -------------------------------------------------------------------------
    // Štandardné gettery a settery
    // -------------------------------------------------------------------------

    public String  getName()           { return name.get(); }
    public void    setName(String v)   { name.set(v); }

    public String  getRegex()          { return regex.get(); }
    public void    setRegex(String v)  { regex.set(v); }

    public int     getPriority()       { return priority.get(); }
    public void    setPriority(int v)  { priority.set(v); }

    public boolean isSkip()            { return skip.get(); }
    public void    setSkip(boolean v)  { skip.set(v); }

    public String  getNote()           { return note.get(); }
    public void    setNote(String v)   { note.set(v); }

    @Override
    public String toString() {
        return "TokenDefinitionConfig{name='" + getName()
                + "', regex='" + getRegex()
                + "', priority=" + getPriority()
                + ", skip=" + isSkip() + '}';
    }
}
