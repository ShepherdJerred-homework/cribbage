// cribbage
// Jerred Shepherd

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class cribbage {
    public static void main(String[] args) throws FileNotFoundException {
        LinkedList<Input> inputs = getInput();
        LinkedList<Solution> solutions = solveInputs(inputs);
        printSolutions(solutions);
    }

    private static LinkedList<Input> getInput() throws FileNotFoundException {
        File file = new File("cribbage.in");
        Scanner scanner = new Scanner(file);

        LinkedList<Input> inputs = new LinkedList<>();

        String line;
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            if (line.contains("0")) {
                break;
            }
            inputs.add(stringToInput(line));
        }

        return inputs;
    }

    public static void printSolutions(LinkedList<Solution> solutions) throws FileNotFoundException {
        File file = new File("cribbage.out");
        PrintWriter printWriter = new PrintWriter(file);

        solutions.forEach(solution -> {
            System.out.println(solution.handValue);
            printWriter.println(solution.handValue);
        });

        printWriter.close();
    }

    public static LinkedList<Solution> solveInputs(LinkedList<Input> inputs) {
        LinkedList<Solution> solutions = new LinkedList<>();
        inputs.forEach(input -> solutions.add(solveInput(input)));
        return solutions;
    }

    // TODO
    public static Solution solveInput(Input input) {
        return new Solution(5);
    }

    public static Input stringToInput(String string) {
        LinkedList<Card> cards = Stream.of(string)
                .map(s -> s.split(" "))
                .flatMap(Arrays::stream)
                .map(cribbage::stringToCard)
                .collect(Collectors.toCollection(LinkedList::new));
        return new cribbage.Input(cards);
    }

    public static Card stringToCard(String string) {
        char[] chars = string.toCharArray();
        Card.Value value = charToValue(chars[0]);
        Card.Suit suit = charToSuit(chars[1]);
        return new Card(value, suit);
    }

    public static Card.Suit charToSuit(char c) {
        switch (c) {
            case 'C':
                return Card.Suit.CLUB;
            case 'D':
                return Card.Suit.DIAMOND;
            case 'H':
                return Card.Suit.HEART;
            case 'S':
                return Card.Suit.SPADE;
            default:
                throw new IllegalArgumentException(String.format("Cannot convert %s to a suit", c));
        }
    }

    public static Card.Value charToValue(char c) {
        switch (c) {
            case 'A':
                return Card.Value.ACE;
            case '2':
                return Card.Value.TWO;
            case '3':
                return Card.Value.THREE;
            case '4':
                return Card.Value.FOUR;
            case '5':
                return Card.Value.FIVE;
            case '6':
                return Card.Value.SIX;
            case '7':
                return Card.Value.SEVEN;
            case '8':
                return Card.Value.EIGHT;
            case '9':
                return Card.Value.NINE;
            case 'T':
                return Card.Value.TEN;
            case 'J':
                return Card.Value.JACK;
            case 'Q':
                return Card.Value.QUEEN;
            case 'K':
                return Card.Value.KING;
            default:
                throw new IllegalArgumentException(String.format("Cannot convert %s to a value", c));
        }
    }

    public static class Input {
        LinkedList<Card> cards;

        public Input(LinkedList<Card> cards) {
            this.cards = cards;
        }

        @Override
        public String toString() {
            return "Input{" +
                    "cards=" + cards +
                    '}';
        }
    }

    public static class Solution {
        int handValue;

        public Solution(int handValue) {
            this.handValue = handValue;
        }
    }

    public static class Card {
        Value value;
        Suit suit;

        Card(Value value, Suit suit) {
            this.value = value;
            this.suit = suit;
        }

        enum Suit {
            CLUB, DIAMOND, HEART, SPADE
        }

        enum Value {
            ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING
        }

        @Override
        public String toString() {
            return "Card{" +
                    "value=" + value +
                    ", suit=" + suit +
                    '}';
        }
    }
}
