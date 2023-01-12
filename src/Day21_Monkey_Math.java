import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.*;

class MonkeyNode {
    private final String name;
    private String operand;
    private double value;
    public MonkeyNode left;
    public MonkeyNode right;

    public MonkeyNode(String name) { this.name = name; }

    public String getName() { return name; }
    public double getValue() { return value; }

    public void setLeft(MonkeyNode left) { this.left = left; }
    public void setRight(MonkeyNode right) { this.right = right; }
    public void setOperand(String operand) { this.operand = operand; }
    public void setValue(double value) { this.value = value; }
    public void setValue(double leftValue, double rightValue) {
        switch (operand) {
            case "*" -> value = leftValue * rightValue;
            case "+" -> value = leftValue + rightValue;
            case "-" -> value = leftValue - rightValue;
            case "/" -> value = leftValue / rightValue;
        }
    }

    public String toString() { return this.name; }
}

public class Day21_Monkey_Math {
    public static void main(String[] args) {
        File file = new File("./inputs/day21/day21.txt");
        Map<String, String> inputMap = new HashMap<>();

        try {
            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] tokens = line.split(": ");
                inputMap.put(tokens[0], tokens[1]);
            }

            MonkeyNode skeletonRoot = new MonkeyNode("root");

            MonkeyNode root = buildTree(skeletonRoot, inputMap);

            long part1 = part1(root);
            System.out.println("Part 1 is: " + part1);

            long part2 = part2(inputMap);
            System.out.println("Part 2 is: " + part2);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // Given a skeleton node (a node with only a name) and a map indicating its expression,
    // returns a tree data structure rooted at that node. If the expression is only a value, it is
    // considered a leaf node (our base case) so its value is simply stored in the node.
    // If the expression has a left and right operand along with an operator, then we generate its left
    // and right subtrees by recursively calling buildTree. At the same time, we evaluate the expressions given
    // by the value of its left and right subtrees along with its assigned operand and store it in the node.
    private static MonkeyNode buildTree(MonkeyNode node, Map<String, String> inputMap) {
        // This is either a single integer or an expression (e.g. a + b)
        String valueOrExpression = inputMap.get(node.getName());

        String[] tokens = valueOrExpression.split(" ");

        // Base case: We've reached a leaf node which is a monkey that only has an integer value.
        if (tokens.length == 1) {
            node.setValue(Long.parseLong(tokens[0]));
            return node;
        }

        // Otherwise, recursively build up the left and right subtrees.
        MonkeyNode skeletonLeft = new MonkeyNode(tokens[0]);
        MonkeyNode skeletonRight = new MonkeyNode(tokens[2]);

        MonkeyNode left = buildTree(skeletonLeft, inputMap);
        MonkeyNode right = buildTree(skeletonRight, inputMap);

        node.setLeft(left);
        node.setRight(right);
        node.setOperand(tokens[1]);
        node.setValue(left.getValue(), right.getValue());

        return node;
    }



    // Part 1: For part 1, the buildTree function does the real work. After the tree is recursively built up,
    // the root node stores the final value of the evaluation of each of its subtrees so all we need to do is
    // return its value.
    private static long part1(MonkeyNode root) {
        return (long)root.getValue();
    }

    // Part 2: For part 2, we vary the value of "humn" and repeatedly call the buildTree function to determine
    // if the values of the left and right subtrees are equal. We try to "binary search" our way to the right value
    // by trying different values of "humn" until we find the right value. We take larger steps at the beginning and
    // then smaller steps when both left/right subtree values are close to each other.
    // Note: The way this code works does not work for the example as we manually figured out which subtree
    // the "humn" node was in for the full input and hard coded the calculate from there.
    private static long part2(Map<String, String> inputMap) {
        MonkeyNode skeletonRoot = new MonkeyNode("root");
        MonkeyNode root = buildTree(skeletonRoot, inputMap);

        while (root.left.getValue() != root.right.getValue()) {
            long initial = Long.parseLong(inputMap.get("humn"));

            // If the right value is greater
            if (root.left.getValue() < root.right.getValue()) {
                long diff = (long)(root.right.getValue() - root.left.getValue());

                if (diff > 1000) {
                    inputMap.put("humn", Long.toString(initial - diff / 1000));
                } else {
                    inputMap.put("humn", Long.toString(initial - 1));
                }
            } else { // If the left value is greater
                long diff = (long)(root.left.getValue() - root.right.getValue());
                if (diff > 1000) {
                    inputMap.put("humn", Long.toString(initial + diff / 1000));
                } else {
                    inputMap.put("humn", Long.toString(initial + 1));
                }
            }

            root = buildTree(skeletonRoot, inputMap);
        }


        return Long.parseLong(inputMap.get("humn"));
    }
}