import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

enum SHAPE {
    ROCK(1), PAPER(2), SCISSORS(3);

    private final int score;

    SHAPE(int score) {
        this.score = score;
    }

    public int getScore() {
        return this.score;
    }
}

enum RESULT {
    LOSS(0), DRAW(3), WIN(6);

    private final int score;

    RESULT(int score) {
        this.score = score;
    }

    public int getScore() {
        return this.score;
    }
}

// Represents each entry in the Rock-Paper-Scissors strategy guide
class StrategyGuideEntry {
    private SHAPE opponent;
    private SHAPE mine;
    private RESULT roundResult;

    // Used in part 1 when interpreting the second column in the strategy guide
    // as what shape we should play.
    public StrategyGuideEntry(SHAPE opponent, SHAPE mine) {
        this.opponent = opponent;
        this.mine = mine;
    }

    // Used in part 2 when interpreting the second column in the strategy guide
    // as what the round result should be.
    public StrategyGuideEntry(SHAPE opponent, RESULT roundResult) {
        this.opponent = opponent;
        this.roundResult = roundResult;
    }

    public SHAPE getOpponentShape() { return this.opponent; }
    public SHAPE getMyShape() { return this.mine; }
    public RESULT getRoundResult() { return this.roundResult; }
}

public class Day2_Rock_Paper_Scissors {
    public static void main(String[] args) {
        File file = new File("./inputs/day2/day2.txt");

        // Maps the letters found in the strategy guide (input) to their respective shapes.
        Map<String, SHAPE> letterToShape = Map.of(
                "A", SHAPE.ROCK,
                "B", SHAPE.PAPER,
                "C", SHAPE.SCISSORS,
                "X", SHAPE.ROCK,
                "Y", SHAPE.PAPER,
                "Z", SHAPE.SCISSORS);

        // Maps the letters found in the strategy guide (input) to their respective round results.
        Map<String, RESULT> letterToResult = Map.of(
                "X", RESULT.LOSS,
                "Y", RESULT.DRAW,
                "Z", RESULT.WIN);
        try {
            Scanner sc = new Scanner(file);

            // Stores each pair of values of the strategy guide assuming that the
            // second column represents my rock-paper-scissors shape.
            List<StrategyGuideEntry> entriesWithMyShape = new ArrayList<>();

            // Stores each pair of values of the strategy guide assuming that the
            // second column represents the round result.
            List<StrategyGuideEntry> entriesWithRoundResult = new ArrayList<>();

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] tokens = line.split(" ");

                SHAPE opponent = letterToShape.get(tokens[0]);
                SHAPE mine = letterToShape.get(tokens[1]);
                entriesWithMyShape.add(new StrategyGuideEntry(opponent, mine));

                RESULT roundResult = letterToResult.get(tokens[1]);
                entriesWithRoundResult.add(new StrategyGuideEntry(opponent, roundResult));
            }

            int part1 = part1(entriesWithMyShape);
            System.out.println("Part 1 is: " + part1);

            int part2 = part2(entriesWithRoundResult);
            System.out.println("Part 2 is: " + part2);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // Given the opponent and my rock-paper-scissor shapes, returns the round result.
    private static RESULT evaluateRound(StrategyGuideEntry entry) {
        int opponent = entry.getOpponentShape().getScore();
        int mine = entry.getMyShape().getScore();

        int difference = mine - opponent;

        if (difference == 0) {
            return RESULT.DRAW;
        }

        // The difference between Scissors (2) and Paper (1) is 1.
        // The difference between Paper (1) and Rock (0) is 1.
        // The difference between Rock (0) and Scissors (2) is -2.
        if (difference == 1 || difference == -2) {
            return RESULT.WIN;
        }

        return RESULT.LOSS;
    }

    // Part 1: Store the score of selecting a Rock, Paper, Scissor shape and whether a round is a win, draw, or loss
    // in an enum at the top of this file. Then, iterate through all the pairs of opponent shapes and my shapes and
    // determine whether the round is a win, loss, or draw and calculate the resulting score accordingly.
    private static int part1(List<StrategyGuideEntry> entries) {
        int totalScore = 0;
        for (StrategyGuideEntry entry : entries) {
            totalScore += entry.getMyShape().getScore();

            RESULT roundResult = evaluateRound(entry);
            totalScore += roundResult.getScore();
        }

        return totalScore;
    }

    // Given the opponent rock-paper-scissor shape and the desired round result,
    // returns the shape I should select.
    private static SHAPE determineMyShape(StrategyGuideEntry entry) {
        RESULT roundResult = entry.getRoundResult();
        SHAPE opponent = entry.getOpponentShape();
        Map<Integer, SHAPE> scoreToShape = Map.of(
                1, SHAPE.ROCK,
                2, SHAPE.PAPER,
                3, SHAPE.SCISSORS
        );

        // The shape that loses to an opponent's shape is immediately to its "left" (Rock - Paper - Scissors),
        // wrapping around if we fall off the edge.
        if (roundResult == RESULT.LOSS) {
            int previousKey = (opponent.getScore() + 1) % 3 + 1;
            return scoreToShape.get(previousKey);
        }

        // The shape that beats an opponent shape is immediately to its "right" (Rock - Paper - Scissors),
        // wrapping around if we fall off the edge.
        if (roundResult == RESULT.WIN) {
            int nextKey = opponent.getScore() % 3 + 1;
            return scoreToShape.get(nextKey);
        }

        // Otherwise, the result is a draw and we just return the same shape as the opponent.
        return opponent;
    }

    // Part 2: Store the score of selecting a Rock, Paper, Scissor shape and whether a round is a win, draw, or loss
    // in an enum at the top of this file. Then, iterate through all the pairs of opponent shapes and round results and
    // calculate which shape I should select in order for the round to end as expected.
    // Calculate the total score accordingly.
    private static int part2(List<StrategyGuideEntry> entries) {
        int totalScore = 0;
        for (StrategyGuideEntry entry : entries) {
            totalScore += entry.getRoundResult().getScore();
            totalScore += determineMyShape(entry).getScore();
        }

        return totalScore;
    }
}
