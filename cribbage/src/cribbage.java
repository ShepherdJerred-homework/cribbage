// cribbage
// Jerred Shepherd

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * Abandon hope all ye who enter here
 * Cases 12 and 13 are still not passing, not sure why
 */
public class cribbage {

    private static final int NUMBER_OF_POINTS_PER_COMBINATION_EQUAL_TO_FIFTEEN = 2;
    private static final int NUMBER_OF_POINTS_FOR_ALL_CARDS_SAME_SUIT = 5;
    private static final int NUMBER_OF_POINTS_FOR_HAND_SAME_SUIT = 4;
    private static final int NUMBER_OF_POINTS_PER_PAIR = 2;

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

    private static void printSolutions(LinkedList<Solution> solutions) throws FileNotFoundException {
        File file = new File("cribbage.out");
        PrintWriter printWriter = new PrintWriter(file);

        int counter = 1;
        for (Solution solution : solutions) {
            System.out.println(String.format("Case #%s", counter));
            System.out.println(solution.toString());
            printWriter.println(solution.getTotalPoints());
            counter += 1;
        }

        printWriter.close();
    }

    private static LinkedList<Solution> solveInputs(LinkedList<Input> inputs) {
        LinkedList<Solution> solutions = new LinkedList<>();
        inputs.forEach(input -> solutions.add(solveInput(input)));
        return solutions;
    }

    private static Solution solveInput(Input input) {
        int pointsForPairs = calculatePointsForPairs(input);
        int pointsForFlushes = calculatePointsForFlushes(input);
        int pointsForRuns = calculatePointsForRuns(input);
        int pointsForFifteens = calculatePointsForFifteens(input);
        int pointsForNobs = calculatePointsForNobs(input);

        return new Solution(
                pointsForPairs,
                pointsForFlushes,
                pointsForRuns,
                pointsForFifteens,
                pointsForNobs
        );
    }

    private static int calculatePointsForNobs(Input input) {
        List<Card> hand = input.hand;
        Card starter = input.starter;

        for (Card card : hand) {
            if (card.value == Card.Value.JACK && card.suit == starter.suit) {
                return 1;
            }
        }

        return 0;
    }

    private static int calculatePointsForFifteens(Input input) {
        List<Card> cards = input.cards;

        int fifteens = 0;
        for (int i = 0; i < cards.size(); i++) {
            fifteens += fifteen(cards, i, 0);
        }

        return fifteens * NUMBER_OF_POINTS_PER_COMBINATION_EQUAL_TO_FIFTEEN;
    }

    private static int fifteen(List<Card> cards, int index, int acc) {
        Card card = cards.get(index);
        int cardValue = card.getValueAsInt(false);
        int comb = acc + cardValue;

//        System.out.println(String.format("card: %s\nindex: %s\nacc: %s", card, index, acc));

        if (comb == 15) {
//            System.out.println(String.format("fifteen on %s", card));
            return 1;
        } else if (comb < 15) {
            int fifteens = 0;
            // keep going, merge answers
            for (int i = index + 1; i < cards.size(); i++) {
                fifteens += fifteen(cards, i, comb);
            }
            return fifteens;
        } else {
            // too high
            return 0;
        }
    }

    private static int calculatePointsForRuns(Input input) {
        LinkedList<Card> cards = input.cards;

        List<RunInfo> allRuns = new ArrayList<>();
        int bestRunLength = 0;

        // iterate over all cards, let them find their largest runs
        for (int i = 0; i < cards.size(); i++) {
            RunInfo run = findBestRunStartingAtCard(cards, i);
            allRuns.add(run);

//            System.out.println(String.format("Card: %s\nRun:%s", cards.get(i), run));

            // only replace run if it is better
            if (bestRunLength == 0 || run.runLength > bestRunLength) {
                bestRunLength = run.runLength;
            }
        }

        int totalNumberOfRunsOfBestLength = 0;
        for (RunInfo run : allRuns) {
            if (run.runLength == bestRunLength) {
                totalNumberOfRunsOfBestLength += run.numberOfRuns;
            }
        }

        if (bestRunLength >= 3) {
            return totalNumberOfRunsOfBestLength * bestRunLength;
        } else {
            return 0;
        }
    }

    private static RunInfo findBestRunStartingAtCard(LinkedList<Card> cards, int index) {
        Card card = cards.get(index);
        int cardValue = card.getValueAsInt(true);

        // If we're at the very last card, let's return a run of '1'
        if (index != cards.size() - 1) {
            List<RunInfo> runsAfterThisNumber = new ArrayList<>();

            int bestRunLength = 0;

            // Find best runs starting at the next card
            for (int i = index + 1; i < cards.size(); i++) {
                Card nextCard = cards.get(i);
                int nextCardValue = nextCard.getValueAsInt(true);

                // iterate over cards that have a value one more than this one
                if (nextCardValue == cardValue + 1) {
                    RunInfo run = findBestRunStartingAtCard(cards, i);
                    if (run.runLength > bestRunLength) {
                        bestRunLength = run.runLength;
                    }
                    runsAfterThisNumber.add(run);
                } else if (nextCardValue == cardValue) {
                    continue;
                } else {
                    break;
                }
            }

            if (bestRunLength != 0) {
                RunInfo runInfo = new RunInfo(bestRunLength + 1, 0);
                for (RunInfo run : runsAfterThisNumber) {
                    // merge all runs that match the best
                    if (run.runLength == bestRunLength) {
                        runInfo.numberOfRuns += 1;
                    }
                }
                return runInfo;
            } else {
                return new RunInfo(1, 1);
            }
        } else {
            return new RunInfo(1, 1);
        }
    }

    private static int calculatePointsForFlushes(Input input) {
        List<Card> hand = input.hand;
        Card starter = input.starter;

        Card.Suit suit = null;
        for (Card card : hand) {
            if (suit == null) {
                suit = card.suit;
            } else {
                if (card.suit != suit) {
//                    System.out.println(String.format("%s | %s", card.suit, suit));
                    return 0;
                }
            }
        }

        if (suit == starter.suit) {
            return NUMBER_OF_POINTS_FOR_ALL_CARDS_SAME_SUIT;
        } else {
            return NUMBER_OF_POINTS_FOR_HAND_SAME_SUIT;
        }
    }

    private static int calculatePointsForPairs(Input input) {
        Set<Pair> pairs = input.pairs;

        int numberOfPairs = Math.toIntExact(pairs.stream().filter(Pair::isPairSameValue).count());

        return numberOfPairs * NUMBER_OF_POINTS_PER_PAIR;
    }

    private static Input stringToInput(String string) {
        LinkedList<Card> cards = Stream.of(string)
                .map(s -> s.split(" "))
                .flatMap(Arrays::stream)
                .map(cribbage::stringToCard)
                .collect(Collectors.toCollection(LinkedList::new));
        return new cribbage.Input(cards);
    }

    private static Card stringToCard(String string) {
        char[] chars = string.toCharArray();
        Card.Value value = charToValue(chars[0]);
        Card.Suit suit = charToSuit(chars[1]);
        return new Card(value, suit);
    }

    private static Card.Suit charToSuit(char c) {
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

    private static Card.Value charToValue(char c) {
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
                throw new IllegalArgumentException(String.format("Cannot convert %s to a card", c));
        }
    }

    private static class Input {
        LinkedList<Card> cards;
        List<Card> hand;
        Card starter;
        Set<Pair> pairs;

        public Input(LinkedList<Card> cards) {
            this.cards = cards;
            this.hand = new ArrayList<>(cards).subList(0, cards.size() - 1);
            this.starter = cards.getLast();
            cards.sort(Comparator.comparingInt(c -> c.getValueAsInt(true)));

            pairs = new HashSet<>();
            for (int inner = 0; inner < cards.size(); inner++) {
                for (int outer = 0; outer < cards.size(); outer++) {
                    if (inner == outer || inner < outer) {
                        continue;
                    }
                    pairs.add(new Pair(cards.get(inner), cards.get(outer)));
                }
            }
        }

        @Override
        public String toString() {
            return "Input{" +
                    "cards=" + cards +
                    '}';
        }
    }

    private static class Solution {
        int pointsForPairs;
        int pointsForFlushes;
        int pointsForRuns;
        int pointsForFifteens;
        int pointsForNobs;

        public Solution(int pointsForPairs, int pointsForFlushes, int pointsForRuns, int pointsForFifteens, int pointsForNobs) {
            this.pointsForPairs = pointsForPairs;
            this.pointsForFlushes = pointsForFlushes;
            this.pointsForRuns = pointsForRuns;
            this.pointsForFifteens = pointsForFifteens;
            this.pointsForNobs = pointsForNobs;
        }

        public int getTotalPoints() {
            return pointsForPairs
                    + pointsForFlushes
                    + pointsForRuns
                    + pointsForFifteens
                    + pointsForNobs;
        }

        @Override
        public String toString() {
            return String.format("Pairs: %s\nFlushes: %s\nRuns: %s\nFifteens: %s\nNobs: %s\nTotal: %s\n\n",
                    pointsForPairs,
                    pointsForFlushes,
                    pointsForRuns,
                    pointsForFifteens,
                    pointsForNobs,
                    getTotalPoints());
        }
    }

    private static class RunInfo {
        int runLength;
        int numberOfRuns;

        public RunInfo(int runLength, int numberOfRuns) {
            this.runLength = runLength;
            this.numberOfRuns = numberOfRuns;
        }

        @Override
        public String toString() {
            return "RunInfo{" +
                    "runLength=" + runLength +
                    ", numberOfRuns=" + numberOfRuns +
                    '}';
        }
    }

    private static class Pair {
        Card cardOne;
        Card cardTwo;

        public Pair(Card cardOne, Card cardTwo) {
            this.cardOne = cardOne;
            this.cardTwo = cardTwo;
        }

        public int pairValue() {
            return cardOne.getValueAsInt(false) + cardTwo.getValueAsInt(false);
        }

        public boolean isPairSameValue() {
            return cardOne.value == cardTwo.value;
        }

        public boolean contains(Card card) {
            return (cardOne.equals(card) || cardTwo.equals(card));
        }

        @Override
        public String toString() {
            return "Pair{" +
                    "cardOne=" + cardOne +
                    ", cardTwo=" + cardTwo +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return contains(pair.cardOne) && contains(pair.cardTwo);
        }

        @Override
        public int hashCode() {
            return cardOne.suit.ordinal() + cardTwo.suit.ordinal() + cardOne.value.ordinal() + cardTwo.suit.ordinal();
        }
    }

    private static class Card {
        Value value;
        Suit suit;

        Card(Value value, Suit suit) {
            this.value = value;
            this.suit = suit;
        }

        public int getValueAsInt(boolean straight) {
            switch (value) {
                case ACE:
                    return 1;
                case TWO:
                    return 2;
                case THREE:
                    return 3;
                case FOUR:
                    return 4;
                case FIVE:
                    return 5;
                case SIX:
                    return 6;
                case SEVEN:
                    return 7;
                case EIGHT:
                    return 8;
                case NINE:
                    return 9;
                case TEN:
                    return 10;
                case JACK:
                    return straight ? 11 : 10;
                case QUEEN:
                    return straight ? 12 : 10;
                case KING:
                    return straight ? 13 : 10;
                default:
                    throw new IllegalArgumentException(String.format("Cannot get the int card of %s", value));
            }
        }

        enum Suit {
            CLUB, DIAMOND, HEART, SPADE
        }

        enum Value {
            ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING
        }

        @Override
        public String toString() {
            return String.format("v=%s s=%s", value, suit);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Card card = (Card) o;
            return value == card.value &&
                    suit == card.suit;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, suit);
        }
    }
}
