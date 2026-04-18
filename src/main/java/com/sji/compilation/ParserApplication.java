package com.sji.compilation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.*;

/**
 * ============================================================
 *  MAIN — Compiler Pipeline Driver
 * ============================================================
 *
 * This class ties the Lexer and Parser together.
 * It runs every collected sentence through both phases and
 * prints a full report: token tables, frequency counts,
 * and accept/reject results.
 *
 * RUN THIS CLASS to see the full compiler in action.
 *
 * Compile:  javac *.java
 * Run:      java Main
 */
@SpringBootApplication
public class ParserApplication {

    // ────────────────────────────────────────────────────────────────
    //  ALL COLLECTED SENTENCES — real Yaoundé informal speech
    //  (from field data collection — transcribed verbatim)
    // ────────────────────────────────────────────────────────────────
    static final String[][] SENTENCES = {
            // {sentence, source/context}
            {"Drop me for rond-point, I go pay you after, driver no dey waste my time.",
                    "Taxi – passenger to driver"},
            {"Chauffeur, you no fit go Mvog-Ada? Na only Biyem-Asoa you dey do?",
                    "Taxi – passenger asking destination"},
            {"Place available? I dey go Melen, na 200 I get, no change.",
                    "Taxi – negotiating fare"},
            {"This taxi no get AC, sun dey kill person for inside, abeg open window.",
                    "Taxi – complaint about conditions"},
            {"Driver stop here! You don pass my own stop, I go pay half only.",
                    "Taxi – missed stop dispute"},
            {"E get embouteillage for carrefour, we go wait reach when? Ekiee!",
                    "Taxi – traffic frustration"},
            {"Na so dem dey do for this Yaoundé — one taxi, ten passenger, zero comfort.",
                    "Taxi – general commentary"},
            {"Abeg shift small for seat, you dey sit like say na you buy the taxi.",
                    "Taxi – space dispute between passengers"},

            {"Current don cut since morning, I no sabi wetin ENEO dey do with our money.",
                    "Electricity – outage complaint"},
            {"Light don go again! Na every night dem dey do this thing, je wanda.",
                    "Electricity – repeated outage"},
            {"My phone don die, generator no dey, ENEO go kill us here so.",
                    "Electricity – no power and no backup"},
            {"They say light go come 6pm, e don reach 10pm, nothing. Classic ENEO.",
                    "Electricity – broken promise"},
            {"Abeg, anybody get powerbank? This load shedding don tire me wella.",
                    "Electricity – asking for powerbank"},
            {"Na so we dey live — light go, light come, no schedule, no apology.",
                    "Electricity – resignation commentary"},
            {"The freezer don defrost, all my fish don spoil. ENEO must pay me back!",
                    "Electricity – economic damage"},
            {"Since dem do that transformer work for our quartier, current never stable again.",
                    "Electricity – infrastructure complaint"},

            {"MTN dey do me somehow, data finish but e say network unavailable. Wetin?",
                    "Internet – MTN network issue"},
            {"I dey try submit assignment online, connection no dey, deadline na today. Help!",
                    "Internet – student deadline crisis"},
            {"Orange better pass MTN for data but the two useless when rain fall.",
                    "Internet – network comparison"},
            {"Prof say send the work by email, but since morning my 4G na just H+.",
                    "Internet – 4G degraded to H+"},
            {"Somebody share hotspot abeg, I go buy data tomorrow, na emergency.",
                    "Internet – asking for hotspot"},

            {"Patron, lower am small, na 500 I get for pocket, no be lie.",
                    "Market – price negotiation"},
            {"Madame, this tomato na 1000 for three? Last week na 500. Wetin happen?",
                    "Market – price increase complaint"},
            {"I go take everything if you do me good price — na regular customer I be.",
                    "Market – bulk deal negotiation"},
            {"You dey sell spoil thing! Look this plantain, e don black for inside!",
                    "Market – quality dispute"},
            {"Final price be wetin? I no get time dey bargain up and down today.",
                    "Market – closing the deal"},

            {"Rain don start, road don become river, my shoe don finish. Yaoundé no try.",
                    "Rain – flooding commentary"},
            {"Abeg enter inside quick, this rain no be normal rain — na flood e be.",
                    "Rain – flood warning"},
            {"My umbrella fly go since that wind blow, I reach class like say I swim.",
                    "Rain – storm damage"},
            {"Every year same thing — rain fall, road block, government do nothing. Ekiee!",
                    "Rain – recurring infrastructure failure"},

            {"Essence don finish for every station, queue long reach three street.",
                    "Fuel – scarcity"},
            {"Black market fuel na 1000 per litre now — dem wan kill us with this thing.",
                    "Fuel – black market price spike"},
            {"My bike don stop for road, tank empty, no station dey near here. God help me.",
                    "Fuel – stranded on road"},

            {"Patron! Phone case, charger, earpiece — all dey here, come check am.",
                    "Roadside vendor – pitch"},
            {"You want change your money? Dollar, Euro, CFA — I get everything, good rate.",
                    "Roadside – money changer"},
            {"Buy cold water for me na, sun don fry my head for this place.",
                    "Roadside – heat complaint / purchase"},

            {"Bendskin, you know Quartier Nkolbisson? Take me there, how much you want?",
                    "Bendskin – destination negotiation"},
            {"Ride me fast, I dey late for work, but no kill me for road abeg!",
                    "Bendskin – speed vs safety"},
            {"This one na 300, Moto-man no dey negotiate. Take or find another.",
                    "Bendskin – fixed price refusal"},
    };

    // ────────────────────────────────────────────────────────────────
    //  MAIN METHOD
    // ────────────────────────────────────────────────────────────────
    public static void main(String[] args) {

        Lexer lexer   = new Lexer();
        Parser parser = new Parser(false);  // set true to see step-by-step parse trace

        // ── Summary counters ──────────────────────────────────────
        int accepted = 0;
        int rejected = 0;
        Map<Lexer.TokenType, Integer> globalFreq = new LinkedHashMap<>();

        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║     YAOUNDÉ INFORMAL SPEECH — COMPILER PIPELINE RESULTS     ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        // ── Process each sentence ─────────────────────────────────
        for (int i = 0; i < SENTENCES.length; i++) {
            String sentence = SENTENCES[i][0];
            String context  = SENTENCES[i][1];

            System.out.println("\n══════════════════════════════════════════════════════════════");
            System.out.printf("  SENTENCE #%d  [%s]%n", i + 1, context);
            System.out.println("══════════════════════════════════════════════════════════════");

            // ── PHASE 1: LEXICAL ANALYSIS ─────────────────────────
            List<Lexer.Token> tokens = lexer.tokenize(sentence);
            lexer.printTokenTable(sentence, tokens);

            // accumulate frequency counts
            Map<Lexer.TokenType, Integer> freq = lexer.countFrequencies(tokens);
            for (Map.Entry<Lexer.TokenType, Integer> e : freq.entrySet()) {
                globalFreq.merge(e.getKey(), e.getValue(), Integer::sum);
            }

            // ── PHASE 2: SYNTACTIC ANALYSIS ───────────────────────
            boolean ok = parser.parse(tokens);

            if (ok) {
                System.out.println("\n  ✅  PARSER RESULT: ACCEPTED — sentence fits the grammar.");
                accepted++;
            } else {
                System.out.println("\n  ❌  PARSER RESULT: REJECTED — see parse log below.");
                // print the last few log entries for debugging
                List<String> log = parser.getParseLog();
                int start = Math.max(0, log.size() - 4);
                for (int j = start; j < log.size(); j++) {
                    System.out.println("     " + log.get(j));
                }
                rejected++;
            }
        }

        // ── GLOBAL SUMMARY ────────────────────────────────────────
        System.out.println("\n\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    OVERALL SUMMARY                          ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Total sentences   : %-4d                                   ║%n",
                SENTENCES.length);
        System.out.printf("║  Accepted          : %-4d (%.0f%%)                              ║%n",
                accepted, 100.0 * accepted / SENTENCES.length);
        System.out.printf("║  Rejected          : %-4d (%.0f%%)                              ║%n",
                rejected, 100.0 * rejected / SENTENCES.length);
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  TOKEN FREQUENCY ACROSS ALL SENTENCES                       ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");

        // sort by count descending
        globalFreq.entrySet()
                .stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .forEach(e -> System.out.printf("║  %-20s : %-5d                             ║%n",
                        e.getKey(), e.getValue()));

        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        // ── GRAMMAR REFERENCE TABLE ───────────────────────────────
        printGrammarReference();
        printLL1Table();
        SpringApplication.run(ParserApplication.class, args);
    }

    // ────────────────────────────────────────────────────────────────
    //  PRINT GRAMMAR REFERENCE (for the report / screenshot)
    // ────────────────────────────────────────────────────────────────
    static void printGrammarReference() {
        System.out.println("\n\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║          CONTEXT-FREE GRAMMAR (after transformations)        ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        String[] rules = {
                "S   → VP                          (imperative)",
                "S   → INTERJECTION S              (interjection prefix)",
                "S   → SLANG S                     (slang/discourse prefix)",
                "S   → CODE_MIX VP                 (code-mix subject)",
                "S   → NP VP                       (full subject + predicate)",
                "NP  → DETERMINER NOUN NP'",
                "NP  → NOUN NP'",
                "NP  → PRONOUN NP'",
                "NP  → CODE_MIX",
                "NP' → NOUN NP'                    (right-recursive, no left recursion)",
                "NP' → ε",
                "VP  → VERB VP'",
                "VP' → NP VP'",
                "VP' → PP VP'",
                "VP' → ADJECTIVE VP'",
                "VP' → SLANG VP'",
                "VP' → CONJUNCTION VP'",
                "VP' → NUMBER VP'",
                "VP' → VERB VP'                    (chained verb)",
                "VP' → ε",
                "PP  → PREPOSITION NP",
                "PP  → PREPOSITION ADJECTIVE",
                "PP  → PREPOSITION NUMBER",
        };
        for (String r : rules) {
            System.out.printf("║  %-58s ║%n", r);
        }
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    // ────────────────────────────────────────────────────────────────
    //  PRINT LL(1) PARSING TABLE  (rows=non-terminals, cols=terminals)
    // ────────────────────────────────────────────────────────────────
    static void printLL1Table() {
        System.out.println("\n\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    LL(1) PARSING TABLE                      ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  Non-Terminal | Input Token       | Rule to Apply            ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");

        String[][] table = {
                {"S",   "VERB",         "S → VP"},
                {"S",   "INTERJECTION", "S → INTERJECTION S"},
                {"S",   "SLANG",        "S → SLANG S"},
                {"S",   "CODE_MIX",     "S → CODE_MIX VP"},
                {"S",   "NOUN",         "S → NP VP"},
                {"S",   "PRONOUN",      "S → NP VP"},
                {"S",   "DETERMINER",   "S → NP VP"},
                {"NP",  "DETERMINER",   "NP → DET NOUN NP'"},
                {"NP",  "NOUN",         "NP → NOUN NP'"},
                {"NP",  "PRONOUN",      "NP → PRONOUN NP'"},
                {"NP",  "CODE_MIX",     "NP → CODE_MIX"},
                {"NP'", "NOUN",         "NP' → NOUN NP'"},
                {"NP'", "VERB",         "NP' → ε"},
                {"NP'", "PREPOSITION",  "NP' → ε"},
                {"NP'", "$",            "NP' → ε"},
                {"VP",  "VERB",         "VP → VERB VP'"},
                {"VP'", "PREPOSITION",  "VP' → PP VP'"},
                {"VP'", "ADJECTIVE",    "VP' → ADJECTIVE VP'"},
                {"VP'", "SLANG",        "VP' → SLANG VP'"},
                {"VP'", "CONJUNCTION",  "VP' → CONJUNCTION VP'"},
                {"VP'", "NUMBER",       "VP' → NUMBER VP'"},
                {"VP'", "NOUN",         "VP' → NP VP'"},
                {"VP'", "PRONOUN",      "VP' → NP VP'"},
                {"VP'", "DETERMINER",   "VP' → NP VP'"},
                {"VP'", "VERB",         "VP' → VERB VP'"},
                {"VP'", "$",            "VP' → ε"},
                {"PP",  "PREPOSITION",  "PP → PREP NP/ADJ/NUM"},
        };

        for (String[] row : table) {
            System.out.printf("║  %-13s | %-17s | %-24s ║%n", row[0], row[1], row[2]);
        }

        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println("\n  $ = end of input (EOF)");
        System.out.println("  ε = empty (the rule produces nothing — do not consume any token)");
    }
}