import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Pair {
    private int sectionIdLow;
    private int sectionIdHigh;

    public Pair(int sectionIdLow, int sectionIdHigh) {
        this.sectionIdLow = sectionIdLow;
        this.sectionIdHigh = sectionIdHigh;
    }

    public int getSectionIdLow() { return this.sectionIdLow; }
    public int getSectionIdHigh() { return this.sectionIdHigh; }
}

class Entry {
    private Pair firstElfAssignment;
    private Pair secondElfAssignment;

    public Entry(Pair firstElfAssignment, Pair secondElfAssignment) {
        this.firstElfAssignment = firstElfAssignment;
        this.secondElfAssignment = secondElfAssignment;
    }

    public Pair getFirstElfAssignment() { return this.firstElfAssignment; }
    public Pair getSecondElfAssignment() { return this.secondElfAssignment; }
}

public class Day4_Camp_Cleanup {
    public static void main(String[] args) {
        File file = new File("./inputs/day4/day4.txt");
        List<Entry> entries = new ArrayList<>();
        try {
            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] elfAssignmentRanges = line.split(",");
                String[] elfAssignments1 = elfAssignmentRanges[0].split("-");
                String[] elfAssignments2 = elfAssignmentRanges[1].split("-");

                Pair p1 = new Pair(Integer.parseInt(elfAssignments1[0]), Integer.parseInt(elfAssignments1[1]));
                Pair p2 = new Pair(Integer.parseInt(elfAssignments2[0]), Integer.parseInt(elfAssignments2[1]));

                entries.add(new Entry(p1, p2));
            }

            int part1 = part1(entries);
            System.out.println("Part 1 is: " + part1);

            int part2 = part2(entries);
            System.out.println("Part 2 is: " + part2);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // Returns true if one range of values is fully contained in the other range of values.
    // Returns false otherwise.
    // e.g. 2-8, 3-7 => true
    private static boolean isFullyContained(Pair p1, Pair p2) {
        int p1SectionIdLow = p1.getSectionIdLow();
        int p1SectionIdHigh = p1.getSectionIdHigh();
        int p2SectionIdLow = p2.getSectionIdLow();
        int p2SectionIdHigh = p2.getSectionIdHigh();

        return ((p2SectionIdLow >= p1SectionIdLow && p2SectionIdHigh <= p1SectionIdHigh) ||
                (p1SectionIdLow >= p2SectionIdLow && p1SectionIdHigh <= p2SectionIdHigh));
    }

    // Part 1: Counts the number of entries in which one of the ranges is fully contained within the other.
    private static int part1(List<Entry> entries) {
        int numFullyContained = 0;

        for (Entry e : entries) {
            if (isFullyContained(e.getFirstElfAssignment(), e.getSecondElfAssignment())) {
                numFullyContained++;
            }
        }

        return numFullyContained;
    }

    // Returns true if one range of values is overlaps the other range of values.
    // Returns false otherwise.
    // e.g. 2-4, 6-8 => false
    // e.g. 5-7, 7-9 => true
    private static boolean isOverlapping(Pair p1, Pair p2) {
        int p1SectionIdLow = p1.getSectionIdLow();
        int p1SectionIdHigh = p1.getSectionIdHigh();
        int p2SectionIdLow = p2.getSectionIdLow();
        int p2SectionIdHigh = p2.getSectionIdHigh();

        // A range of values does NOT overlap another range of values if the highest value of the range is
        // always less than the lowest value of the other range.
        if (p1SectionIdHigh < p2SectionIdLow || p2SectionIdHigh < p1SectionIdLow)
            return false;

        return true;
    }

    // Part 2: Counts the number of overlapping ranges.
    private static int part2(List<Entry> entries) {
        int numOverlaps = 0;

        for (Entry e : entries) {
            if (isOverlapping(e.getFirstElfAssignment(), e.getSecondElfAssignment())) {
                numOverlaps++;
            }
        }

        return numOverlaps;
    }
}