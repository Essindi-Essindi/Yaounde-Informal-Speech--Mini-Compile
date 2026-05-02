package com.sji.compilation;
import java.util.*;
import java.util.regex.*;

/**
 * Lexer for Yaounde informal speech (Camfranglais/Pidgin).
 * Authors: Abena, Essindi and Yimgaing
 * This class handles lexical analysis by breaking down input strings into tokens.
 */
public class Lexer {

    public enum TokenType {
        SLANG, CODE_MIX, NUMBER, PRONOUN, WH_PRONOUN, FOCUS, AUX, ADV, VERB, NOUN, ADJECTIVE, PREPOSITION, CONJUNCTION, DETERMINER, INTERJECTION, PUNCTUATION, UNKNOWN
    }

    public static class Token {
        public final List<TokenType> types;
        public final String lexeme;
        public Token(List<TokenType> types, String lexeme) { 
            this.types = types; 
            this.lexeme = lexeme; 
        }
        public Token(TokenType type, String lexeme) { 
            this.types = Collections.singletonList(type); 
            this.lexeme = lexeme; 
        }
        @Override public String toString() { 
            return String.format("%-15s | %s", types.toString(), lexeme); 
        }
    }

    private static class TokenRule {
        final TokenType type;
        final Pattern pattern;
        TokenRule(TokenType type, String regex) {
            this.type = type;
            this.pattern = Pattern.compile("^(" + regex + ")", Pattern.CASE_INSENSITIVE);
        }
    }

    private static final List<TokenRule> RULES = new ArrayList<>();

    static {
        RULES.add(new TokenRule(TokenType.PUNCTUATION, "[,\\.!?;:\\-]+"));
        RULES.add(new TokenRule(TokenType.NUMBER, "[0-9]+(k)?|zero[-]?zero"));
        RULES.add(new TokenRule(TokenType.SLANG, "\\bwella\\b|\\bsomehow\\b|\\bwahala\\b|\\bnot\\b|\\bjust\\b|\\btoo\\b|\\bvery\\b|\\bnow\\b|\\bkia-kia\\b"));
        RULES.add(new TokenRule(TokenType.CODE_MIX, "\\bje\\b|\\btu\\b|\\bil\\b|\\bnous\\b|\\bvous\\b|\\bils\\b"));
        RULES.add(new TokenRule(TokenType.PRONOUN, "\\bI\\b|\\bme\\b|\\byou\\b|\\bwe\\b|\\bus\\b|\\bdem\\b|\\bthem\\b|\\be\\b|\\bam\\b|\\buna\\b|\\bmy\\b|\\bour\\b|\\byour\\b|\\btheir\\b|\\bsomebody\\b|\\banybody\\b|\\bnobody\\b|\\ba\\b|\\byu\\b|\\bem\\b"));
        RULES.add(new TokenRule(TokenType.WH_PRONOUN, "\\bwetin\\b|\\bwusai\\b|\\bwho\\b|\\bhow\\b"));
        RULES.add(new TokenRule(TokenType.FOCUS, "\\bna\\b"));
        RULES.add(new TokenRule(TokenType.AUX, "\\bdon\\b|\\bgo\\b|\\bdey\\b|\\bfit\\b|\\bno\\b|\\bnever\\b|\\bwan\\b"));
        RULES.add(new TokenRule(TokenType.ADV, "\\btumoro\\b|\\bnau\\b|\\bhia\\b|\\bdea\\b|\\bsoon\\b|\\balready\\b|\\bagain\\b"));
        RULES.add(new TokenRule(TokenType.VERB, "\\bwaste\\b|\\bdrop\\b|\\bpay\\b|\\bcut\\b|\\bsabi\\b|\\blower\\b|\\bopen\\b|\\bstop\\b|\\bpass\\b|\\bwait\\b|\\bbuy\\b|\\bsell\\b|\\btake\\b|\\bget\\b|\\bcome\\b|\\bsay\\b|\\bdo\\b|\\bwant\\b|\\bknow\\b|\\bread\\b|\\benter\\b|\\bfly\\b|\\bswim\\b|\\bsend\\b|\\bsubmit\\b|\\bwanda\\b|\\bchop\\b|\\bshare\\b|\\bbreak\\b|\\bride\\b|\\bwear\\b|\\bshift\\b|\\bblock\\b|\\bdouble\\b|\\bfry\\b|\\bkill\\b|\\bblow\\b|\\bruin\\b|\\brun\\b|\\btry\\b|\\bstart\\b|\\bfinish\\b|\\bfind\\b|\\bneed\\b|\\bfeel\\b|\\bhear\\b|\\bsee\\b|\\bgive\\b|\\buse\\b|\\bcheck\\b|\\bhelp\\b|\\bkeep\\b|\\blast\\b|\\bbring\\b|\\bfall\\b|\\bput\\b|\\bmake\\b|\\bcall\\b|\\btell\\b|\\blook\\b|\\blive\\b|\\bwalk\\b|\\bwaka\\b|\\bask\\b|\\bown\\b|\\bwork\\b|\\bchange\\b|\\bspend\\b|\\bbecome\\b|\\bdefrost\\b|\\bspoil\\b|\\bnegotiate\\b|\\bjump\\b|\\bstand\\b|\\bmiss\\b|\\breach\\b|\\btie\\b|\\bpull\\b|\\bpush\\b|\\bturn\\b|\\bcommot\\b|\\bchook\\b|\\bbe\\b|\\btire\\b|\\bdie\\b|\\bmisbehave\\b|\\bis\\b|\\bare\\b|\\bwas\\b|\\bwere\\b|\\bgi\\b|\\bkam\\b|\\bkom\\b|\\bcry\\b|\\bsit\\b"));
        RULES.add(new TokenRule(TokenType.DETERMINER, "\\bthis\\b|\\bthe\\b|\\bthat\\b|\\ball\\b|\\bevery\\b|\\beach\\b|\\bsome\\b|\\bany\\b|\\bone\\b|\\btwo\\b|\\bthree\\b|\\bhalf\\b|\\bsame\\b|\\banother\\b|\\bdi\\b|\\bwan\\b"));
        RULES.add(new TokenRule(TokenType.PREPOSITION, "\\bfor\\b|\\bsince\\b|\\bafter\\b|\\bby\\b|\\bfrom\\b|\\bwith\\b|\\blike\\b|\\babout\\b|\\bat\\b|\\bon\\b|\\bin\\b|\\bof\\b|\\bto\\b|\\bnear\\b|\\binside\\b|\\boutside\\b|\\bthrough\\b|\\bper\\b|\\bfo\\b"));
        RULES.add(new TokenRule(TokenType.CONJUNCTION, "\\bbut\\b|\\band\\b|\\bor\\b|\\bif\\b|\\bwhen\\b|\\bso\\b|\\bthen\\b|\\bthat\\b|\\bbecause\\b|\\buntil\\b|\\bwhere\\b|\\bsotey\\b|\\bwey\\b"));
        RULES.add(new TokenRule(TokenType.ADJECTIVE, "\\bsmall\\b|\\bbig\\b|\\bfresh\\b|\\bcold\\b|\\blong\\b|\\bstable\\b|\\bnormal\\b|\\bgood\\b|\\bbad\\b|\\bfull\\b|\\bempty\\b|\\bblack\\b|\\bnew\\b|\\bold\\b|\\bfar\\b|\\bfast\\b|\\bquick\\b|\\bavailable\\b|\\bfinal\\b|\\bregular\\b|\\bbroken\\b|\\bdiscreet\\b|\\bfree\\b|\\bclear\\b|\\bcomplete\\b"));
        RULES.add(new TokenRule(TokenType.INTERJECTION, "\\bekiee\\b|\\babeg\\b|\\bchai\\b|\\bashia\\b|\\bmassa\\b|\\bmofide\\b|\\boh\\b|\\bah\\b|\\bwow\\b|\\beiya\\b|\\bgod\\b|\\bshame\\b"));
        RULES.add(new TokenRule(TokenType.NOUN, "\\bpikin\\b|\\bmugu\\b|\\bnjangi\\b|\\bkaba\\b|\\bmatango\\b|\\btori\\b|\\bbayam-sellam\\b|\\bcaban\\b|\\brond-point\\b|\\bmoto\\b|\\btaxi\\b|[a-zA-Z][a-zA-Z'\\-]*"));
        RULES.add(new TokenRule(TokenType.UNKNOWN, "\\S+"));
    }

    public List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        String remaining = input.trim();
        while (!remaining.isEmpty()) {
            if (remaining.charAt(0) == ' ' || remaining.charAt(0) == '\t') {
                remaining = remaining.substring(1);
                continue;
            }
            boolean matched = false;
            String foundLexeme = null;
            List<TokenType> matchedTypes = new ArrayList<>();

            for (TokenRule rule : RULES) {
                Matcher m = rule.pattern.matcher(remaining);
                if (m.find()) {
                    String lexeme = m.group(1);
                    if (foundLexeme == null) {
                        foundLexeme = lexeme;
                        matchedTypes.add(rule.type);
                    } else if (lexeme.equals(foundLexeme)) {
                        if (rule.type != TokenType.UNKNOWN) {
                            matchedTypes.add(rule.type);
                        }
                    }
                }
            }

            if (foundLexeme != null) {
                if (matchedTypes.size() > 1) {
                    matchedTypes.remove(TokenType.NOUN);
                }
                tokens.add(new Token(matchedTypes, foundLexeme));
                remaining = remaining.substring(foundLexeme.length());
                matched = true;
            }

            if (!matched) {
                tokens.add(new Token(TokenType.UNKNOWN, String.valueOf(remaining.charAt(0))));
                remaining = remaining.substring(1);
            }
        }
        return tokens;
    }

    public void printTokenTable(String sentence, List<Token> tokens) {
        System.out.println("Input: " + sentence);
        System.out.println("------------------------------------------------------");
        for (Token t : tokens) { System.out.printf("%-20s | %-25s%n", t.types, t.lexeme); }
    }

    public Map<TokenType, Integer> countFrequencies(List<Token> tokens) {
        Map<TokenType, Integer> freq = new LinkedHashMap<>();
        for (Token t : tokens) { 
            for (TokenType type : t.types) {
                freq.merge(type, 1, Integer::sum); 
            }
        }
        return freq;
    }
}