package com.AI;
import java.util.*;
import java.util.regex.*;

/**
 * ============================================================
 *  LEXER — Camfranglais / Pidgin / Yaoundé Informal Speech
 * ============================================================
 *
 * WHAT THIS CLASS DOES:
 *   The Lexer is the first phase of the compiler pipeline.
 *   It reads a raw sentence (a String) and breaks it down into
 *   a list of TOKENS — the smallest meaningful units of the language.
 *
 *   This is called "Lexical Analysis" or "Scanning".
 *
 *   Input  : "Drop me for rond-point, I go pay you after"
 *   Output : [VERB:"Drop"] [PRONOUN:"me"] [PREPOSITION:"for"]
 *            [NOUN:"rond-point"] [PUNCTUATION:","] [PRONOUN:"I"]
 *            [VERB:"go"] [VERB:"pay"] [PRONOUN:"you"] [PREPOSITION:"after"]
 *
 * HOW IT WORKS:
 *   1. We define a list of token rules — each rule is a (TokenType, Regex) pair.
 *   2. We try each regex against the START of the remaining input.
 *   3. The first one that matches wins → we emit that token and advance.
 *   4. We repeat until the input is exhausted.
 *
 *   This is the "maximal munch" / "first-match" strategy.
 *   ORDER MATTERS: more specific rules (SLANG, NUMBER) must come before
 *   general rules (NOUN, VERB) or they will never match.
 */
public class Lexer {

    // ----------------------------------------------------------------
    //  TOKEN TYPES  — every word category in Yaoundé informal speech
    // ----------------------------------------------------------------
    public enum TokenType {
        // Structural
        SLANG,          // je wanda, ekiee, abeg, na so, wella, somehow
        CODE_MIX,       // mixed-language chunks: "na me", "I no sabi"
        NUMBER,         // 500, 2k, 1000, 200, 100
        PRONOUN,        // I, me, you, we, dem, e (he/it in Pidgin)
        VERB,           // go, pay, cut, sabi, drop, dey, don, fit, want
        NOUN,           // taxi, current, patron, road, phone, ENEO, moto
        ADJECTIVE,      // small, big, fresh, cold, long, stable, normal
        PREPOSITION,    // for, since, after, by, from, with, like
        CONJUNCTION,    // but, and, or, if, when, how
        DETERMINER,     // this, the, that, my, our, your, all, every
        INTERJECTION,   // Ekiee!, Abeg!, God help me, Shame on you
        PUNCTUATION,    // , . ! ? —
        UNKNOWN         // fallback for anything unrecognised
    }

    // ----------------------------------------------------------------
    //  TOKEN — one matched unit  (the data record we emit)
    // ----------------------------------------------------------------
    public static class Token {
        public final TokenType type;
        public final String lexeme;   // the actual text from input

        public Token(TokenType type, String lexeme) {
            this.type = type;
            this.lexeme = lexeme;
        }

        @Override
        public String toString() {
            return String.format("%-15s | %s", type, lexeme);
        }
    }

    // ----------------------------------------------------------------
    //  TOKEN RULE — pairs a regex with a token type
    // ----------------------------------------------------------------
    private static class TokenRule {
        final TokenType type;
        final Pattern pattern;

        TokenRule(TokenType type, String regex) {
            this.type = type;
            // CASE_INSENSITIVE so "Drop" and "drop" both match VERB
            this.pattern = Pattern.compile("^(" + regex + ")", Pattern.CASE_INSENSITIVE);
        }
    }

    // ----------------------------------------------------------------
    //  THE RULE TABLE  — ORDER IS CRITICAL
    //  More specific / longer patterns MUST come before generic ones.
    // ----------------------------------------------------------------
    private static final List<TokenRule> RULES = new ArrayList<>();

    static {
        // ── 1. PUNCTUATION (single chars — catch early so they don't confuse word rules)
        RULES.add(new TokenRule(TokenType.PUNCTUATION, "[,\\.!?;:\\-—]+"));

        // ── 2. NUMBERS  (e.g. 500, 2k, 1000, zéro-zéro)
        RULES.add(new TokenRule(TokenType.NUMBER, "[0-9]+(k)?|zéro[-]?zéro"));

        // ── 3. SLANG / DISCOURSE MARKERS  (fixed multi-word list — must come before VERB/NOUN)
        RULES.add(new TokenRule(TokenType.SLANG,
                "je wanda|na so|wella|ekiee|abeg|garr+|hmmm+|somehow|wahala|" +
                        "no dey|no be lie|up and down|show love|since morning|" +
                        "don tire|don die|don cut|don go|don reach|don spoil|don finish|" +
                        "go kill|go come|go block|go wait|go misbehave|go ruin|go stop|" +
                        "no sabi|no try|no get|no correct|no fit|no be|no dey"
        ));

        // ── 4. CODE-MIX chunks (Pidgin-French or mixed structures)
        RULES.add(new TokenRule(TokenType.CODE_MIX,
                "je go|na me|na you|na only|na so|na 500|na 200|na 300|na 1000|" +
                        "na every|na regular|na just|na new|na emergency|na wetin|na today|" +
                        "I no sabi|I dey|I go|I get|I be"
        ));

        // ── 5. PRONOUNS
        RULES.add(new TokenRule(TokenType.PRONOUN,
                "\\bI\\b|\\bme\\b|\\byou\\b|\\bwe\\b|\\bdem\\b|\\be\\b|\\bmy\\b|" +
                        "\\bour\\b|\\byour\\b|\\btheir\\b|\\bthem\\b|\\bus\\b|somebody|anybody|nobody"
        ));

        // ── 6. VERBS  (action words common in Yaoundé speech + Pidgin forms)
        RULES.add(new TokenRule(TokenType.VERB,
                "\\bdrop\\b|\\bpay\\b|\\bcut\\b|\\bsabi\\b|\\blower\\b|\\bopen\\b|" +
                        "\\bstop\\b|\\bpass\\b|\\bwait\\b|\\bbuy\\b|\\bsell\\b|\\btake\\b|" +
                        "\\bget\\b|\\bgo\\b|\\bcome\\b|\\bsay\\b|\\bdo\\b|\\bdey\\b|" +
                        "\\bdon\\b|\\bfit\\b|\\bwant\\b|\\bknow\\b|\\breed\\b|\\benter\\b|" +
                        "\\bfly\\b|\\bswim\\b|\\beach\\b|\\bread\\b|\\bsend\\b|\\bsubmit\\b|" +
                        "\\bshare\\b|\\bbreak\\b|\\bride\\b|\\bwear\\b|\\bshift\\b|\\bblock\\b|" +
                        "\\bdouble\\b|\\bfry\\b|\\bkill\\b|\\bblow\\b|\\bruins\\b|\\brun\\b|" +
                        "\\btry\\b|\\bstart\\b|\\bfinish\\b|\\bfind\\b|\\bneed\\b|\\bfeel\\b|" +
                        "\\bhear\\b|\\bsee\\b|\\bgive\\b|\\buse\\b|\\bcheck\\b|\\bhelp\\b|" +
                        "\\bkeep\\b|\\blast\\b|\\bbring\\b|\\bfall\\b|\\bbring\\b|\\bput\\b|" +
                        "\\bcall\\b|\\btell\\b|\\bmake\\b|\\bread\\b|\\blook\\b|\\bbuy\\b|" +
                        "\\blive\\b|\\bwalk\\b|\\bask\\b|\\bown\\b|\\bpay\\b|\\bwork\\b|" +
                        "\\bchanged\\b|\\bspent\\b|\\bbecome\\b|\\bdefrost\\b|\\bspoil\\b|" +
                        "\\bnegotiate\\b|\\bjump\\b|\\bstand\\b|\\bmiss\\b|\\breach\\b|" +
                        "\\btie\\b|\\blow\\b|\\bpull\\b|\\bpush\\b|\\bturn\\b|\\bcommot\\b"
        ));

        // ── 7. DETERMINERS (point to nouns)
        RULES.add(new TokenRule(TokenType.DETERMINER,
                "\\bthis\\b|\\bthe\\b|\\bthat\\b|\\ball\\b|\\bevery\\b|\\beach\\b|" +
                        "\\bsome\\b|\\bany\\b|\\bno\\b|\\bone\\b|\\btwo\\b|\\bthree\\b|" +
                        "\\bhalf\\b|\\bsame\\b|\\banother\\b"
        ));

        // ── 8. PREPOSITIONS
        RULES.add(new TokenRule(TokenType.PREPOSITION,
                "\\bfor\\b|\\bsince\\b|\\bafter\\b|\\bby\\b|\\bfrom\\b|\\bwith\\b|" +
                        "\\blike\\b|\\babout\\b|\\bat\\b|\\bon\\b|\\bin\\b|\\bof\\b|\\bto\\b|" +
                        "\\bnear\\b|\\binside\\b|\\boutside\\b|\\bthrough\\b|\\bper\\b|\\bnow\\b"
        ));

        // ── 9. CONJUNCTIONS
        RULES.add(new TokenRule(TokenType.CONJUNCTION,
                "\\bbut\\b|\\band\\b|\\bor\\b|\\bif\\b|\\bwhen\\b|\\bhow\\b|" +
                        "\\bso\\b|\\bthen\\b|\\bthat\\b|\\bbecause\\b|\\buntil\\b|\\bwhere\\b"
        ));

        // ── 10. ADJECTIVES
        RULES.add(new TokenRule(TokenType.ADJECTIVE,
                "\\bsmall\\b|\\bbig\\b|\\bfresh\\b|\\bcold\\b|\\blong\\b|\\bstable\\b|" +
                        "\\bnormal\\b|\\bgood\\b|\\bbad\\b|\\bfull\\b|\\bempty\\b|\\bspoil\\b|" +
                        "\\bblack\\b|\\bnew\\b|\\bold\\b|\\bfar\\b|\\bfast\\b|\\bquick\\b|" +
                        "\\bavailable\\b|\\bfinal\\b|\\bregular\\b|\\bbroken\\b|\\bstable\\b|" +
                        "\\bdiscreet\\b|\\bfree\\b|\\bclear\\b|\\bdouble\\b|\\bcomplete\\b"
        ));

        // ── 11. INTERJECTIONS
        RULES.add(new TokenRule(TokenType.INTERJECTION,
                "\\bekiee\\b|\\babeg\\b|\\bwetin\\b|\\bwahala\\b|\\bna\\b|" +
                        "\\bgod\\b|\\bclassic\\b|\\bshame\\b|\\bhelp\\b"
        ));

        // ── 12. NOUNS  (everything else that is a word — the catch-all for content words)
        //  This regex matches any word with letters (including accented French chars).
        //  It is INTENTIONALLY LAST among word rules so named categories win first.
        RULES.add(new TokenRule(TokenType.NOUN,
                "[a-zA-ZÀ-ÿ][a-zA-ZÀ-ÿ'\\-]*"
        ));

        // ── 13. UNKNOWN  (anything we still can't classify — safety net)
        RULES.add(new TokenRule(TokenType.UNKNOWN, "\\S+"));
    }

    // ----------------------------------------------------------------
    //  TOKENIZE  — the main public method
    //  Input  : raw sentence string
    //  Output : ordered list of Token objects
    // ----------------------------------------------------------------
    public List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        String remaining = input.trim();

        while (!remaining.isEmpty()) {
            // skip leading whitespace
            if (remaining.charAt(0) == ' ' || remaining.charAt(0) == '\t') {
                remaining = remaining.substring(1);
                continue;
            }

            boolean matched = false;

            for (TokenRule rule : RULES) {
                Matcher m = rule.pattern.matcher(remaining);
                if (m.find()) {
                    String lexeme = m.group(1);
                    tokens.add(new Token(rule.type, lexeme));
                    remaining = remaining.substring(lexeme.length());
                    matched = true;
                    break;   // first-match wins
                }
            }

            if (!matched) {
                // safety: consume one character as UNKNOWN to avoid infinite loop
                tokens.add(new Token(TokenType.UNKNOWN, String.valueOf(remaining.charAt(0))));
                remaining = remaining.substring(1);
            }
        }

        return tokens;
    }

    // ----------------------------------------------------------------
    //  PRINT TABLE — formats lexer output as a readable table
    // ----------------------------------------------------------------
    public void printTokenTable(String sentence, List<Token> tokens) {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║  INPUT: " + sentence);
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.printf("║  %-20s | %-25s ║%n", "TOKEN TYPE", "LEXEME");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        for (Token t : tokens) {
            System.out.printf("║  %-20s | %-25s ║%n", t.type, t.lexeme);
        }
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    // ----------------------------------------------------------------
    //  FREQUENCY COUNT — counts how often each token TYPE appears
    // ----------------------------------------------------------------
    public Map<TokenType, Integer> countFrequencies(List<Token> tokens) {
        Map<TokenType, Integer> freq = new LinkedHashMap<>();
        for (Token t : tokens) {
            freq.merge(t.type, 1, Integer::sum);
        }
        return freq;
    }
}
