import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.*;

class Elf {
    private Point position;
    private Point proposedPosition;

    public Elf(Point position) {
        this.position = position;
        this.proposedPosition = null;
    }

    public Point getPosition() { return position; }
    public Point getProposedPosition() { return proposedPosition; }

    public void setPosition(Point pos) { position = pos; }
    public void setProposedPosition(Point pos) { proposedPosition = pos; }
}

interface ElfDirection {
    Point checkDirection(Set<Point> elfPositions, Point elfPosition);
}

class ElfDirectionNorth implements ElfDirection {
    // Checks north to see if there are any elves present. If not, returns the proposed point.
    // If so, returns null.
    public Point checkDirection(Set<Point> elfPositions, Point elf) {
        if (elfPositions.contains(new Point(elf.x-1, elf.y-1)) ||
                elfPositions.contains(new Point(elf.x, elf.y-1)) ||
                elfPositions.contains(new Point(elf.x+1, elf.y-1))) {
            return null;
        }
        return new Point(elf.x, elf.y-1);
    }
}

class ElfDirectionSouth implements ElfDirection {
    // Checks south to see if there are any elves present. If not, returns the proposed point.
    // If so, returns null.
    public Point checkDirection(Set<Point> elfPositions, Point elf) {
        if (elfPositions.contains(new Point(elf.x-1, elf.y+1)) ||
                elfPositions.contains(new Point(elf.x, elf.y+1)) ||
                elfPositions.contains(new Point(elf.x+1, elf.y+1))) {
            return null;
        }
        return new Point(elf.x, elf.y+1);
    }
}

class ElfDirectionEast implements ElfDirection {
    // Checks east to see if there are any elves present. If not, returns the proposed point.
    // If so, returns null.
    public Point checkDirection(Set<Point> elfPositions, Point elf) {
        if (elfPositions.contains(new Point(elf.x+1, elf.y-1)) ||
                elfPositions.contains(new Point(elf.x+1, elf.y)) ||
                elfPositions.contains(new Point(elf.x+1, elf.y+1))) {
            return null;
        }
        return new Point(elf.x+1, elf.y);
    }
}

class ElfDirectionWest implements ElfDirection {
    // Checks west to see if there are any elves present. If not, returns the proposed point.
    // If so, returns null.
    public Point checkDirection(Set<Point> elfPositions, Point elf) {
        if (elfPositions.contains(new Point(elf.x-1, elf.y-1)) ||
                elfPositions.contains(new Point(elf.x-1, elf.y)) ||
                elfPositions.contains(new Point(elf.x-1, elf.y+1))) {
            return null;
        }
        return new Point(elf.x-1, elf.y);
    }
}

public class Day23_Unstable_Diffusion {
    public static void main(String[] args) {
        File file = new File("./inputs/day23/day23.txt");

        // Two elf lists. One for part 1 and one for part 2.
        List<Elf> elvesPart1 = new ArrayList<>();
        List<Elf> elvesPart2 = new ArrayList<>();

        try {
            Scanner sc = new Scanner(file);

            int row = 0;
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                for (int i = 0; i < line.length(); i++) {
                    // Instead of creating a 2D array, we simply store the positions of each elf.
                    if (line.charAt(i) == '#') {
                        elvesPart1.add(new Elf(new Point(i, row)));
                        elvesPart2.add(new Elf(new Point(i, row)));
                    }
                }
                row++;
            }

            int part1 = part1(elvesPart1);
            System.out.println("Part 1 is: " + part1);

            int part2 = part2(elvesPart2);
            System.out.println("Part 2 is: " + part2);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // An elf is isolated if there are no elves in its 8 adjacent squares.
    private static boolean isIsolated(Set<Point> elfPositions, Point elf) {
        // In order, test if there is an elf in directions: NW, N, NE, W, E, SW, S, SE
        return !elfPositions.contains(new Point(elf.x - 1, elf.y - 1)) &&
                !elfPositions.contains(new Point(elf.x, elf.y - 1)) &&
                !elfPositions.contains(new Point(elf.x + 1, elf.y - 1)) &&
                !elfPositions.contains(new Point(elf.x - 1, elf.y)) &&
                !elfPositions.contains(new Point(elf.x + 1, elf.y)) &&
                !elfPositions.contains(new Point(elf.x - 1, elf.y + 1)) &&
                !elfPositions.contains(new Point(elf.x, elf.y + 1)) &&
                !elfPositions.contains(new Point(elf.x + 1, elf.y + 1));
    }

    // Each elf checks if there are any elves around them. If there are, then in the order of the directions
    // checks to see if there are any elves in that direction and proposes a new position to move to.
    private static Map<Point, Integer> proposePositions(List<Elf> elves, LinkedList<ElfDirection> directions) {
        Map<Point, Integer> proposedPositions = new HashMap<>();
        Set<Point> elfPositions = new HashSet<>();
        for (Elf elf : elves) {
            elfPositions.add(elf.getPosition());
        }

        for (Elf elf : elves) {
            Point elfPosition = elf.getPosition();

            // If an elf has no neighbors, it doesn't move.
            if (isIsolated(elfPositions, elfPosition)) {
                continue;
            }

            // Otherwise, the elf looks in each of the four directions
            // and proposes moving one step in the first valid direction.
            for (ElfDirection dir : directions) {
                Point proposedPosition = dir.checkDirection(elfPositions, elfPosition);

                // If a given direction does not succeed our elf test, we move on to the next direction.
                if (proposedPosition == null)
                    continue;

                // Otherwise, we set our elf's proposed position.
                elf.setProposedPosition(proposedPosition);

                // Store the elf positions in a map which we use later to determine if there are any
                // two proposed positions that are the same.
                if (proposedPositions.containsKey(proposedPosition)) {
                    proposedPositions.put(proposedPosition, proposedPositions.get(proposedPosition) + 1);
                } else {
                    proposedPositions.put(proposedPosition, 1);
                }

                // As soon as we propose a direction, we don't need to test other directions.
                break;
            }
        }

        return proposedPositions;
    }

    // For each elf, move the elf if it has a unique proposed position.
    // Reset all the proposed positions to null afterwards.
    // Returns true if at least one elf moved. Returns false otherwise.
    private static boolean moveElves(List<Elf> elves, Map<Point, Integer> proposedPositions) {
        boolean elfMoved = false;

        for (Elf elf : elves) {
            Point proposedPosition = elf.getProposedPosition();

            // If an elf has a unique proposed position, move the elf to that position.
            if (proposedPosition != null && proposedPositions.get(proposedPosition) == 1) {
                elfMoved = true;
                elf.setPosition(proposedPosition);
            }
        }

        for (Elf elf : elves) {
            elf.setProposedPosition(null);
        }

        return elfMoved;
    }

    // Count the number of empty tiles found in the smallest bounding box that contains every elf.
    private static int calculateEmptyGroundTiles(List<Elf> elves) {
        int emptyTiles = 0;
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        Set<Point> elfPositions = new HashSet<>();

        // Calculate the bounds of the bounding box.
        for (Elf elf : elves) {
            minX = Math.min(minX, elf.getPosition().x);
            maxX = Math.max(maxX, elf.getPosition().x);
            minY = Math.min(minY, elf.getPosition().y);
            maxY = Math.max(maxY, elf.getPosition().y);

            elfPositions.add(elf.getPosition());
        }

        for (int row = minY; row <= maxY; row++) {
            for (int col = minX; col <= maxX; col++) {
                // If an elf is there, then it's not an empty tile.
                if (elfPositions.contains(new Point(col, row))) {
                    continue;
                }
                emptyTiles++;
            }
        }

        return emptyTiles;
    }

    // Part 1: Simulate ten rounds of elf movement. For every elf, we find whether it proposes a position
    // to move to. If an elf proposes a unique position, it moves to that position.
    private static int part1(List<Elf> elves) {
        // Initial directions ordering: N, S, W, E.
        LinkedList<ElfDirection> directions = new LinkedList<>();
        directions.add(new ElfDirectionNorth());
        directions.add(new ElfDirectionSouth());
        directions.add(new ElfDirectionWest());
        directions.add(new ElfDirectionEast());

        for (int i = 0 ; i < 10; i++) {
            // Retrieve a mapping for all the proposed positions to their frequency.
            Map<Point, Integer> proposedPositions = proposePositions(elves, directions);

            moveElves(elves, proposedPositions);

            // Move the first direction the elves considered to the end of the list of directions.
            directions.add(directions.remove(0));
        }

        return calculateEmptyGroundTiles(elves);
    }

    // Part 2: Similar to part 1, instead of simulating a static 10 rounds, we simulate as many rounds as needed
    // until equilibrium is reached (i.e. no elves need to move - each elf has no eleves in its 8 adjacent points).
    private static int part2(List<Elf> elves) {
        int round = 0;

        // Initial directions ordering: N, S, W, E.
        LinkedList<ElfDirection> directions = new LinkedList<>();
        directions.add(new ElfDirectionNorth());
        directions.add(new ElfDirectionSouth());
        directions.add(new ElfDirectionWest());
        directions.add(new ElfDirectionEast());

        boolean elfMoved;
        do {
            // Retrieve a mapping for all the proposed positions to their frequency.
            Map<Point, Integer> proposedPositions = proposePositions(elves, directions);

            elfMoved = moveElves(elves, proposedPositions);

            // Move the first direction the elves considered to the end of the list of directions.
            directions.add(directions.remove(0));
            round++;
        } while(elfMoved);

        return round;
    }
}