import java.io.File;
import java.io.IOException;
import java.util.*;

public class Day6_Tuning_Trouble {
    private static final int START_OF_PACKET_MARKER = 4;
    private static final int START_OF_MSG_MARKER = 14;

    public static void main(String[] args) {
        File file = new File("./inputs/day6/day6.txt");

        try {
            Scanner sc = new Scanner(file);
            String input = null;
            while (sc.hasNextLine()) {
                input = sc.nextLine();
            }

            int part1 = part1(input);
            System.out.println("Part 1 is: " + part1);

            int part2 = part2(input);
            System.out.println("Part 2 is: " + part2);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // Given a marker threshold, returns the number of characters that
    // must be processed before the first marker is detected.
    // Note: This is the first function I wrote which uses a "sliding window" instead of a static window.
    // When encountering a duplicate char, it will increment the left index and remove items from the set
    // until the set no longer contains duplicates before moving on.
    // This is not strictly necessary to solve this problem and therefore, I tried implementing this logic
    // using a static window in the method findFirstOccurrenceOfMarkerStatic.
    private static int findFirstOccurrenceOfMarker(String input, int marker) {
        Set<Character> set = new HashSet<>();
        int left = 0;
        int right = 0;

        while (right < input.length()) {
            char current = input.charAt(right);

            // If at any point, we have 4 unique chars in our set, we return the number of chars
            // we've seen so far which in our case is the right index.
            if (set.size() == marker) {
                return right;
            }

            // If we encounter a potential duplicate in our set, we keep incrementing the left pointer,
            // and removing items from our set until we have a substring without repeating characters again.
            while (set.contains(current)) {
                set.remove(input.charAt(left));
                left++;
            }

            // Now that we have a substring without repeating characters, add in the current character
            // knowing that there are no duplicates.
            set.add(current);

            right++;
        }

        return 0;
    }

    // This is the second attempt at finding the first occurrence of a marker.
    // We use a static window and shift it down our input string until we encounter
    // a given number of unique characters.
    private static int findFirstOccurrenceOfMarkerStatic(String input, int marker) {
        Set<Character> set = new HashSet<>();

        for (int i = 0; i < input.length(); i++) {
            // Compute the substring of size 'marker'
            String window = input.substring(i, i+marker);

            // Add the substring to a set
            for (int j = 0; j < window.length(); j++) {
                set.add(window.charAt(j));
            }

            // Check if there are any duplicated chars in the substring.
            if (set.size() == marker) {
                return i+marker;
            }

            set.clear();
        }

        return 0;
    }

    // Part 1: Determine the first occurrence of 4 unique characters in the input string.
    private static int part1(String input) {
        return findFirstOccurrenceOfMarker(input, START_OF_PACKET_MARKER);
    }

    // Part 2: Determine the first occurrence of 14 unique characters in the input string.
    private static int part2(String input) {
        return findFirstOccurrenceOfMarker(input, START_OF_MSG_MARKER);
    }
}