package com.AI;

import java.util.List;
import java.util.Map;

public class ParseResponse {
    private String sentence;
    private List<TokenDTO> tokens;
    private boolean accepted;
    private List<String> parseLog;

    public ParseResponse(String sentence, List<TokenDTO> tokens, boolean accepted, List<String> parseLog) {
        this.sentence = sentence;
        this.tokens = tokens;
        this.accepted = accepted;
        this.parseLog = parseLog;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public List<TokenDTO> getTokens() {
        return tokens;
    }

    public void setTokens(List<TokenDTO> tokens) {
        this.tokens = tokens;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public List<String> getParseLog() {
        return parseLog;
    }

    public void setParseLog(List<String> parseLog) {
        this.parseLog = parseLog;
    }

    public static class TokenDTO {
        private String type;
        private String lexeme;

        public TokenDTO(String type, String lexeme) {
            this.type = type;
            this.lexeme = lexeme;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLexeme() {
            return lexeme;
        }

        public void setLexeme(String lexeme) {
            this.lexeme = lexeme;
        }
    }
}
