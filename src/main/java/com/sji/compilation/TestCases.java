package com.sji.compilation;
import java.util.*;

/**
 * ============================================================
 *  TESTCASES.JAVA — Phase 5 Testing
 * ============================================================
 *
 * HOW TO RUN IN INTELLIJ:
 *   Right-click this file → Run 'TestCases.main()'
 *
 * WHAT THIS DOES:
 *   1. Runs a curated set of 15 test sentences through Lexer + Parser
 *   2. For each sentence: prints token table + ACCEPTED/REJECTED
 *   3. For rejected ones: prints the linguistic reason WHY
 *   4. Prints a final summary table (copy this into your report)
 *   5. Prints a SCREENSHOTS GUIDE so you know exactly what to capture
 *
 * This file is your Phase 5 deliverable.
 * Everything printed here goes directly into your report.
 */
public class TestCases {

    // ────────────────────────────────────────────────────────────────
    //  TEST CASE DEFINITION
    //  Each entry: sentence | context | expected result | reason if rejected
    // ────────────────────────────────────────────────────────────────
    static final Object[][] TESTS = {

            // ── SENTENCES EXPECTED TO BE ACCEPTED (clear structure) ──────
            {
                    "Drop me for rond-point, I go pay you after",
                    "Taxi — passenger to driver",
                    true,
                    null
            },
            {
                    "I dey try submit assignment online",
                    "Internet — student, shortened",
                    true,
                    null
            },
            {
                    "Somebody share hotspot abeg",
                    "Internet — asking for hotspot",
                    true,
                    null
            },
            {
                    "Patron lower am small",
                    "Market — price negotiation, core clause",
                    true,
                    null
            },
            {
                    "You dey sell spoil thing",
                    "Market — quality complaint",
                    true,
                    null
            },
            {
                    "Na so dem dey do for this Yaoundé",
                    "Taxi — SLANG-prefixed commentary",
                    true,
                    null
            },
            {
                    "My umbrella fly go since that wind blow",
                    "Rain — storm damage",
                    true,
                    null
            },
            {
                    "You want change your money",
                    "Roadside — money changer",
                    true,
                    null
            },

            // ── SENTENCES EXPECTED TO BE REJECTED (linguistic reasons) ───
            {
                    "Current don cut since morning, I no sabi wetin ENEO dey do with our money",
                    "Electricity — two chained clauses separated by comma",
                    false,
                    "MULTI-CLAUSE PARATAXIS: Two independent clauses joined by comma with no conjunction. " +
                            "Yaoundé speech chains clauses orally without formal connectors. " +
                            "Grammar handles one S at a time; the comma boundary leaves unconsumed tokens."
            },
            {
                    "Abeg shift small for seat, you dey sit like say na you buy the taxi",
                    "Taxi — 'Abeg' interjection + second clause",
                    false,
                    "LEXER TYPE MISMATCH: 'Abeg' is classified as SLANG but S → INTERJECTION S expects " +
                            "token type INTERJECTION. Grammatical handling of 'abeg' requires adding SLANG " +
                            "as an alternative in the interjection rule. Also contains a second clause."
            },
            {
                    "Light don go again! Na every night dem dey do this thing, je wanda",
                    "Electricity — repeated outage, two clauses",
                    false,
                    "SLANG AS SUBJECT + MULTI-CLAUSE: 'Light don go' — 'don go' is a SLANG token " +
                            "(Pidgin perfect aspect). The parser sees NOUN then SLANG, but VP expects VERB. " +
                            "This shows that Pidgin aspect markers (don + verb) function as verb phrases " +
                            "but our grammar expects a single VERB token."
            },
            {
                    "MTN dey do me somehow, data finish but e say network unavailable",
                    "Internet — MTN, two clauses with 'but'",
                    false,
                    "CLAUSE BOUNDARY WITH CONJUNCTION: After the first clause 'data finish', " +
                            "the 'but' conjunction triggers VP' → CONJUNCTION VP'. However 'e say' starts " +
                            "a new subject-verb pair, which our VP' continuation doesn't fully handle. " +
                            "This illustrates ellipsis and clause-chaining typical of informal speech."
            },
            {
                    "Final price be wetin? I no get time dey bargain up and down today",
                    "Market — question + statement, two clauses",
                    false,
                    "ADJECTIVE-INITIAL SENTENCE: 'Final' is classified as ADJECTIVE, but S has no " +
                            "rule for S → ADJECTIVE NP VP. In Yaoundé informal speech, noun phrases can be " +
                            "fronted with modifiers ('Final price') in a way that doesn't match standard " +
                            "English grammar. Also two clauses chained."
            },
            {
                    "Essence don finish for every station, queue long reach three street",
                    "Fuel — scarcity, two comma-joined clauses",
                    false,
                    "SLANG AS PREDICATE + MULTI-CLAUSE: 'don finish' is a SLANG token (Pidgin perfect). " +
                            "After NP 'Essence', the parser expects a VERB but finds SLANG. This shows that " +
                            "Camfranglais uses multi-word Pidgin tenses ('don + V') that act as complete VPs " +
                            "but don't map to a single VERB token."
            },
            {
                    "Rain don start, road don become river, my shoe don finish",
                    "Rain — three chained clauses",
                    false,
                    "THREE-CLAUSE CHAIN: Three independent clauses separated by commas. " +
                            "After parsing 'Rain don start' partially, remaining tokens form two more " +
                            "full clauses. This is classic oral speech parataxis — the pattern " +
                            "'X happen, Y happen, Z happen' is extremely common in Yaoundé but " +
                            "cannot be represented in a single-clause CFG without extension."
            }
    };

    // ────────────────────────────────────────────────────────────────
    //  MAIN — runs all tests and prints report-ready output
    // ────────────────────────────────────────────────────────────────
    public static void main(String[] args) {

        Lexer lexer = new Lexer();
        Parser parser = new Parser(false);

        int passed = 0;   // result matched expectation
        int failed = 0;   // result did NOT match expectation
        int accepted = 0;
        int rejected = 0;

        List<String[]> summaryRows = new ArrayList<>();

        printBanner("PHASE 5 — TESTING REPORT");
        printBanner("Yaoundé Informal Speech Compiler — ICT University");

        // ── Run each test ──────────────────────────────────────────
        for (int i = 0; i < TESTS.length; i++) {
            String sentence  = (String)  TESTS[i][0];
            String context   = (String)  TESTS[i][1];
            boolean expected = (boolean) TESTS[i][2];
            String reason    = (String)  TESTS[i][3];

            System.out.println("\n" + "═".repeat(66));
            System.out.printf("  TEST #%-2d  %s%n", i + 1, context);
            System.out.println("═".repeat(66));

            // ── LEXER ─────────────────────────────────────────────
            List<Lexer.Token> tokens = lexer.tokenize(sentence);
            lexer.printTokenTable(sentence, tokens);

            // ── PARSER ────────────────────────────────────────────
            boolean result = parser.parse(tokens);

            if (result) {
                System.out.println("\n  ✅  PARSER: ACCEPTED — sentence fits the grammar.");
                accepted++;
            } else {
                System.out.println("\n  ❌  PARSER: REJECTED");
                // show last few log entries (the rejection reason from parser)
                List<String> log = parser.getParseLog();
                int start = Math.max(0, log.size() - 3);
                for (int j = start; j < log.size(); j++) {
                    System.out.println("     » " + log.get(j));
                }
                rejected++;
            }

            // ── Linguistic explanation for rejected sentences ──────
            if (!result && reason != null) {
                System.out.println("\n  ┌─ LINGUISTIC REASON FOR REJECTION ─────────────────");
                // word-wrap at ~60 chars
                String[] words = reason.split(" ");
                StringBuilder line = new StringBuilder("  │  ");
                for (String w : words) {
                    if (line.length() + w.length() > 64) {
                        System.out.println(line);
                        line = new StringBuilder("  │  ");
                    }
                    line.append(w).append(" ");
                }
                if (line.length() > 5) System.out.println(line);
                System.out.println("  └────────────────────────────────────────────────────");
            }

            // ── Check expectation ─────────────────────────────────
            boolean correct = (result == expected);
            if (correct) passed++;
            else         failed++;

            // ── Build summary row ──────────────────────────────────
            summaryRows.add(new String[]{
                    String.valueOf(i + 1),
                    truncate(sentence, 42),
                    truncate(context, 30),
                    result ? "ACCEPTED" : "REJECTED",
                    expected ? "ACCEPTED" : "REJECTED",
                    correct ? "✓" : "✗ UNEXPECTED"
            });
        }

        // ── SUMMARY TABLE (paste this into your report) ────────────
        printBanner("SUMMARY TABLE — copy into report");
        System.out.printf("%-4s  %-43s  %-12s  %-12s%n",
                "#", "Sentence (truncated)", "Result", "Expected");
        System.out.println("─".repeat(75));
        for (String[] row : summaryRows) {
            System.out.printf("%-4s  %-43s  %-12s  %-12s  %s%n",
                    row[0], row[1], row[3], row[4], row[5]);
        }

        System.out.println("─".repeat(75));
        System.out.printf("  Total: %d  |  Accepted: %d  |  Rejected: %d  |  " +
                        "Matched expectation: %d/%d%n%n",
                TESTS.length, accepted, rejected, passed, TESTS.length);

        // ── SCREENSHOT GUIDE ───────────────────────────────────────
        printBanner("SCREENSHOT GUIDE — for your report");
        String[] shots = {
                "SCREENSHOT 1: Run TestCases.java → capture the FULL console output",
                "  → Shows all token tables + accept/reject for report section 'Token Tables'",
                "",
                "SCREENSHOT 2: Test #1 (DROP ME FOR ROND-POINT) — zoom into token table",
                "  → Shows VERB/PRONOUN/PREPOSITION/NOUN/CODE_MIX tokens clearly",
                "  → Include the '✅ ACCEPTED' line below it",
                "",
                "SCREENSHOT 3: Test #9 (CURRENT DON CUT...) — zoom into rejection",
                "  → Shows '❌ REJECTED' + linguistic reason block",
                "  → Caption: 'Multi-clause parataxis — common in Yaoundé oral speech'",
                "",
                "SCREENSHOT 4: Run Main.java → capture the SUMMARY at the bottom",
                "  → Shows global token frequency table",
                "  → Caption: 'NOUN (27%) and VERB (17%) dominate — content-heavy register'",
                "",
                "SCREENSHOT 5: Main.java → capture the LL(1) PARSING TABLE section",
                "  → Use this as your LL(1) table screenshot in the grammar section",
                "",
                "HOW TO TAKE SCREENSHOTS IN INTELLIJ:",
                "  1. Run the file (green play button)",
                "  2. In the 'Run' panel at the bottom, right-click → 'Copy All'",
                "  3. Paste into Notepad to save full output",
                "  4. OR use Windows Snip & Sketch (Win+Shift+S) to capture the screen",
        };
        for (String s : shots) System.out.println(s);

        // ── REPORT SECTION HINTS ───────────────────────────────────
        System.out.println();
        printBanner("REPORT WRITING GUIDE");
        System.out.println(
                "Section: Raw collected statements\n" +
                        "  → Use the 39 sentences in Main.java (SENTENCES array)\n" +
                        "  → Group by topic (taxi, electricity, internet, etc.)\n" +
                        "\n" +
                        "Section: Token tables\n" +
                        "  → Pick 5-6 sentences. Screenshot their token tables from this output.\n" +
                        "  → Include one per topic area.\n" +
                        "\n" +
                        "Section: Regular expressions\n" +
                        "  → Copy the RULES list from Lexer.java (lines starting with RULES.add)\n" +
                        "  → Format each as: TOKEN_TYPE | REGEX | MATCHES\n" +
                        "\n" +
                        "Section: CFG Grammar rules\n" +
                        "  → Original CFG (before transforms) → see Parser.java top comment\n" +
                        "  → After left recursion removal → NP' rules\n" +
                        "  → After left factoring → VP' rules\n" +
                        "\n" +
                        "Section: FIRST & FOLLOW sets\n" +
                        "  → Copy from Parser.java top comment (section 'FIRST SETS' and 'FOLLOW SETS')\n" +
                        "  → Format as a 2-column table\n" +
                        "\n" +
                        "Section: Discussion (most important for marks)\n" +
                        "  → Use the linguistic rejection reasons from this output\n" +
                        "  → Key points:\n" +
                        "     1. Parataxis (clauses chained by comma, not conjunction)\n" +
                        "     2. Pidgin aspect markers ('don + V') function as VP but aren't single VERBs\n" +
                        "     3. Code-mixing makes SUBJECT ambiguous (I go, Na so, etc.)\n" +
                        "     4. Heavy ellipsis (dropped subjects, missing verbs)\n" +
                        "     5. Slang evolution — discourse markers like 'abeg' play multiple roles\n"
        );
    }

    // ── Helpers ──────────────────────────────────────────────────────

    static void printBanner(String title) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════════╗");
        System.out.printf( "║  %-64s║%n", title);
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
    }

    static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}