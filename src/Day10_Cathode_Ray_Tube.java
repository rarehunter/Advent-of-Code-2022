import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

enum Command {
    NOOP, ADDX
}

class CommandEntry {
    private final Command command;
    private int value;

    public CommandEntry(Command command) {
        this.command = command;
    }

    public CommandEntry(Command command, int value) {
        this.command = command;
        this.value = value;
    }

    public Command getCommand() { return command; }
    public int getValue() { return value; }

    public String toString() {
        return this.command + " " + this.value;
    }
}

public class Day10_Cathode_Ray_Tube {
    private static final int SCREEN_PIXEL_HEIGHT = 6;
    private static final int SCREEN_PIXEL_WIDTH = 40;

    // The problem description uses '#' by default but modified here for increased readability.
    private static final char LIT_PIXEL = '█';

    // The problem description uses '.' by default but modified here for increased readability.
    private static final char UNLIT_PIXEL = ' ';

    public static void main(String[] args) {
        File file = new File("./inputs/day10/day10.txt");
        List<CommandEntry> entries = new ArrayList<>();

        try {
            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] tokens = line.split(" ");

                if (tokens[0].equals("noop")) {
                    entries.add(new CommandEntry(Command.NOOP));
                } else if (tokens[0].equals("addx")) {
                    entries.add(new CommandEntry(Command.ADDX, Integer.parseInt(tokens[1])));
                }
            }

            int part1 = part1(entries);
            System.out.println("Part 1 is: " + part1);

            System.out.println("Part 2 is: ");
            part2(entries);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    //
    private static int calculateSignalStrength(int currentCycle, int currentRegisterValue) {
        List<Integer> executionCycles = Arrays.asList(20, 60, 100, 140, 180, 220);
        if (executionCycles.contains(currentCycle)) {
            return currentCycle * currentRegisterValue;
        }

        return 0;
    }

    // Part 1: Calculates the sum of the signal strength at the 20th, 60th, 100th, 140th, 180th, 220th cycles.
    private static int part1(List<CommandEntry> entries) {
        int registerValue = 1;
        int currentCycle = 0;
        int signalStrengthSum = 0;

        // For each command entry, execute the specified number of cycles,
        // calculate the signal strength, and set the register value.
        for (CommandEntry entry : entries) {
            if (entry.getCommand().equals(Command.NOOP)) {
                currentCycle++;
                signalStrengthSum += calculateSignalStrength(currentCycle, registerValue);
            } else if (entry.getCommand().equals(Command.ADDX)) {
                currentCycle++;
                signalStrengthSum += calculateSignalStrength(currentCycle, registerValue);

                currentCycle++;
                signalStrengthSum += calculateSignalStrength(currentCycle, registerValue);

                registerValue += entry.getValue();
            }
        }

        return signalStrengthSum;
    }

    // Helper method to initialize a screen with unlit pixel characters.
    private static void initializeScreen(char[][] screen) {
        for (char[] chars : screen) {
            Arrays.fill(chars, UNLIT_PIXEL);
        }
    }

    // Helper method to print the screen pixels to the console.
    private static void printScreen(char[][] screen) {
        for (char[] row : screen) {
            for (char c : row) {
                System.out.print(c + " ");
            }
            System.out.println();
        }
    }

    // Returns true if the position of the sprite (represented by three pixels centered around the register value)
    // is within the pixel that is currently being drawn (represented by the current cycle).
    private static boolean isSpriteOverlappingPixelDrawing(int currentCycle, int registerValue) {
        // Must do some modulo arithmetic to calculate the pixel that is currently being drawn.
        int pixel = (currentCycle - 1) % SCREEN_PIXEL_WIDTH;

        return pixel == registerValue ||
                pixel == registerValue - 1 ||
                pixel == registerValue + 1;
    }

    // Draws a pixel on the screen.
    private static void drawPixel(char[][] screen, int currentCycle, int registerValue) {
        // Modulo arithmetic to calculate the array indices that is currently being drawn.
        int row = (currentCycle-1) / SCREEN_PIXEL_WIDTH;
        int column = (currentCycle-1) % SCREEN_PIXEL_WIDTH;

        // If the pixel we are drawing is overlapping a sprite, then draw a lit pixel.
        // Otherwise, draw an unlit pixel.
        if (isSpriteOverlappingPixelDrawing(currentCycle, registerValue)) {
            screen[row][column] = LIT_PIXEL;
        } else {
            screen[row][column] = UNLIT_PIXEL;
        }
    }

    // Part 2: Prints the resulting pixels of the CRT screen to the console
    // after executing all the input commands. Pixels are considered lit if at the time it is
    // being drawn, there is a sprite overlapping at that location.
    private static void part2(List<CommandEntry> entries) {
        char[][] screen = new char[SCREEN_PIXEL_HEIGHT][SCREEN_PIXEL_WIDTH];
        initializeScreen(screen);

        int registerValue = 1;
        int currentCycle = 0;

        // For each command entry, execute the specified number of cycles, draw pixels, and set the register value.
        for (CommandEntry entry : entries) {
            if (entry.getCommand().equals(Command.NOOP)) {
                currentCycle++;
                drawPixel(screen, currentCycle, registerValue);
            } else if (entry.getCommand().equals(Command.ADDX)) {
                currentCycle++;
                drawPixel(screen, currentCycle, registerValue);

                currentCycle++;
                drawPixel(screen, currentCycle, registerValue);

                registerValue += entry.getValue();
            }
        }

        printScreen(screen);
    }
}