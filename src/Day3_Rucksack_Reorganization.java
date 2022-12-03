import java.io.File;
import java.io.IOException;
import java.util.*;

public class Day3_Rucksack_Reorganization {
    public static void main(String[] args) {
        File file = new File("./inputs/day3/day3.txt");

        try {
            Scanner sc = new Scanner(file);
            List<String> rucksackContents = new ArrayList<>();

            while (sc.hasNextLine()) {
                rucksackContents.add(sc.nextLine());
            }

            int part1 = part1(rucksackContents);
            System.out.println("Part 1 is: " + part1);

            int part2 = part2(rucksackContents);
            System.out.println("Part 2 is: " + part2);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // Given two strings representing both compartments of a rucksack,
    // find the character that exists in both compartments.
    private static char findDuplicateCharacter(String compartment1, String compartment2) {
        char duplicateChar = Character.MIN_VALUE;
        Set<Character> set = new HashSet<>();

        // Start by adding the first compartment's contents into a set
        // (which ignores duplicates within a compartment).
        for (int i = 0; i < compartment1.length(); i++) {
            set.add(compartment1.charAt(i));
        }

        // Then, check each character of the second compartment against the set to find a duplicate.
        for (int i = 0; i < compartment2.length(); i++) {
            if (set.contains(compartment2.charAt(i))) {
                duplicateChar = compartment2.charAt(i);
                break;
            }
        }

        return duplicateChar;
    }

    // Given a character, calculate its priority according to:
    // Lowercase characters 'a' through 'z' have priorities 1 through 26.
    // Uppercase characters 'A' through 'Z' have priorities 27 through 52.
    private static int calculatePriority(char character) {
        if (Character.isLowerCase(character)) {
            // ASCII representation of 'a' is 97 so if we want 'a' -> 1 and 'z' -> 26, we subtract 96 from each.
            return character - 96;
        } else { // uppercase
            // ASCII representation of 'A' is 65 so if we want 'A' -> 27 and 'Z' -> 52, we subtract 38 from each.
            return character- 38;
        }
    }

    // Part 1: Iterates through each of the strings representing each rucksack's contents.
    // Find the duplicate character and convert it into a priority.
    private static int part1(List<String> rucksackContents) {
        int prioritySum = 0;

        for (String rucksackContent : rucksackContents) {
            String compartment1 = rucksackContent.substring(0, rucksackContent.length() / 2);
            String compartment2 = rucksackContent.substring(rucksackContent.length() / 2);

            Character duplicateChar = findDuplicateCharacter(compartment1, compartment2);
            prioritySum += calculatePriority(duplicateChar);
        }

        return prioritySum;
    }

    // Given three strings representing three rucksack contents,
    // find the character that is common between all three strings.
    private static char findCommonCharacter(String rucksack1, String rucksack2, String rucksack3) {
        char commonChar = Character.MIN_VALUE;
        Set<Character> firstRucksackChars = new HashSet<>();
        Set<Character> duplicateChars = new HashSet<>();

        // Start by adding the first rucksack's contents into a set
        // (which ignores duplicates within a rucksack).
        for (int i = 0; i < rucksack1.length(); i++) {
            firstRucksackChars.add(rucksack1.charAt(i));
        }

        // Then, check each character of the second rucksack against the set to find the common chars between them.
        for (int i = 0; i < rucksack2.length(); i++) {
            if (firstRucksackChars.contains(rucksack2.charAt(i))) {
                duplicateChars.add(rucksack2.charAt(i));
            }
        }

        // Finally, check each character of the third rucksack against
        // the set of duplicate chars between the first and second rucksacks
        // to find the char that is common across all three.
        for (int i = 0; i < rucksack3.length(); i++) {
            if (duplicateChars.contains(rucksack3.charAt(i))) {
                commonChar = rucksack3.charAt(i);
                break;
            }
        }

        return commonChar;
    }

    // Part 2: Iterate through each grouping of 3 strings representing each rucksack's contents.
    // Find the character that is common across all 3 of them and convert it into a priority.
    private static int part2(List<String> rucksackContents) {
        int prioritySum = 0;

        for (int i = 0; i < rucksackContents.size(); i += 3) {
            String rucksack1 = rucksackContents.get(i);
            String rucksack2 = rucksackContents.get(i+1);
            String rucksack3 = rucksackContents.get(i+2);

            Character commonCharacter = findCommonCharacter(rucksack1, rucksack2, rucksack3);
            prioritySum += calculatePriority(commonCharacter);
        }

        return prioritySum;
    }
}