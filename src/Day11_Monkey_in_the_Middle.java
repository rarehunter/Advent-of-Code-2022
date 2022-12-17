import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

enum MonkeyOperator {
    MULTIPLY, ADD
}

// Class to represent a monkey and its related metadata, parsed from the input file.
class Monkey {
    private final int id;
    private final LinkedList<Long> items;
    private MonkeyOperator operator;
    private int rightOperand;
    private int divisor;
    private int trueMonkey;
    private int falseMonkey;
    private long numItemsInspected;

    public Monkey(int id) {
        this.id = id;
        this.items = new LinkedList<>();
        this.numItemsInspected = 0;
    }

    public int getId() { return this.id; }
    public LinkedList<Long> getItems() { return items; }
    public void addItem(long item) { items.add(item); }

    public MonkeyOperator getOperator() { return operator; }
    public void setOperator(MonkeyOperator operator) { this.operator = operator; }

    public int getRightOperand() { return rightOperand; }
    public void setRightOperand(int rightOperand) { this.rightOperand = rightOperand; }

    public int getDivisor() { return divisor; }
    public void setDivisor(int divisor) { this.divisor = divisor; }

    public int getTrueMonkey() { return trueMonkey; }
    public void setTrueMonkey(int trueMonkey) { this.trueMonkey = trueMonkey; }

    public int getFalseMonkey() { return falseMonkey; }
    public void setFalseMonkey(int falseMonkey) { this.falseMonkey = falseMonkey; }

    public long getNumItemsInspected() { return numItemsInspected; }
    public void incrementNumItemsInspected() { numItemsInspected++; }

    public String toString() {
        return "\n" + id + ":\nItems: " + items + "\n" +
                "Operator: " + operator + "\n" +
                "Right operand: " + rightOperand + "\n" +
                "Divisor: " + divisor + "\n" +
                "True monkey: " + trueMonkey + "\n" +
                "False monkey: " + falseMonkey + "\n";
    }
}

public class Day11_Monkey_in_the_Middle {
    public static void main(String[] args) {
        File file = new File("./inputs/day11/day11.txt");

        // Note: When originally solving this problem, I determined that figuring
        // out a way to parse the input file was too time-consuming and ended up
        // hardcoding the monkey objects. After I solved the problem, I wanted to make an attempt
        // to write a parser for this problem.
        List<Monkey> monkeys = parseInput(file);

        long part1 = part1(monkeys);
        System.out.println("Part 1 is: " + part1);

        // Reset the initial state of the monkeys, so we parse the file again.
        monkeys = parseInput(file);

        long part2 = part2(monkeys);
        System.out.println("Part 2 is: " + part2);
    }

    // Helper method to parse the input file into a list of monkey objects.
    private static List<Monkey> parseInput(File file) {
        List<Monkey> monkeys = new ArrayList<>();

        try {
            Scanner sc = new Scanner(file);

            Monkey currentMonkey = null;
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();

                if (line.equals("")) {
                    currentMonkey = null;
                    continue;
                }

                // We don't have an active monkey we're parsing yet, so we need to create one
                if (currentMonkey == null) {
                    String[] tokens = line.split(" ");
                    String monkeyIdStr = tokens[1].substring(0, tokens[1].length()-1);
                    int monkeyId = Integer.parseInt(monkeyIdStr);
                    Monkey m = new Monkey(monkeyId);
                    monkeys.add(m);
                    currentMonkey = m;
                    continue;
                }

                // At this point we have a current monkey so any other lines we parse
                // will be metadata for that monkey.
                String[] tokens = line.split(": ");

                // Parse the starting items' worry levels
                switch (tokens[0]) {
                    case "Starting items" -> {
                        String[] itemIdStrs = tokens[1].split(", ");
                        for (String itemIdStr : itemIdStrs) {
                            currentMonkey.addItem(Long.parseLong(itemIdStr));
                        }
                    }
                    case "Operation" -> {  // Parse the operator and operand
                        String[] operatorTokens = tokens[1].split(" ");
                        if (operatorTokens[3].equals("*")) {
                            currentMonkey.setOperator(MonkeyOperator.MULTIPLY);
                        } else if (operatorTokens[3].equals("+")) {
                            currentMonkey.setOperator(MonkeyOperator.ADD);
                        }

                        // If the right operand is "old" instead of a number,
                        // we encode that using the max int.
                        if (operatorTokens[4].equals("old")) {
                            currentMonkey.setRightOperand(Integer.MAX_VALUE);
                        } else {
                            currentMonkey.setRightOperand(Integer.parseInt(operatorTokens[4]));
                        }
                    }
                    case "Test" -> {  // Parse the divisibility test divisor
                        String[] testDivisibilityTokens = tokens[1].split(" ");
                        currentMonkey.setDivisor(Integer.parseInt(testDivisibilityTokens[2]));
                    }
                    case "If true" -> {  // Parse the monkey id when true
                        String[] trueTokens = tokens[1].split(" ");
                        currentMonkey.setTrueMonkey(Integer.parseInt(trueTokens[3]));
                    }
                    case "If false" -> {  // Parse the monkey id when false
                        String[] falseTokens = tokens[1].split(" ");
                        currentMonkey.setFalseMonkey(Integer.parseInt(falseTokens[3]));
                    }
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return monkeys;
    }

    // Helper method to find the monkey business level: the product of the number of
    // inspected items by the two most active monkeys. We do this by adding
    // all the monkey item counts into a list, sorting the list in descending order
    // and multiplying the first two elements.
    private static long calculateMonkeyBusinessLevel(List<Monkey> monkeys) {
        List<Long> numItemsInspected = new ArrayList<>();
        for (Monkey m : monkeys) {
            numItemsInspected.add(m.getNumItemsInspected());
        }

        numItemsInspected.sort((a,b) -> (int)(b-a));
        return numItemsInspected.get(0) * numItemsInspected.get(1);
    }

    // Part 1: Simulate 20 rounds of monkey activity. Each monkey in turn will
    // inspect their queue of items and determine which monkey to throw it to.
    // We adjust the worry level according to what is described in the problem description.
    private static long part1(List<Monkey> monkeys) {
        for (int i = 0; i < 20; i++) {
            for (Monkey monkey : monkeys) {
                LinkedList<Long> items = monkey.getItems();
                while (!items.isEmpty()) {
                    long item = items.poll();

                    long worryDuringInspection = Long.MIN_VALUE;
                    MonkeyOperator operator = monkey.getOperator();
                    int rightOperand = monkey.getRightOperand();

                    if (operator.equals(MonkeyOperator.ADD)) {
                        worryDuringInspection = item + ((rightOperand < Integer.MAX_VALUE) ? rightOperand : item);
                    } else if (operator.equals(MonkeyOperator.MULTIPLY)) {
                        worryDuringInspection = item * ((rightOperand < Integer.MAX_VALUE) ? rightOperand : item);
                    }
                    long worryAfterInspection = worryDuringInspection / 3;

                    int nextMonkeyId;
                    if (worryAfterInspection % monkey.getDivisor() == 0) {
                        nextMonkeyId = monkey.getTrueMonkey();
                    } else {
                        nextMonkeyId = monkey.getFalseMonkey();
                    }
                    monkeys.get(nextMonkeyId).addItem(worryAfterInspection);

                    // Keep track of how many items each monkey inspected
                    monkey.incrementNumItemsInspected();
                }
            }
        }

        return calculateMonkeyBusinessLevel(monkeys);
    }

    /* Part 2: Simulate 10,000 rounds of monkey activity without dividing the worry level
    by 3 before performing the monkey's divisibility test. The issue here is that
    without applying any fancy tricks, the numbers get very, very large. For instance,
    one of the transformation operations is to square a worry level. As one can imagine,
    squaring a huge number results in an even huger number. Then, we take a modulo
    of that huge number in order to determine which monkey to throw it to. Therefore,
    is keeping track of the huge number necessary or is there a more efficient way to
    determine the information we need to know.

    What do we actually need the worry level for?
    For two things: for calculating the next worry level and for the divisibility test.
    But it's really one thing: what ultimately matters is the result of the divisibility test.
    Therefore, we don't have to keep the worry level around as long as the results
    of the test stay the same.

    Upon doing some pencil and paper calculations, we found that:
    (a + b) mod m = ((a mod m) + (b mod m)) % m
    (a * b) mod m = ((a mod m) * (b mod m)) % m

    What this means is that we can modulo our worry levels by a value as long as it
    does not affect any of our tests. In our input file, we see that we are testing
    for divisibility from a predefined set of values (e.g. 7, 11, 13, 3, etc.)

    We note that: for any integer n which is divisible by p, n-kp is also divisible by p.
    Conversely, if n is NOT divisible by p then n-kp is also NOT divisible by p.

    Our modulo operation must avoid affecting any future divisibility test by *any* monkey
    that will see the item later.

    For any set of integers n, p and d: if p mod d = 0, then (n mod p) mod d = n mod d.
    Example: p = 6, d = 2, n = 11
    In other words, if 6 mod 2 = 0 (6 is divisible by 2)
    Therefore, (11 mod 6) mod 2 = 11 mod 2
    d is the divisor (which is different for each monkey)
    n is the input number (worry level)
    and (n mod p) is the reduced number.

    The above observation shows that if you take p to be a common multiple for every
    possible d, then you may safely replace n by (n mod p) without messing up any
    future divisibility tests.

    In this case, since all the monkey divisors are different primes, the minimum
    number p that satisfies these conditions is simply the product of all those primes.
    If the monkey divisors weren't primes or if they had some divisors in common,
    then the "least common multiple" would be a lot smaller.

    Another way to think about it:
    For any n mod m, you end up with "blocks" of size m where consecutive
    values of n result in 0,1,2,3,...,m-1.
    If you modulo n by another number before doing mod m, some blocks might be "cut off"
    (examples below) so that (n mod k) mod m results in a different value than n mod m.
    However, this doesn't happen when k is a multiple of m.

    In this problem, you have several different "m"s, so you need to use a k that
    is a multiple of all of them.

    n                 0  1  2  3  4  5  6  7  8  9  10 11 12 13 14
    n mod 5           0  1  2  3  4  0  1  2  3  4  0  1  2  3  4
    (n mod 7) mod 5   0  1  2  3  4  0  1  0  1  2  3  4  0  1  0 // doesn't match n mod 5.
    (n mod 15) mod 5  0  1  2  3  4  0  1  2  3  4  0  1  2  3  4

    We see that (n mod 5) and (n mod 15) mod 5 generate the same values.
    */
    private static long part2(List<Monkey> monkeys) {
        long commonMultiple = 1L;
        for (Monkey m : monkeys) {
            commonMultiple *= m.getDivisor();
        }

        for (int i = 0; i < 10000; i++) {
            for (Monkey monkey : monkeys) {
                LinkedList<Long> items = monkey.getItems();
                while (!items.isEmpty()) {
                    long item = items.poll();
                    monkey.incrementNumItemsInspected();

                    long worryDuringInspection  = item % commonMultiple;
                    int rightOperand = monkey.getRightOperand();

                    if (monkey.getOperator().equals(MonkeyOperator.ADD)) {
                        if (rightOperand < Integer.MAX_VALUE) {
                            worryDuringInspection += rightOperand % commonMultiple;
                        } else {
                            worryDuringInspection += item % commonMultiple;
                        }
                    } else if (monkey.getOperator().equals(MonkeyOperator.MULTIPLY)) {
                        if (rightOperand < Integer.MAX_VALUE) {
                            worryDuringInspection *= rightOperand % commonMultiple;
                        } else {
                            worryDuringInspection *= item % commonMultiple;
                        }
                    }

                    int nextMonkeyId;
                    if (worryDuringInspection % monkey.getDivisor() == 0) {
                        nextMonkeyId = monkey.getTrueMonkey();
                    } else {
                        nextMonkeyId = monkey.getFalseMonkey();
                    }

                    monkeys.get(nextMonkeyId).addItem(worryDuringInspection);
                }
            }
        }

        return calculateMonkeyBusinessLevel(monkeys);
        // 27267163742
    }
}

