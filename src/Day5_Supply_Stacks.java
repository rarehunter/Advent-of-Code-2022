import java.io.File;
import java.io.IOException;
import java.util.*;

// Using a record instead of a class as all its fields are final.
// Only exists to act as a "transparent carrier of immutable data".
// This is nice because it eliminates all the boilerplate code needed to set and get the data from the instance.
record Instruction(int numCratesToMove, int sourceStack, int destinationStack) { }

public class Day5_Supply_Stacks {
    public static void main(String[] args) {
        File file = new File("./inputs/day5/day5.txt");

        // Stores the lines of input pertaining to the parsing of the initial crate/stack state.
        Stack<String> stackOfStacks = new Stack<>();
        List<Stack<Character>> supplies = new ArrayList();
        supplies.add(new Stack<>()); // We insert an empty stack at index 0 to make indexing later on easier for us.
        List<Instruction> instructions = new ArrayList<>();

        try {
            Scanner sc = new Scanner(file);

            boolean processingStacks = true;
            while (sc.hasNextLine()) {
                String line = sc.nextLine();

                if (line.isEmpty()) {
                    processingStacks = false;
                    continue;
                }

                // Start by storing the lines pertaining to the creation of the stacks in reverse order.
                if (processingStacks) {
                    stackOfStacks.push(line);
                    continue;
                }

                // Next store the movement instructions.
                String[] tokens = line.split(" ");

                instructions.add(new Instruction(
                        Integer.parseInt(tokens[1]),
                        Integer.parseInt(tokens[3]),
                        Integer.parseInt(tokens[5])));
            }

            // Now, we process the creation of the stacks starting with the stack numbers.
            String stackNumbers = stackOfStacks.pop();
            String[] tokens = stackNumbers.trim().split("   ");
            int numStacks = Integer.parseInt(tokens[tokens.length-1]);

            // Initialize our supplies list with all empty stacks.
            // Note that there is an extra empty stack at index 0 to account for the fact that stack numbering starts
            // at 1 in our instructions.
            for (int i = 0; i < numStacks; i++) {
                supplies.add(new Stack<>());
            }

            // Observe that the crate character is found at i*4+1 on a line.
            while (!stackOfStacks.isEmpty()) {
                String line = stackOfStacks.pop();
                // For each stack, find the character of that crate (if any) and add it to the stack.
                for (int i = 0; i < numStacks; i++) {
                    int crateIndex = i * 4 + 1;

                    if (crateIndex < line.length()) {
                        char crate = line.charAt(crateIndex);

                        if (crate != ' ')
                            supplies.get(i + 1).add(crate);
                    }
                }
            }

            // Because the supplies list is modified in each of the part1 and part2 functions,
            // only one of them can remain uncommented at a time. We could perform a deep copy
            // of the supplies list, but it's not strictly necessary to get the final solutions.
            String part1 = part1(supplies, instructions);
            System.out.println("Part 1 is: " + part1);

            //String part2 = part2(supplies, instructions);
            //System.out.println("Part 2 is: " + part2);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // Part 1: For each instruction, execute the instruction by moving the given number of crates from one stack to another.
    private static String part1(List<Stack<Character>> supplies, List<Instruction> instructions) {
        for (Instruction instruction : instructions) {
            int numCratesToMove = instruction.numCratesToMove();
            Stack<Character> sourceStack = supplies.get(instruction.sourceStack());
            Stack<Character> destinationStack = supplies.get(instruction.destinationStack());

            for (int i = 0; i < numCratesToMove; i++) {
                destinationStack.push(sourceStack.pop());
            }
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 1; i < supplies.size(); i++) {
            sb.append(supplies.get(i).peek());
        }

        return sb.toString();
    }

    // Part 2: For each instruction, execute the instruction by moving the given number of crates into a temporary
    // stack and then from the temporary stack to the destination stack.
    // This preserves the order of the crates in the stack.
    private static String part2(List<Stack<Character>> supplies, List<Instruction> instructions) {
        Stack<Character> tempStack = new Stack<>();

        for (Instruction instruction : instructions) {
            int numCratesToMove = instruction.numCratesToMove();
            Stack<Character> sourceStack = supplies.get(instruction.sourceStack());
            Stack<Character> destinationStack = supplies.get(instruction.destinationStack());

            for (int i = 0; i < numCratesToMove; i++) {
                tempStack.push(sourceStack.pop());
            }

            for (int i = 0; i < numCratesToMove; i++) {
                destinationStack.push(tempStack.pop());
            }

            tempStack.clear();
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 1; i < supplies.size(); i++) {
            sb.append(supplies.get(i).peek());
        }

        return sb.toString();
    }
}