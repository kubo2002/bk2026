package sk.jakubgubany.lexer.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sk.jakubgubany.lexer.config.TokenDefinitionConfig;
import sk.jakubgubany.lexer.regex.RegexPattern;
import sk.jakubgubany.lexer.token.TokenDefinition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Kontrolér pre okno konfigurácie tokenových pravidiel.
 * Zodpovednosti:
 * <ul>
 *   <li>Uchováva pozorovateľný zoznam riadkov TokenDefinitionConfig.</li>
 *   <li>Overuje správnosť regulárnych výrazov pred aplikovaním zmien.</li>
 *   <li>Prevedie konfiguračné riadky na TokenDefinition objekty pre analyzátor.</li>
 *   <li>Ukladá a načítava zoznam pravidiel ako JSON súbor (knižnica Gson).</li>
 * </ul>
 * Táto trieda sa vôbec nedotýka JavaFX scény — to je úlohou TokenRulesWindow.
 */
public class TokenRulesController {

    private final ObservableList<TokenDefinitionConfig> rules =
            FXCollections.observableArrayList();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // -------------------------------------------------------------------------
    // List management
    // -------------------------------------------------------------------------

    /** Pridá prázdne pravidlo na koniec zoznamu a vráti ho, aby naň mohol pohľad prescrollovať. */
    public TokenDefinitionConfig addRule() {
        TokenDefinitionConfig blank = new TokenDefinitionConfig();
        rules.add(blank);
        return blank;
    }

    /** Odstráni dané pravidlo. Ak je null, neurobí nič. */
    public void removeRule(TokenDefinitionConfig rule) {
        if (rule != null) {
            rules.remove(rule);
        }
    }

    /**
     * Pozorovateľný zoznam, na ktorý sa viaže TableView.
     * Referenciu na zoznam nesmieme vymeniť — treba meniť len jeho obsah.
     */
    public ObservableList<TokenDefinitionConfig> getRules() {
        return rules;
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    /**
     * Overí každé pravidlo v zozname.
     *
     * @return prvá nájdená chybová správa, alebo null ak sú všetky pravidlá v poriadku
     */
    public String validate() {
        for (TokenDefinitionConfig cfg : rules) {
            if (cfg.getName() == null || cfg.getName().isBlank()) {
                return "Token name must not be empty.";
            }
            if (cfg.getRegex() == null || cfg.getRegex().isBlank()) {
                return "Regex pattern for token '" + cfg.getName() + "' must not be empty.";
            }
            try {
                Pattern.compile(cfg.getRegex());
            } catch (PatternSyntaxException e) {
                return "Invalid regex pattern for token '" + cfg.getName()
                        + "':\n" + e.getDescription();
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Conversion to TokenDefinition
    // -------------------------------------------------------------------------

    /**
     * Prevedie aktuálne pravidlá na TokenDefinition objekty pre analyzátor.
     * Pravidlá sú zoradené podľa priority vzostupne — nižšie číslo znamená
     * vyššiu prioritu (pravidlo sa vyskúša skôr).
     * Pred volaním tejto metódy treba zavolať validate(), aby boli všetky regexy platné.
     *
     * @return zoradený zoznam definícií tokenov
     */
    public List<TokenDefinition> toTokenDefinitions() {
        // Skopírujeme zoznam, aby sme neupravili poradie v tabuľke.
        List<TokenDefinitionConfig> sortedRules = new ArrayList<>(rules);
        // Zoradíme podľa priority: nižšie číslo = vyššia priorita.
        sortedRules.sort((a, b) -> a.getPriority() - b.getPriority());

        List<TokenDefinition> result = new ArrayList<>();
        for (TokenDefinitionConfig cfg : sortedRules) {
            TokenDefinition def = new TokenDefinition(
                    cfg.getName(),
                    new RegexPattern(cfg.getRegex()),
                    cfg.isSkip()
            );
            result.add(def);
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // JSON persistence
    // -------------------------------------------------------------------------

    /**
     * Uloží aktuálny zoznam pravidiel do JSON súboru.
     *
     * @param file cieľový súbor (vytvorí sa alebo prepíše)
     * @throws IOException ak sa súbor nedá zapísať
     */
    public void saveToFile(File file) throws IOException {
        List<RuleDto> dtos = new ArrayList<>();
        for (TokenDefinitionConfig rule : rules) {
            dtos.add(RuleDto.from(rule));
        }
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(dtos, writer);
        }
    }

    /**
     * Nahradí aktuálny zoznam pravidiel pravidlami načítanými z JSON súboru.
     *
     * @param file zdrojový súbor
     * @throws IOException ak sa súbor nedá prečítať alebo JSON je neplatný
     */
    public void loadFromFile(File file) throws IOException {
        // Deserializujeme do poľa RuleDto[] — jednoduchšie ako generický TypeToken vzor.
        try (Reader reader = new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8)) {
            RuleDto[] dtosArray = gson.fromJson(reader, RuleDto[].class);
            rules.clear();
            if (dtosArray != null) {
                for (RuleDto dto : dtosArray) {
                    rules.add(dto.toConfig());
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // JSON DTO — čistý Java objekt kompatibilný s Gson (bez JavaFX vlastností)
    // -------------------------------------------------------------------------

    /**
     * Jednoduchý Java objekt používaný výlučne na JSON serializáciu.
     * Gson nedokáže serializovať JavaFX Property priamo,
     * preto konvertujeme do/z tohto DTO pri čítaní a zápise.
     */
    private static class RuleDto {
        String  name;
        String  regex;
        int     priority;
        boolean skip;
        String  note;

        static RuleDto from(TokenDefinitionConfig cfg) {
            RuleDto dto = new RuleDto();
            dto.name     = cfg.getName();
            dto.regex    = cfg.getRegex();
            dto.priority = cfg.getPriority();
            dto.skip     = cfg.isSkip();
            dto.note     = cfg.getNote();
            return dto;
        }

        TokenDefinitionConfig toConfig() {
            return new TokenDefinitionConfig(
                    name     != null ? name     : "",
                    regex    != null ? regex    : "",
                    priority,
                    skip,
                    note     != null ? note     : ""
            );
        }
    }
}
