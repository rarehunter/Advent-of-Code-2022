import java.io.File;
import java.io.IOException;
import java.util.*;

class Number {
    private final long number;
    private final int index; // original index of the number in the input file

    public Number(long number, int index) {
        this.number = number;
        this.index = index;
    }

    public String toString() { return "(" + number + ", " + index + ")"; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Number number1 = (Number) o;
        return number == number1.number && index == number1.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, index);
    }
}

class NumberNode {
    private final long number;
    public NumberNode next;
    public NumberNode prev;

    public NumberNode (long number) {
        this.number = number;
    }

    public long getNumber() { return number; }

    public String toString() { return number + ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumberNode that = (NumberNode) o;
        return number == that.number && Objects.equals(next, that.next) && Objects.equals(prev, that.prev);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, next, prev);
    }
}

record Triplet(NumberNode head, List<Long> originalList, Map<Number, NumberNode> originalNodeMap) {}

public class Day20_Grove_Positioning_System {
    private static final long DECRYPTION_KEY = 811589153;

    public static void main(String[] args) {
        File file = new File("./inputs/day20/day20.txt");

        try {
            Scanner sc = new Scanner(file);
            Triplet tripletPart1 = parseInput(sc, false);

            long part1 = part1(tripletPart1.head(), tripletPart1.originalList(), tripletPart1.originalNodeMap());
            System.out.println("Part 1 is: " + part1);

            // Because part 2 requires the way the list looked at the beginning (with each number multiplied by
            // the decryption key), we parse our input from the file again.
            Scanner sc2 = new Scanner(file);
            Triplet tripletPart2 = parseInput(sc2, true);

            long part2 = part2(tripletPart2.head(), tripletPart2.originalList(), tripletPart2.originalNodeMap());
            System.out.println("Part 2 is: " + part2);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // Parse the input and build the circular doubly-linked list. Return three pieces of information:
    // 1. The head of the circular doubly-linked list.
    // 2. A list which represents the original input.
    // 3. A mapping from a <number, index> pair (in order to handle duplicates) to a node representing
    // the number in our circular linked list.
    private static Triplet parseInput(Scanner sc, boolean withDecryptionKey) {
        Map<Number, NumberNode> originalNodeMap = new HashMap<>(); // maps the <number, index> pair to the NumberNode object.
        List<Long> originalList = new ArrayList<>();

        NumberNode head = null;
        NumberNode current = null;
        int index = 0;

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            long number = Integer.parseInt(line);

            if (withDecryptionKey)
                number *= DECRYPTION_KEY;

            originalList.add(number);

            NumberNode node = new NumberNode(number);

            if (index == 0) {
                head = node;
                current = node;
            } else {
                current.next = node;
                node.prev = current;
                current = current.next;
            }

            Number n = new Number(number, index);
            index++;

            originalNodeMap.put(n, node);
        }

        // Finally, hook up the last node in the LL to the head node of the LL.
        current.next = head;
        head.prev = current;

        return new Triplet(head, originalList, originalNodeMap);
    }

    // Helper method to print our linked list.
    // Because it is a circular, linked list, we don't reprint the head if we come across it again.
    private static void printLL(NumberNode head) {
        NumberNode current = head;
        boolean headReachedAgain = false;
        while (!headReachedAgain) {
            System.out.print(current.getNumber() + " -> ");
            current = current.next;

            if (current.equals(head) && !headReachedAgain) {
                headReachedAgain = true;
            }
        }
        System.out.println();
    }

    // Performs a movement of the given node by the given amount to move.
    // If the number is negative, moves the node to the left by that amount.
    // If the number is positive, moves the node to the right by that amount.
    private static void moveNode(NumberNode nodeToMove, long number, int listLength) {
        NumberNode current = nodeToMove;

        if (number > 0) {
            // First, extract the node from the list.
            nodeToMove.next.prev = nodeToMove.prev;
            nodeToMove.prev.next = nodeToMove.next;

            // Find out where the new position should be. We use a modulo instead of having to repeatedly
            // loop through the linked list.
            for (long i = 0; i < number % (listLength - 1); i++) {
                current = current.next;
            }

            // Insert the node into the new position.
            nodeToMove.next = current.next;
            nodeToMove.prev = current;
            current.next.prev = nodeToMove;
            current.next = nodeToMove;
        } else if (number < 0) {
            // First, extract the node from the list.
            nodeToMove.next.prev = nodeToMove.prev;
            nodeToMove.prev.next = nodeToMove.next;

            // Find out where the new position should be. We use a modulo instead of having to repeatedly
            // loop through the linked list.
            for (long i = 0; i < Math.abs(number % (listLength - 1)); i++) {
                current = current.prev;
            }

            // Insert the node into the new position.
            nodeToMove.next = current;
            nodeToMove.prev = current.prev;
            current.prev.next = nodeToMove;
            current.prev = nodeToMove;
        }
    }

    // Calculates the sum of the 1000th, 2000th, and 3000th numbers after the node of 0.
    private static long calculateGroveCoordinates(NumberNode zeroNode) {
        NumberNode current = zeroNode;
        long sum = 0;

        for (int i = 1; i <= 3000; i++) {
            current = current.next;
            if (i == 1000 || i == 2000 || i == 3000) {
                sum += current.getNumber();
            }
        }

        return sum;
    }

    // Part 1: Implement a circular, doubly linked list. To handle duplicates, we use a <number, index> pair.
    private static long part1(NumberNode head, List<Long> originalList, Map<Number, NumberNode> originalListMap) {
        // Iterate through the original ordering of the numbers and move them according to their values.
        for (int i = 0; i < originalList.size(); i++) {
            long number = originalList.get(i);
            NumberNode nodeToMove = originalListMap.get(new Number(number, i));

            // If we're moving a head, be sure to update the head.
            if (nodeToMove.equals(head)) {
                head = nodeToMove.next;
            }
            moveNode(nodeToMove, number, originalList.size());
        }

        // Get the grove coordinates of the 1000th, 2000th, and 3000th numbers
        // AFTER the value of 0. So first we must find where the 0 is in our list.
        int indexOfZero = originalList.indexOf(0L);
        NumberNode zeroNode = originalListMap.get(new Number(0L, indexOfZero));
        return calculateGroveCoordinates(zeroNode);
    }

    // Part 2: Now we have to rerun the same algorithm as in part 1 but with larger numbers and through
    // the original list ten times.
    private static long part2(NumberNode head, List<Long> originalList, Map<Number, NumberNode> originalListMap) {
        // Iterate through the original ordering of the numbers and move them according to their values.
        for (int i = 0; i < originalList.size() * 10; i++) {
            long number = originalList.get(i % originalList.size());

            NumberNode nodeToMove = originalListMap.get(new Number(number, i % originalList.size()));

            // If we're moving a head, be sure to update the head.
            if (nodeToMove.equals(head)) {
                head = nodeToMove.next;
            }
            moveNode(nodeToMove, number, originalList.size());
        }

        // Get the grove coordinates of the 1000th, 2000th, and 3000th numbers
        // AFTER the value of 0. So first we must find where the 0 is in our list.
        int indexOfZero = originalList.indexOf(0L);
        NumberNode zeroNode = originalListMap.get(new Number(0L, indexOfZero));
        return calculateGroveCoordinates(zeroNode);
    }
}