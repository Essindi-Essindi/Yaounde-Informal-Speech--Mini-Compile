package com.sji.compilation;
import java.util.*;

/**
 * Test cases for the Lexer and Parser. Sentences collected from real day to day conversations.
 * Authors: Abena, Essindi and Yimgaing
 */
public class TestCases {

    static final Object[][] TESTS = {
            {
                    "Drop me for rond-point, I go pay you after",
                    "Taxi - passenger to driver",
                    true,
                    null
            },
            {
                    "I dey try submit assignment online",
                    "Internet - student",
                    true,
                    null
            },
            {
                    "Somebody share hotspot abeg",
                    "Internet - asking for hotspot",
                    true,
                    null
            },
            {
                    "Patron lower am small",
                    "Market - negotiation",
                    true,
                    null
            },
            {
                    "You dey sell spoil thing",
                    "Market - complaint",
                    true,
                    null
            },
            {
                    "Na so dem dey do for this Yaounde",
                    "General commentary",
                    true,
                    null
            },
            {
                    "My umbrella fly go since that wind blow",
                    "Rain - storm damage",
                    true,
                    null
            },
            {
                    "You want change your money",
                    "Roadside - money changer",
                    true,
                    null
            },

            // REJECTED SENTENCES
            {
                    "Current don cut since morning, I no sabi wetin ENEO dey do with our money",
                    "Electricity - comma joined clauses",
                    false,
                    "Sentence has multiple independent clauses. The grammar handles one clause at a time."
            },
            {
                    "Abeg shift small for seat, you dey sit like say na you buy the taxi",
                    "Taxi - space dispute",
                    false,
                    "Multi-clause sentence. Also 'Abeg' is treated as slang instead of an interjection in this context."
            },
            {
                    "Light don go again! Na every night dem dey do this thing, je wanda",
                    "Electricity - repeated outage",
                    false,
                    "The parser finds issue with Pidgin aspect markers like 'don' when used in this specific structure."
            },
            {
                    "MTN dey do me somehow, data finish but e say network unavailable",
                    "Internet - network issue",
                    false,
                    "Clause boundary problem. The grammar doesn't fully support the 'but' transition between these specific clauses."
            },
            {
                    "Final price be wetin? I no get time dey bargain up and down today",
                    "Market - bargain",
                    false,
                    "Sentences starting with adjectives like 'Final' are not handled by the current grammar."
            },
            {
                    "Essence don finish for every station, queue long reach three street",
                    "Fuel - scarcity",
                    false,
                    "Multi-clause structure and Pidgin tense markers aren't fully mapped to the grammar yet."
            },
            {
                    "Rain don start, road don become river, my shoe don finish",
                    "Rain - flooding",
                    false,
                    "Three independent clauses. The grammar lacks rules for this level of parataxis."
            }
    };

    public static void main(String[] args) {
        Lexer lexer = new Lexer();
        Parser parser = new Parser(false);
        int accepted = 0;
        int rejected = 0;

        printBanner("Compiler Testing");

        for (int i = 0; i < TESTS.length; i++) {
            String sentence = (String) TESTS[i][0];
            String context = (String) TESTS[i][1];
            boolean expected = (boolean) TESTS[i][2];
            String reason = (String) TESTS[i][3];

            System.out.println("TEST #" + (i + 1) + ": " + context);
            List<Lexer.Token> tokens = lexer.tokenize(sentence);
            boolean result = parser.parse(tokens);

            if (result) {
                System.out.println("RESULT: ACCEPTED");
                accepted++;
            } else {
                System.out.println("RESULT: REJECTED");
                if (reason != null) System.out.println("REASON: " + reason);
                rejected++;
            }
            System.out.println("------------------------------------------------------");
        }

        System.out.printf("Total: %d | Accepted: %d | Rejected: %d%n", TESTS.length, accepted, rejected);
    }

    static void printBanner(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}
