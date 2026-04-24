# Yaounde Informal Speech Compiler

**Authors:** Abena, Essindi and Yimgaing

This project is a compiler for Yaounde informal speech (Camfranglais, Pidgin, and slang). It includes a lexer and a parser to analyze the structure of this informal language.

## Overview

The compiler works in two steps:
1. Lexical Analysis: Breaking sentences into tokens.
2. Syntactic Analysis: Checking if the tokens follow the grammar rules.

## Grammar Rules (CFG)

The compiler uses a Context-Free Grammar (CFG) that has been specifically adapted to handle the parataxis and code-switching found in Yaounde informal speech. To enable LL(1) parsing, the grammar has been left-factored and left-recursion has been removed.

### 1. Sentence Structure (S)
The entry point of the grammar handles various ways a sentence can start in informal speech:
- **S → INTERJECTION S**: Handles opening interjections like "Abeg, ..." or "Ekiee, ...".
- **S → SLANG S**: Handles discourse markers or negation at the start.
- **S → VP**: Handles imperative sentences (commands) where the subject is dropped (e.g., "Drop me for carrefour").
- **S → NP VP**: The standard subject-predicate structure.

### 2. Noun Phrases (NP)
The noun phrase handles subjects and objects, including code-mixed elements:
- **NP → DETERMINER [NOUN|ADJECTIVE] NP'**: Handles "this taxi", "that big pikin".
- **NP → NOUN NP'**: Handles "Taxi", "Chauffeur".
- **NP → PRONOUN NP'**: Handles "I", "you", "dem".
- **NP → CODE_MIX**: Handles specifically recognized mixed-language chunks.
- **NP' → NOUN NP' | ε**: Allows for noun chaining (e.g., "taxi driver") and terminates the phrase.

### 3. Verb Phrases (VP)
The verb phrase handles actions and their modifiers:
- **VP → SLANG VP**: Handles aspect markers and negation (e.g., "no sabi", "don finish").
- **VP → VERB VP'**: The main verb followed by its complements.
- **VP' (Complements)**: A recursive rule that handles a sequence of:
    - **NP VP'**: Direct objects.
    - **PP VP'**: Prepositional phrases.
    - **ADJECTIVE VP'**: Predicate adjectives.
    - **CONJUNCTION S**: Clause joining (limited support).
    - **VERB VP'**: Chained verbs (serial verb constructions).
    - **ε**: End of the verb phrase.

### 4. Prepositional Phrases (PP)
- **PP → PREPOSITION [NP | ADJECTIVE | NUMBER]**: Handles "for rond-point", "since morning", "na 200".

## Lexer

The lexer uses regular expressions to find tokens like:
- Slang: wella, wahala.
- Pronouns: I, dem, e.
- Verbs: pay, sabi, don.
- Nouns: taxi, rond-point.
- Prepositions: for, since.

## Parser

The parser is implemented using a recursive descent approach. It is an LL(1) parser, meaning it reads tokens from Left to right, performs a Leftmost derivation, and uses 1 token of lookahead.

### LL(1) Parsing Table
To ensure the grammar is LL(1) compatible, we checked that for every non-terminal and current token, there is only one possible rule to apply. For example:
- If the current token is a **NOUN**, and we are parsing **S**, we must go to **NP VP**.
- If the current token is a **VERB**, and we are parsing **S**, we go directly to **VP**.

This deterministic approach makes the compiler fast and efficient, though it requires the grammar to be carefully structured to avoid ambiguity.

## Running the Project

You can run the project using the following files:
- ParserApplication.java: Processes a large set of sample sentences.
- TestCases.java: Runs specific tests and shows if they are accepted or rejected.
