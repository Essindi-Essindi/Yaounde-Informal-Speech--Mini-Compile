package com.sji.compilation;
import java.util.*;

/**
 * ============================================================
 *  PARSER — Recursive Descent LL(1) for Yaoundé Informal Speech
 * ============================================================
 *
 * WHAT THIS CLASS DOES:
 *   The Parser is the second phase of the compiler pipeline.
 *   It takes the list of tokens from the Lexer and checks whether
 *   the sentence follows our Context-Free Grammar (CFG).
 *
 *   This is called "Syntactic Analysis" or "Parsing".
 *
 *   Input  : [VERB:"Drop"] [PRONOUN:"me"] [PREPOSITION:"for"] [NOUN:"rond-point"]
 *   Output : "✓ ACCEPTED — valid sentence structure"
 *         or "✗ REJECTED — unexpected token at position 3"
 *
 * ──────────────────────────────────────────────────────────────
 *  OUR GRAMMAR (CFG) — Context-Free Grammar
 * ──────────────────────────────────────────────────────────────
 *
 *  S         → NP VP
 *            | VP                        (imperative: "Drop me for rond-point")
 *            | INTERJECTION S            (interjection prefix: "Abeg shift small")
 *            | SLANG S                   (slang prefix: "Na so dem dey do")
 *            | CODE_MIX VP              (code-mix subject: "I no sabi wetin...")
 *
 *  NP        → DET? NOUN NP'
 *            | PRONOUN NP'
 *            | CODE_MIX                  (e.g., "Na me")
 *
 *  NP'       → NOUN NP'                 (right-recursive, avoids left recursion)
 *            | ε
 *
 *  VP        → VERB VP'
 *
 *  VP'       → NP VP'                   (object)
 *            | PP VP'                   (prepositional phrase)
 *            | ADJECTIVE VP'            (predicate adjective)
 *            | SLANG VP'               (discourse marker)
 *            | ε
 *
 *  PP        → PREPOSITION NP
 *            | PREPOSITION ADJECTIVE
 *
 *  DET       → DETERMINER
 *
 * ──────────────────────────────────────────────────────────────
 *  WHY LL(1)?
 *   LL(1) means:
 *     L  = reads input Left to right
 *     L  = produces Leftmost derivation
 *     (1) = looks ahead only 1 token to decide which rule to apply
 *
 *   This works for our grammar because:
 *   - Our sentences are short (5-15 words)
 *   - We removed Left Recursion (NP → NP NOUN became NP → NOUN NP')
 *   - Each alternative starts with a DISTINCT token type
 *
 * ──────────────────────────────────────────────────────────────
 *  HOW RECURSIVE DESCENT WORKS:
 *   Each grammar rule becomes a Java method.
 *   The method looks at the CURRENT token and decides which
 *   sub-rule to apply. It calls other methods for sub-rules.
 *   The call stack mirrors the parse tree.
 *
 *   Grammar rule:  VP → VERB VP'
 *   Java method:   void parseVP() { consume(VERB); parseVP_prime(); }
 *
 * ──────────────────────────────────────────────────────────────
 *  FIRST SETS (used to decide which rule to pick):
 *
 *   FIRST(S)   = {VERB, PRONOUN, NOUN, DETERMINER, INTERJECTION, SLANG, CODE_MIX}
 *   FIRST(NP)  = {DETERMINER, NOUN, PRONOUN, CODE_MIX}
 *   FIRST(NP') = {NOUN, ε}
 *   FIRST(VP)  = {VERB}
 *   FIRST(VP') = {NOUN, PRONOUN, DETERMINER, CODE_MIX, PREPOSITION, ADJECTIVE, SLANG, ε}
 *   FIRST(PP)  = {PREPOSITION}
 *
 *  FOLLOW SETS (used to know when a nullable rule ends):
 *
 *   FOLLOW(S)   = {$}                        ($ = end of input)
 *   FOLLOW(NP)  = {VERB, PREPOSITION, PUNCTUATION, CONJUNCTION, $}
 *   FOLLOW(NP') = FOLLOW(NP)
 *   FOLLOW(VP)  = {PUNCTUATION, CONJUNCTION, $}
 *   FOLLOW(VP') = FOLLOW(VP)
 *   FOLLOW(PP)  = {VERB, NOUN, PRONOUN, PUNCTUATION, $}
 */
public class Parser {

    private List<Lexer.Token> tokens;  // the token stream from Lexer
    private int pos;                   // current position in token list
    private List<String> parseLog;    // records every decision made (for the report)
    private boolean verbose;          // whether to print step-by-step trace

    // ----------------------------------------------------------------
    //  CONSTRUCTOR
    // ----------------------------------------------------------------
    public Parser(boolean verbose) {
        this.verbose = verbose;
        this.parseLog = new ArrayList<>();
    }

    // ----------------------------------------------------------------
    //  PARSE — main entry point
    //  Returns true if sentence is accepted, false if rejected
    // ----------------------------------------------------------------
    public boolean parse(List<Lexer.Token> tokenList) {
        this.tokens = tokenList;
        this.pos = 0;
        this.parseLog = new ArrayList<>();

        // filter out punctuation at the end (it doesn't affect structure)
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
            parseS();   // start the parse from the top-level rule

            if (pos == tokens.size()) {
                log("ACCEPT: all tokens consumed successfully");
                return true;
            } else {
                log("REJECT: unconsumed tokens starting at position " + pos +
                        " → \"" + current().lexeme + "\"");
                return false;
            }
        } catch (ParseException e) {
            log("REJECT: " + e.getMessage());
            return false;
        }
    }

    // ================================================================
    //  GRAMMAR RULE METHODS — each method = one grammar rule
    // ================================================================

    /**
     *  S → INTERJECTION S
     *    | SLANG S
     *    | CODE_MIX VP
     *    | NP VP
     *    | VP               (imperative sentences)
     */
    private void parseS() throws ParseException {
        log("Parsing S with lookahead: " + (pos < tokens.size() ? current() : "EOF"));

        if (isType(Lexer.TokenType.INTERJECTION) || isLexeme("abeg") || isLexeme("ekiee")) {
            log("  S → INTERJECTION S");
            consume(Lexer.TokenType.INTERJECTION);
            if (pos < tokens.size()) parseS();

        } else if (isType(Lexer.TokenType.SLANG)) {
            log("  S → SLANG S");
            consume(Lexer.TokenType.SLANG);
            if (pos < tokens.size()) parseS();

        } else if (isType(Lexer.TokenType.CODE_MIX)) {
            log("  S → CODE_MIX VP");
            consume(Lexer.TokenType.CODE_MIX);
            parseVP();

        } else if (isType(Lexer.TokenType.VERB)) {
            log("  S → VP  (imperative)");
            parseVP();

        } else if (inFIRST_NP()) {
            log("  S → NP VP");
            parseNP();
            if (pos < tokens.size() && isType(Lexer.TokenType.VERB)) {
                parseVP();
            }
        } else {
            throw new ParseException(
                    "S: expected sentence start (VERB/NOUN/PRONOUN/SLANG/CODE_MIX), got: " + current()
            );
        }
    }

    /**
     *  NP  → DET NOUN NP'
     *      | NOUN NP'
     *      | PRONOUN NP'
     *      | CODE_MIX
     */
    private void parseNP() throws ParseException {
        log("  Parsing NP with lookahead: " + (pos < tokens.size() ? current() : "EOF"));

        if (isType(Lexer.TokenType.DETERMINER)) {
            log("    NP → DET NOUN NP'");
            consume(Lexer.TokenType.DETERMINER);
            if (isType(Lexer.TokenType.NOUN) || isType(Lexer.TokenType.ADJECTIVE)) {
                consume(current().type);
            }
            parseNP_prime();

        } else if (isType(Lexer.TokenType.NOUN)) {
            log("    NP → NOUN NP'");
            consume(Lexer.TokenType.NOUN);
            parseNP_prime();

        } else if (isType(Lexer.TokenType.PRONOUN)) {
            log("    NP → PRONOUN NP'");
            consume(Lexer.TokenType.PRONOUN);
            parseNP_prime();

        } else if (isType(Lexer.TokenType.CODE_MIX)) {
            log("    NP → CODE_MIX");
            consume(Lexer.TokenType.CODE_MIX);

        } else {
            throw new ParseException("NP: expected noun phrase, got: " +
                    (pos < tokens.size() ? current() : "EOF"));
        }
    }

    /**
     *  NP' → NOUN NP'    (right-recursive tail — replaces left recursion)
     *       | ε
     *
     *  FOLLOW(NP') = {VERB, PREPOSITION, CONJUNCTION, PUNCTUATION, $}
     *  → if current token is in FOLLOW, we choose ε (empty)
     */
    private void parseNP_prime() throws ParseException {
        if (pos >= tokens.size() || inFOLLOW_NP()) {
            log("    NP' → ε");
            return;  // epsilon production
        }

        if (isType(Lexer.TokenType.NOUN)) {
            log("    NP' → NOUN NP'");
            consume(Lexer.TokenType.NOUN);
            parseNP_prime();
        } else {
            log("    NP' → ε (no more noun chain)");
        }
    }

    /**
     *  VP → VERB VP'
     */
    private void parseVP() throws ParseException {
        log("  Parsing VP with lookahead: " + (pos < tokens.size() ? current() : "EOF"));

        if (isType(Lexer.TokenType.VERB)) {
            log("    VP → VERB VP'");
            consume(Lexer.TokenType.VERB);
            parseVP_prime();
        } else {
            throw new ParseException("VP: expected VERB, got: " +
                    (pos < tokens.size() ? current() : "EOF"));
        }
    }

    /**
     *  VP' → NP VP'
     *      | PP VP'
     *      | ADJECTIVE VP'
     *      | SLANG VP'
     *      | CONJUNCTION VP'   (for sentences like "... but I go pay")
     *      | ε
     *
     *  FOLLOW(VP') = {$, CONJUNCTION}
     */
    private void parseVP_prime() throws ParseException {
        if (pos >= tokens.size() || inFOLLOW_VP()) {
            log("    VP' → ε");
            return;
        }

        if (isType(Lexer.TokenType.PREPOSITION)) {
            log("    VP' → PP VP'");
            parsePP();
            parseVP_prime();

        } else if (isType(Lexer.TokenType.ADJECTIVE)) {
            log("    VP' → ADJECTIVE VP'");
            consume(Lexer.TokenType.ADJECTIVE);
            parseVP_prime();

        } else if (isType(Lexer.TokenType.SLANG)) {
            log("    VP' → SLANG VP'");
            consume(Lexer.TokenType.SLANG);
            parseVP_prime();

        } else if (isType(Lexer.TokenType.CONJUNCTION)) {
            log("    VP' → CONJUNCTION VP'");
            consume(Lexer.TokenType.CONJUNCTION);
            if (pos < tokens.size() && isType(Lexer.TokenType.VERB)) {
                parseVP();
            } else if (pos < tokens.size() && inFIRST_NP()) {
                parseNP();
            }

        } else if (isType(Lexer.TokenType.NUMBER)) {
            log("    VP' → NUMBER VP'  (amount/price)");
            consume(Lexer.TokenType.NUMBER);
            parseVP_prime();

        } else if (inFIRST_NP()) {
            log("    VP' → NP VP'");
            parseNP();
            parseVP_prime();

        } else if (isType(Lexer.TokenType.VERB)) {
            log("    VP' → VERB VP'  (chained verb)");
            consume(Lexer.TokenType.VERB);
            parseVP_prime();

        } else {
            log("    VP' → ε (unrecognised continuation, stopping gracefully)");
        }
    }

    /**
     *  PP → PREPOSITION NP
     *      | PREPOSITION ADJECTIVE
     *      | PREPOSITION NUMBER
     */
    private void parsePP() throws ParseException {
        log("    Parsing PP");
        consume(Lexer.TokenType.PREPOSITION);

        if (pos < tokens.size()) {
            if (inFIRST_NP()) {
                parseNP();
            } else if (isType(Lexer.TokenType.ADJECTIVE)) {
                consume(Lexer.TokenType.ADJECTIVE);
            } else if (isType(Lexer.TokenType.NUMBER)) {
                consume(Lexer.TokenType.NUMBER);
            }
            // else: bare preposition at end of sentence — accepted
        }
    }

    // ================================================================
    //  HELPER METHODS
    // ================================================================

    /** Returns the current token without consuming it (lookahead) */
    private Lexer.Token current() {
        return tokens.get(pos);
    }

    /** Checks if current token has the given type */
    private boolean isType(Lexer.TokenType type) {
        return pos < tokens.size() && tokens.get(pos).type == type;
    }

    /** Checks if current token has the given lexeme (case-insensitive) */
    private boolean isLexeme(String lex) {
        return pos < tokens.size() && tokens.get(pos).lexeme.equalsIgnoreCase(lex);
    }

    /**
     * CONSUME — advances past the current token if it matches the expected type.
     * Throws ParseException if the token doesn't match.
     */
    private void consume(Lexer.TokenType expected) throws ParseException {
        if (pos >= tokens.size()) {
            throw new ParseException("Expected " + expected + " but reached end of input");
        }
        Lexer.Token t = tokens.get(pos);
        if (t.type != expected) {
            throw new ParseException(
                    "Expected " + expected + " but got " + t.type + " (\"" + t.lexeme + "\") at position " + pos
            );
        }
        log("      ✓ consumed [" + t.type + ": \"" + t.lexeme + "\"]");
        pos++;
    }

    // ── FIRST sets as boolean predicates ────────────────────────────

    /** FIRST(NP) = {DETERMINER, NOUN, PRONOUN, CODE_MIX} */
    private boolean inFIRST_NP() {
        return isType(Lexer.TokenType.DETERMINER)
                || isType(Lexer.TokenType.NOUN)
                || isType(Lexer.TokenType.PRONOUN)
                || isType(Lexer.TokenType.CODE_MIX);
    }

    // ── FOLLOW sets as boolean predicates ───────────────────────────

    /** FOLLOW(NP) and FOLLOW(NP') = {VERB, PREPOSITION, CONJUNCTION, $} */
    private boolean inFOLLOW_NP() {
        return isType(Lexer.TokenType.VERB)
                || isType(Lexer.TokenType.PREPOSITION)
                || isType(Lexer.TokenType.CONJUNCTION);
    }

    /** FOLLOW(VP) and FOLLOW(VP') = {CONJUNCTION, $} */
    private boolean inFOLLOW_VP() {
        return isType(Lexer.TokenType.CONJUNCTION);
    }

    // ── Logging ─────────────────────────────────────────────────────

    private void log(String msg) {
        parseLog.add(msg);
        if (verbose) System.out.println("  [PARSE] " + msg);
    }

    public List<String> getParseLog() {
        return Collections.unmodifiableList(parseLog);
    }

    // ── Custom exception ────────────────────────────────────────────

    public static class ParseException extends Exception {
        public ParseException(String msg) { super(msg); }
    }
}