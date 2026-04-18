package com.AI;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ParseController {

    @PostMapping("/parse")
    public ParseResponse parse(@RequestBody String sentence) {

        Lexer lexer = new Lexer();
        Parser parser = new Parser(false);

        List<Lexer.Token> tokens = lexer.tokenize(sentence);

        // convert all tokens to DTOs to avoid serialization issues
        List<ParseResponse.TokenDTO> tokenDTOs = new ArrayList<>();
        for (Lexer.Token token : tokens) {
            tokenDTOs.add(new ParseResponse.TokenDTO(
                    token.type.toString(),
                    token.lexeme
            ));
        }

        boolean accepted = parser.parse(tokens);

        List<String> parseLog = parser.getParseLog();

        return new ParseResponse(sentence, tokenDTOs, accepted, parseLog);
    }
}
