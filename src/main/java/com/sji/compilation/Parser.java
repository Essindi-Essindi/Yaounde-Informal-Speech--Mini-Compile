package com.sji.compilation;
import java.util.*;

/**
 * Recursive Descent Parser for Yaounde informal speech.
 * Authors: Abena, Essindi and Yimgaing
 * Implements an LL(1)-style parser to validate sentence structure against defined grammar rules.
 *
 * Key grammar rules:
 *  - MultiClause -> S (S)*       multi-clause sentences joined by comma
 *  - S  -> NP PRONOUN VP         appositive / topic-comment ("Chauffeur, you no fit go")
 *  - S  -> ADJECTIVE VP          adjective-subject sentences ("Final price be wetin")
 *  - VP -> NEG_VERB VERB VP'     negation marker MUST be immediately followed by a VERB
 *  - VP -> VERB VP'              normal verb phrase
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
            filtered.add(t);
        }
        this.tokens = filtered;

        if (tokens.isEmpty()) {
            log("REJECT: empty input");
            return false;
        }

        try {
            parseMultiClause();
            if (pos == tokens.size()) {
                log("ACCEPT: all tokens consumed successfully");
                return true;
            } else {
                log("REJECT: unconsumed tokens at position " + pos +
                        " --> \"" + (pos < tokens.size() ? current().lexeme : "EOF") + "\"");
                return false;
            }
        } catch (ParseException e) {
            log("REJECT: " + e.getMessage());
            return false;
        }
    }



    /**
     * Top-level: handles comma-joined multi-clause sentences.
     */
    private void parseMultiClause() throws ParseException {
        parseS();
        while (pos < tokens.size()) {
            boolean hasSeparator = false;
            while (pos < tokens.size() && (isType(Lexer.TokenType.PUNCTUATION) || isType(Lexer.TokenType.CONJUNCTION))) {
                if (isType(Lexer.TokenType.PUNCTUATION)) {
                    consume(Lexer.TokenType.PUNCTUATION);
                } else {
                    consume(Lexer.TokenType.CONJUNCTION);
                }
                hasSeparator = true;
            }
            if (hasSeparator && pos < tokens.size() && canStartClause()) {
                log("Continuing to next clause at position " + pos + " --> \"" + current().lexeme + "\"");
                parseS();
            } else {
                break;
            }
        }
    }

    private boolean canStartClause() {
        return isType(Lexer.TokenType.INTERJECTION)
                || isType(Lexer.TokenType.SLANG)
                || isType(Lexer.TokenType.VERB)
                || isType(Lexer.TokenType.AUX)
                || isType(Lexer.TokenType.WH_PRONOUN)
                || isType(Lexer.TokenType.FOCUS)
                || isType(Lexer.TokenType.ADJECTIVE)
                || isType(Lexer.TokenType.PREPOSITION)
                || inFIRST_NP();
    }

    /**
     * S -> INTERJECTION S
     *    | SLANG S
     *    | VP                   (imperative / verb-first, including negation-first)
     *    | ADJECTIVE [NOUN] VP  (adjective-subject: "Final price be wetin")
     *    | NP [PRONOUN] [VP]    (subject-predicate, with optional appositive pronoun)
     */
    private void parseS() throws ParseException {
        log("ENTER parseS | Lookahead: " + (pos < tokens.size() ? current() : "EOF"));

        if (isType(Lexer.TokenType.INTERJECTION)) {
            log("Applying Rule: S -> INTERJECTION S");
            consume(Lexer.TokenType.INTERJECTION);
            if (pos < tokens.size() && canStartClause()) parseS();

        } else if (isType(Lexer.TokenType.SLANG)) {
            log("Applying Rule: S -> SLANG S");
            consume(Lexer.TokenType.SLANG);
            if (pos < tokens.size()) parseS();
        } else if (isType(Lexer.TokenType.WH_PRONOUN)) {
            log("Applying Rule: S -> WH_PRONOUN NP VP");
            consume(Lexer.TokenType.WH_PRONOUN);
            parseNP();
            parseVP();
        } else if (isType(Lexer.TokenType.FOCUS)) {
            log("Applying Rule: S -> FOCUS NP [S]");
            consume(Lexer.TokenType.FOCUS);
            parseNP();
            if (canStartClause()) parseS();
        } else if (isType(Lexer.TokenType.PREPOSITION)) {
            log("Applying Rule: S -> PP S");
            parsePP();
            if (pos < tokens.size() && canStartClause()) {
                parseS();
            }
        } else if (isType(Lexer.TokenType.VERB) || isType(Lexer.TokenType.AUX)) {
            log("Applying Rule: S -> VP");
            parseVP();

        } else if (isType(Lexer.TokenType.ADJECTIVE)) {
            log("Applying Rule: S -> ADJECTIVE [NOUN] VP");
            consume(Lexer.TokenType.ADJECTIVE);
            if (isType(Lexer.TokenType.NOUN)) consume(Lexer.TokenType.NOUN);
            if (pos < tokens.size() && isType(Lexer.TokenType.VERB)) parseVP();

        } else if (inFIRST_NP()) {
            log("Applying Rule: S -> NP [VP]");
            parseNP();
            if (pos < tokens.size() && (isType(Lexer.TokenType.VERB) || isType(Lexer.TokenType.AUX) || isType(Lexer.TokenType.SLANG))) {
                parseVP();
            }
        } else {
            throw new ParseException("Expected sentence start (INTERJECTION, SLANG, WH_PRONOUN, FOCUS, VERB, AUX, or NP), but found: " + (pos < tokens.size() ? current() : "EOF"));
        }
    }

    private void parseNP() throws ParseException {
        log("ENTER parseNP | Lookahead: " + (pos < tokens.size() ? current() : "EOF"));
        if (isType(Lexer.TokenType.DETERMINER)) {
            log("Applying Rule: NP -> DETERMINER [NOUN|ADJECTIVE|VERB] NP'");
            consume(Lexer.TokenType.DETERMINER);
            if (isType(Lexer.TokenType.NOUN)) {
                consume(Lexer.TokenType.NOUN);
            } else if (isType(Lexer.TokenType.ADJECTIVE)) {
                consume(Lexer.TokenType.ADJECTIVE);
            } else if (isType(Lexer.TokenType.VERB)) {
                consume(Lexer.TokenType.VERB);
            } else {
                throw new ParseException("Expected NOUN, ADJECTIVE, or VERB after DETERMINER, but found: " + (pos < tokens.size() ? current() : "EOF"));
            }
            parseNP_prime();
        } else if (isType(Lexer.TokenType.PRONOUN)) {
            log("Applying Rule: NP -> PRONOUN NP'");
            consume(Lexer.TokenType.PRONOUN);
            parseNP_prime();
        } else if (isType(Lexer.TokenType.NOUN)) {
            log("Applying Rule: NP -> NOUN NP'");
            consume(Lexer.TokenType.NOUN);
            parseNP_prime();
        } else if (isType(Lexer.TokenType.CODE_MIX)) {
            log("Applying Rule: NP -> CODE_MIX");
            consume(Lexer.TokenType.CODE_MIX);
        } else {
            throw new ParseException("Expected Noun Phrase (DETERMINER, NOUN, PRONOUN, or CODE_MIX), but found: "
                    + (pos < tokens.size() ? current() : "EOF"));
        }
    }

    private void parseNP_prime() throws ParseException {
        log("ENTER parseNP_prime | Lookahead: " + (pos < tokens.size() ? current() : "EOF"));
        if (pos >= tokens.size() || inFOLLOW_NP()) {
            log("Applying Rule: NP' -> ε (Epsilon transition)");
            return;
        }
        if (isType(Lexer.TokenType.NOUN)) {
            log("Applying Rule: NP' -> NOUN NP'");
            consume(Lexer.TokenType.NOUN);
            parseNP_prime();
        } else {
            log("Applying Rule: NP' -> ε (Fallback)");
        }
    }

    private void parseVP() throws ParseException {
        log("ENTER parseVP | Lookahead: " + (pos < tokens.size() ? current() : "EOF"));
        if (isType(Lexer.TokenType.AUX)) {
            log("Applying Rule: VP -> AUX [VP]");
            consume(Lexer.TokenType.AUX);
            if (pos < tokens.size() && (isType(Lexer.TokenType.VERB) || isType(Lexer.TokenType.AUX) || isType(Lexer.TokenType.SLANG))) {
                parseVP();
            } else {
                parseVP_prime(0, 0, 0);
            }
        } else if (isType(Lexer.TokenType.SLANG)) {
            log("Applying Rule: VP -> SLANG VP");
            consume(Lexer.TokenType.SLANG);
            parseVP();


        } else if (isType(Lexer.TokenType.VERB)) {
            log("Applying Rule: VP -> VERB VP'");
            consume(Lexer.TokenType.VERB);
            parseVP_prime(0, 0, 0);
        } else {
            throw new ParseException("Expected Verb Phrase (AUX, SLANG or VERB), but found: " + (pos < tokens.size() ? current() : "EOF"));
        }
    }

    private void parseVP_prime(int npCount, int verbCount, int totalCount) throws ParseException {
        log("ENTER parseVP_prime | Lookahead: " + (pos < tokens.size() ? current() : "EOF"));
        if (pos >= tokens.size()) {
            log("Applying Rule: VP' -> ε (End of tokens)");
            return;
        }

        if (totalCount > 5) {
            log("Applying Rule: VP' -> ε (Max total complements reached)");
            return;
        }

        if (isType(Lexer.TokenType.PREPOSITION)) {
            log("Applying Rule: VP' -> PP VP'");
            parsePP();
            parseVP_prime(npCount, verbCount, totalCount + 1);
        } else if (isType(Lexer.TokenType.ADV)) {
            log("Applying Rule: VP' -> ADV VP'");
            consume(Lexer.TokenType.ADV);
            parseVP_prime(npCount, verbCount, totalCount + 1);
        } else if (isType(Lexer.TokenType.ADJECTIVE)) {
            log("Applying Rule: VP' -> ADJECTIVE VP'");
            consume(Lexer.TokenType.ADJECTIVE);
            parseVP_prime(npCount, verbCount, totalCount + 1);
        } else if (isType(Lexer.TokenType.SLANG)) {
            log("Applying Rule: VP' -> SLANG VP'");
            consume(Lexer.TokenType.SLANG);
            parseVP_prime(npCount, verbCount, totalCount + 1);
        } else if (isType(Lexer.TokenType.INTERJECTION)) {
            log("Applying Rule: VP' -> INTERJECTION VP'");
            consume(Lexer.TokenType.INTERJECTION);
            parseVP_prime(npCount, verbCount, totalCount + 1);
        } else if (isType(Lexer.TokenType.CONJUNCTION)) {
            log("Applying Rule: VP' -> CONJUNCTION S");
            consume(Lexer.TokenType.CONJUNCTION);
            parseS();
        } else if (isType(Lexer.TokenType.NUMBER)) {
            log("Applying Rule: VP' -> NUMBER VP'");
            consume(Lexer.TokenType.NUMBER);
            parseVP_prime(npCount, verbCount, totalCount + 1);
        } else if (inFIRST_NP()) {
            if (npCount >= 2) {
                log("Applying Rule: VP' -> ε (Max 2 NP complements reached)");
                return;
            }
            log("Applying Rule: VP' -> NP VP'");
            parseNP();
            parseVP_prime(npCount + 1, verbCount, totalCount + 1);
        } else if (isType(Lexer.TokenType.VERB)) {
            log("Applying Rule: VP' -> VERB VP'");
            consume(Lexer.TokenType.VERB);
            parseVP_prime(npCount, verbCount + 1, totalCount + 1);
        } else if (isType(Lexer.TokenType.FOCUS)) {
            log("Applying Rule: VP' -> FOCUS  (sentence-final emphasis particle)");
            consume(Lexer.TokenType.FOCUS);
        } else {
            log("Applying Rule: VP' -> ε (No matching complement, exiting VP_prime)");
        }
    }

    private void parsePP() throws ParseException {
        log("ENTER parsePP | Lookahead: " + (pos < tokens.size() ? current() : "EOF"));
        log("Applying Rule: PP -> PREPOSITION [NP | ADJECTIVE | NUMBER]");
        consume(Lexer.TokenType.PREPOSITION);
        
        if (pos < tokens.size()) {
            if (isType(Lexer.TokenType.ADJECTIVE)) {
                consume(Lexer.TokenType.ADJECTIVE);
            } else if (isType(Lexer.TokenType.NUMBER)) {
                consume(Lexer.TokenType.NUMBER);
            } else if (inFIRST_NP()) {
                parseNP();
            }
        }
    }

    private Lexer.Token current() { return tokens.get(pos); }
    private boolean isType(Lexer.TokenType type) { return pos < tokens.size() && tokens.get(pos).types.contains(type); }
    private boolean isLexeme(String lex) { return pos < tokens.size() && tokens.get(pos).lexeme.equalsIgnoreCase(lex); }

    private void consume(Lexer.TokenType expected) throws ParseException {
        if (pos >= tokens.size())
            throw new ParseException("Expected " + expected + " but reached end of input");
        Lexer.Token t = tokens.get(pos);
        if (!t.types.contains(expected)) {
            throw new ParseException("Expected " + expected + " at position " + pos + ", but got " + t.types + " (\"" + t.lexeme + "\")");
        }
        log("[MATCH] Consumed " + expected + " (\"" + t.lexeme + "\") at position " + pos);
        pos++;
    }

    private boolean inFIRST_NP() {
        return isType(Lexer.TokenType.DETERMINER)
                || isType(Lexer.TokenType.NOUN)
                || isType(Lexer.TokenType.PRONOUN)
                || isType(Lexer.TokenType.CODE_MIX);
    }

    private boolean inFOLLOW_NP() {
        return isType(Lexer.TokenType.VERB) || isType(Lexer.TokenType.AUX) || 
               isType(Lexer.TokenType.PREPOSITION) || isType(Lexer.TokenType.CONJUNCTION) || 
               isType(Lexer.TokenType.SLANG) || isType(Lexer.TokenType.ADJECTIVE) || 
               isType(Lexer.TokenType.INTERJECTION) || isType(Lexer.TokenType.NUMBER);
    }

    private void log(String msg) {
        int depth = 0;
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            String mName = element.getMethodName();
            if (mName.startsWith("parse") && !mName.equals("parse") && !mName.equals("parseMultiClause")) {
                depth++;
            }
        }
        depth = Math.max(0, depth - 1);
        
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("  ");
        }
        
        String formattedMsg = indent.toString() + msg;
        parseLog.add(formattedMsg);
        if (verbose) System.out.println("Parse log: " + formattedMsg);
    }

    public List<String> getParseLog() { return Collections.unmodifiableList(parseLog); }

    public static class ParseException extends Exception {
        public ParseException(String msg) { super(msg); }
    }
}