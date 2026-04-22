package sk.jakubgubany.lexer.engine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import sk.jakubgubany.lexer.token.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Pomocná trieda na export výsledkov analyzátora do externých formátov.
 * Momentálne podporuje len JSON. Trieda nemá stav, všetky metódy sú statické.
 *
 * <h3>Príklad JSON výstupu</h3>
 * <pre>{@code
 * [
 *   {
 *     "type": "IDENTIFIER",
 *     "lexeme": "counter",
 *     "line": 2,
 *     "column": 5,
 *     "startIndex": 14,
 *     "endIndex": 21
 *   },
 *   {
 *     "type": "ASSIGN",
 *     "lexeme": "=",
 *     "line": 2,
 *     "column": 13,
 *     "startIndex": 22,
 *     "endIndex": 23
 *   }
 * ]
 * }</pre>
 */
public final class TokenExporter {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private TokenExporter() {}

    /**
     * Prevedie zoznam tokenov na formátovaný JSON reťazec.
     *
     * @param tokens tokeny na export; nesmie byť null
     * @return JSON reťazec
     */
    public static String exportToJson(List<Token> tokens) {
        List<TokenDto> dtos = new ArrayList<>();
        for (Token token : tokens) {
            dtos.add(TokenDto.from(token));
        }
        return GSON.toJson(dtos);
    }

    /**
     * Prevedie celý LexerResult na JSON — obsahuje tokeny, chyby a ich počty.
     *
     * @param result výsledok analýzy; nesmie byť null
     * @return JSON reťazec
     */
    public static String exportResultToJson(LexerResult result) {
        ResultDto dto = ResultDto.from(result);
        return GSON.toJson(dto);
    }

    // -------------------------------------------------------------------------
    // Jednoduché Java DTO objekty kompatibilné s knižnicou Gson (bez JavaFX vlastností)
    // -------------------------------------------------------------------------

    /** DTO pre jeden token. */
    private static class TokenDto {
        String type;
        String lexeme;
        int    line;
        int    column;
        int    startIndex;
        int    endIndex;

        static TokenDto from(Token t) {
            TokenDto dto  = new TokenDto();
            dto.type       = t.getType();
            dto.lexeme     = t.getLexeme();
            dto.line       = t.getLine();
            dto.column     = t.getColumn();
            dto.startIndex = t.getStartIndex();
            dto.endIndex   = t.getEndIndex();
            return dto;
        }
    }

    /** DTO pre jednu lexikálnu chybu. */
    private static class ErrorDto {
        String message;
        int    line;
        int    column;
        String fragment;

        static ErrorDto from(LexerError e) {
            ErrorDto dto  = new ErrorDto();
            dto.message   = e.getMessage();
            dto.line      = e.getLine();
            dto.column    = e.getColumn();
            dto.fragment  = e.getFragment();
            return dto;
        }
    }

    /** DTO pre celý výsledok analýzy. */
    private static class ResultDto {
        int              tokenCount;
        int              errorCount;
        List<TokenDto>   tokens;
        List<ErrorDto>   errors;

        static ResultDto from(LexerResult r) {
            ResultDto dto  = new ResultDto();
            dto.tokenCount = r.getTokens().size();
            dto.errorCount = r.getErrors().size();

            List<TokenDto> tokenDtos = new ArrayList<>();
            for (Token t : r.getTokens()) {
                tokenDtos.add(TokenDto.from(t));
            }
            dto.tokens = tokenDtos;

            List<ErrorDto> errorDtos = new ArrayList<>();
            for (LexerError e : r.getErrors()) {
                errorDtos.add(ErrorDto.from(e));
            }
            dto.errors = errorDtos;

            return dto;
        }
    }
}
