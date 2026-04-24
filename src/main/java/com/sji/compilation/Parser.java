package com.sji.compilation;
import java.util.*;

/**
 * Recursive Descent Parser for Yaounde informal speech.
 * Authors: Abena, Essindi and Yimgaing
 * Implements an LL(1) parser to validate sentence structure against defined grammar rules.
 */
public class Parser {

    private List<Lexer.Token> tokens;
    private int pos;
    private List<String> parseLog;
    private boolean verbose;

    public Parser(boolean verbose) {
        this.verbose = verbose;
        this.parseLog = new ArrayList<>();
    }

    public boolean parse(List<Lexer.Token> tokenList) {
        this.tokens = tokenList;
        this.pos = 0;
        this.parseLog = new ArrayList<>();

        List<Lexer.Token> filtered = new ArrayList<>();
        for (Lexer.Token t : tokenList) {
            if (t.type != Lexer.TokenType.PUNCTUATION) {
                filtered.add(t);
            }
        }
        this.tokens = filtered;

        if (tokens.isEmpty()) {
            log("REJECT: empty input");
            return false;
        }

        try {
            parseS();
            if (pos == tokens.size()) {
                log("ACCEPT: all tokens consumed successfully");
                return true;
            } else {
                log("REJECT: unconsumed tokens at position " + pos);
                return false;
            }
        } catch (ParseException e) {
            log("REJECT: " + e.getMessage());
            return false;
        }
    }

    private void parseS() throws ParseException {
        if (isType(Lexer.TokenType.INTERJECTION) || isLexeme(\"abeg\") || isLexeme(\"ekiee\")) {
            consume(Lexer.TokenType.INTERJECTION);
            if (pos < tokens.size()) parseS();
        } else if (isType(Lexer.TokenType.SLANG)) {
            consume(Lexer.TokenType.SLANG);
            if (pos < tokens.size()) parseS();
        } else if (isType(Lexer.TokenType.VERB)) {
            parseVP();
        } else if (inFIRST_NP()) {
            parseNP();
            if (pos < tokens.size() && isType(Lexer.TokenType.VERB)) {
                parseVP();
            }
        } else {
            throw new ParseException("Expected sentence start, got: " + current());
        }
    }

    private void parseNP() throws ParseException {
        if (isType(Lexer.TokenType.DETERMINER)) {
            consume(Lexer.TokenType.DETERMINER);
            if (isType(Lexer.TokenType.NOUN) || isType(Lexer.TokenType.ADJECTIVE)) {
                consume(current().type);
            }
            parseNP_prime();
        } else if (isType(Lexer.TokenType.NOUN)) {
            consume(Lexer.TokenType.NOUN);
            parseNP_prime();
        } else if (isType(Lexer.TokenType.PRONOUN)) {
            consume(Lexer.TokenType.PRONOUN);
            parseNP_prime();
        } else if (isType(Lexer.TokenType.CODE_MIX)) {
            consume(Lexer.TokenType.CODE_MIX);
        } else {
            throw new ParseException("Expected noun phrase");
        }
    }

    private void parseNP_prime() throws ParseException {
        if (pos >= tokens.size() || inFOLLOW_NP()) return;
        if (isType(Lexer.TokenType.NOUN)) {
            consume(Lexer.TokenType.NOUN);
            parseNP_prime();
        }
    }

    private void parseVP() throws ParseException {
        if (isType(Lexer.TokenType.SLANG)) {
            consume(Lexer.TokenType.SLANG);
            parseVP();
        } else if (isType(Lexer.TokenType.VERB)) {
            consume(Lexer.TokenType.VERB);
            parseVP_prime();
        } else {
            throw new ParseException("Expected verb");
        }
    }

    private void parseVP_prime() throws ParseException {
        if (pos >= tokens.size()) return;
        if (isType(Lexer.TokenType.PREPOSITION)) {
            parsePP();
            parseVP_prime();
        } else if (isType(Lexer.TokenType.ADJECTIVE)) {
            consume(Lexer.TokenType.ADJECTIVE);
            parseVP_prime();
        } else if (isType(Lexer.TokenType.SLANG)) {
            consume(Lexer.TokenType.SLANG);
            parseVP_prime();
        } else if (isType(Lexer.TokenType.INTERJECTION)) {
            consume(Lexer.TokenType.INTERJECTION);
            parseVP_prime();
        } else if (isType(Lexer.TokenType.CONJUNCTION)) {
            consume(Lexer.TokenType.CONJUNCTION);
            if (pos < tokens.size()) parseS();
        } else if (isType(Lexer.TokenType.NUMBER)) {
            consume(Lexer.TokenType.NUMBER);
            parseVP_prime();
        } else if (inFIRST_NP()) {
            parseNP();
            parseVP_prime();
        } else if (isType(Lexer.TokenType.VERB)) {
            consume(Lexer.TokenType.VERB);
            parseVP_prime();
        }
    }

    private void parsePP() throws ParseException {
        consume(Lexer.TokenType.PREPOSITION);
        if (pos < tokens.size()) {
            if (inFIRST_NP()) parseNP();
            else if (isType(Lexer.TokenType.ADJECTIVE)) consume(Lexer.TokenType.ADJECTIVE);
            else if (isType(Lexer.TokenType.NUMBER)) consume(Lexer.TokenType.NUMBER);
        }
    }

    private Lexer.Token current() { return tokens.get(pos); }
    private boolean isType(Lexer.TokenType type) { return pos < tokens.size() && tokens.get(pos).type == type; }
    private boolean isLexeme(String lex) { return pos < tokens.size() && tokens.get(pos).lexeme.equalsIgnoreCase(lex); }

    private void consume(Lexer.TokenType expected) throws ParseException {
        if (pos >= tokens.size()) throw new ParseException("Expected " + expected + " but reached end");
        Lexer.Token t = tokens.get(pos);
        if (t.type != expected) throw new ParseException("Expected " + expected + " but got " + t.type);
        pos++;
    }

    private boolean inFIRST_NP() {
        return isType(Lexer.TokenType.DETERMINER) || isType(Lexer.TokenType.NOUN) || isType(Lexer.TokenType.PRONOUN) || isType(Lexer.TokenType.CODE_MIX);
    }

    private boolean inFOLLOW_NP() {
        return isType(Lexer.TokenType.VERB) || isType(Lexer.TokenType.PREPOSITION) || isType(Lexer.TokenType.CONJUNCTION);
    }

    private void log(String msg) {
        parseLog.add(msg);
        if (verbose) System.out.println("Parse log: " + msg);
    }

    public List<String> getParseLog() { return Collections.unmodifiableList(parseLog); }

    public static class ParseException extends Exception {
        public ParseException(String msg) { super(msg); }
    }
}
