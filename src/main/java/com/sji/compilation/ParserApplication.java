package com.sji.compilation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.*;

/**
 * Main application class that drives the compiler pipeline.
 * Authors: Abena, Essindi and Yimgaing
 */
@SpringBootApplication
public class ParserApplication {

    static final String[][] SENTENCES = {
        {"Drop me for rond point, I go pay you after, driver no dey waste my time.", "Taxi passenger to driver"},
        {"Chauffeur, you no fit go Mvog Ada? Na only Biyem Asoa you dey do?", "Taxi passenger asking destination"},
        {"Place available? I dey go Melen, na 200 I get, no change.", "Taxi negotiating fare"},
        {"This taxi no get AC, sun dey kill person for inside, abeg open window.", "Taxi complaint about conditions"},
        {"Driver stop here! You don pass my own stop, I go pay half only.", "Taxi missed stop dispute"},
        {"E get embouteillage for carrefour, we go wait reach when? Ekiee!", "Taxi traffic frustration"},
        {"Na so dem dey do for this Yaounde, one taxi, ten passenger, zero comfort.", "Taxi general commentary"},
        {"Abeg shift small for seat, you dey sit like say na you buy the taxi.", "Taxi space dispute between passengers"},
        {"Current don cut since morning, I no sabi wetin ENEO dey do with our money.", "Electricity outage complaint"},
        {"Light don go again! Na every night dem dey do this thing, je wanda.", "Electricity repeated outage"},
        {"My phone don die, generator no dey, ENEO go kill us here so.", "Electricity no power and no backup"},
        {"Dem say light go come 6pm, e don reach 10pm, nothing. Classic ENEO.", "Electricity broken promise"},
        {"Abeg, anybody get powerbank? This load shedding don tire me wella.", "Electricity asking for powerbank"},
        {"Na so we dey live, light go, light come, no schedule, no apology.", "Electricity resignation commentary"},
        {"The freezer don defrost, all my fish don spoil. ENEO must pay me back!", "Electricity economic damage"},
        {"Since dem do that transformer work for our quartier, current never stable again.", "Electricity infrastructure complaint"},
        {"MTN dey do me somehow, data finish but e say network unavailable. Wetin?", "Internet MTN network issue"},
        {"I dey try submit assignment online, connection no dey, deadline na today. Help!", "Internet student deadline crisis"},
        {"Orange better pass MTN for data but the two useless when rain fall.", "Internet network comparison"},
        {"Prof say make I send the work by email, but since morning my 4G na just H plus.", "Internet 4G degraded"},
        {"Somebody share hotspot abeg, I go buy data tomorrow, na emergency.", "Internet asking for hotspot"},
        {"Patron, lower am small, na 500 I get for pocket, no be lie.", "Market price negotiation"},
        {"Madame, this tomato na 1000 for three? Last week na 500. Wetin happen?", "Market price increase complaint"},
        {"I go take everything if you do me good price, na regular customer I be.", "Market bulk deal negotiation"},
        {"You dey sell spoil thing! Look this plantain, e don black for inside!", "Market quality dispute"},
        {"Final price be wetin? I no get time dey bargain up and down today.", "Market closing the deal"},
        {"Rain don start, road don become river, my shoe don finish. Yaounde no try.", "Rain flooding commentary"},
        {"Abeg enter inside quick, this rain no be normal rain, na flood e be.", "Rain flood warning"},
        {"My umbrella fly go since that wind blow, I reach class like say I swim.", "Rain storm damage"},
        {"Every year same thing, rain fall, road block, government do nothing. Ekiee!", "Rain recurring infrastructure failure"},
        {"Essence don finish for every station, queue long reach three street.", "Fuel scarcity"},
        {"Black market fuel na 1000 per litre now, dem wan kill us with this thing.", "Fuel black market price spike"},
        {"My bike don stop for road, tank empty, no station dey near here. God help me.", "Fuel stranded on road"},
        {"Patron! Phone case, charger, earpiece, all dey here, come check am.", "Roadside vendor pitch"},
        {"You want change your money? Dollar, Euro, CFA, I get everything, good rate.", "Roadside money changer"},
        {"Buy cold water for me na, sun don fry my head for this place.", "Roadside heat complaint"},
        {"Bendskin, you know Quartier Nkolbisson? Take me there, how much you want?", "Bendskin destination negotiation"},
        {"Ride me fast, I dey late for work, but no kill me for road abeg!", "Bendskin speed vs safety"},
        {"This one na 300, Moto man no dey negotiate. Take am or find another.", "Bendskin fixed price refusal"}
    };

    public static void main(String[] args) {
        Lexer lexer = new Lexer();
        Parser parser = new Parser(false);
        int accepted = 0;
        int rejected = 0;
        Map<Lexer.TokenType, Integer> globalFreq = new LinkedHashMap<>();

        System.out.println("=== Yaounde Informal Speech - Compiler Results ===");

        for (int i = 0; i < SENTENCES.length; i++) {
            String sentence = SENTENCES[i][0];
            String context = SENTENCES[i][1];
            System.out.println("Sentence #" + (i + 1) + " [" + context + "]");
            List<Lexer.Token> tokens = lexer.tokenize(sentence);
            lexer.printTokenTable(sentence, tokens);
            Map<Lexer.TokenType, Integer> freq = lexer.countFrequencies(tokens);
            for (Map.Entry<Lexer.TokenType, Integer> e : freq.entrySet()) {
                globalFreq.merge(e.getKey(), e.getValue(), Integer::sum);
            }
            if (parser.parse(tokens)) {
                System.out.println("Parser: ACCEPTED");
                accepted++;
            } else {
                System.out.println("Parser: REJECTED");
                rejected++;
            }
        }

        System.out.println("\n=== Overall Summary ===");
        System.out.println("Total: " + SENTENCES.length);
        System.out.println("Accepted: " + accepted);
        System.out.println("Rejected: " + rejected);

        SpringApplication.run(ParserApplication.class, args);
    }
}
