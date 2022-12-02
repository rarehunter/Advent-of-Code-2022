import java.io.File;
import java.io.IOException;
import java.util.*;

public class Day1_Calorie_Counting {
    public static void main(String[] args) {
        File file = new File("./inputs/day1/day1.txt");

        try {
            Scanner sc = new Scanner(file);
            List<Integer> elfCalories = new ArrayList<>();
            int maxCalories = 0; // The largest number of calories that an elf carries.
            int currentElfCalories = 0; // Accumulator for the number of calories the current elf is carrying.

            // Read each line of the input and add up the calories for each elf.
            // Also, keep track of the max calories seen so far in order to solve part 1.
            while (sc.hasNextLine()) {
                String line = sc.nextLine();

                if (line == "") {
                    maxCalories = Math.max(currentElfCalories, maxCalories);
                    elfCalories.add(currentElfCalories);
                    currentElfCalories = 0;
                    continue;
                }

                currentElfCalories += Integer.parseInt(line);
            }

            System.out.println("Part 1 is: " + maxCalories);

            int part2 = part2(elfCalories);
            System.out.println("Part 2 is: " + part2);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // Part 2: To find the top 3 calories, sort the array of all the calories.
    // Add up the three largest calories in order to solve part 2.
    private static int part2(List<Integer> elfCalories) {
        List<Integer> calories = new ArrayList<>(elfCalories);
        Collections.sort(calories);

        int lastIndex = calories.size() - 1;
        return calories.get(lastIndex) + calories.get(lastIndex - 1) + calories.get(lastIndex - 2);
    }
}