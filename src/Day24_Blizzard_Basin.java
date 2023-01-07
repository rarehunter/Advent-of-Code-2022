import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.*;

enum BlizzardDirection {
    UP, DOWN, LEFT, RIGHT
}

// Represents a blizzard which has a position and a direction.
record Blizzard(Point position, BlizzardDirection direction) {}

// Represents the state of the BFS traversal.
// We need to store where we are at what minute in time.
record BFSState(int minute, Point position) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BFSState bfsState = (BFSState) o;
        return minute == bfsState.minute && position.equals(bfsState.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minute, position);
    }
}

public class Day24_Blizzard_Basin {
    public static void main(String[] args) {
        File file = new File("./inputs/day24/day24.txt");
        List<Blizzard> initialBlizzards = new ArrayList<>();
        Set<Point> walls = new HashSet<>();
        int width = 0;
        int rows = 0;

        try {
            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                width = line.length();

                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);

                    // If it's a wall, add it to our set of walls and move on.
                    if (c == '#') {
                        walls.add(new Point(i, rows));
                        continue;
                    }

                    // Otherwise, it's either a blizzard or an empty.
                    BlizzardDirection dir;
                    switch (c) {
                        case '<': dir = BlizzardDirection.LEFT; break;
                        case '>': dir = BlizzardDirection.RIGHT; break;
                        case '^': dir = BlizzardDirection.UP; break;
                        case 'v': dir = BlizzardDirection.DOWN; break;
                        default: continue; // If it's an empty, move on.
                    }

                    initialBlizzards.add(new Blizzard(new Point(i, rows), dir));
                }

                rows++;
            }

            Map<Integer, Set<Point>> blizzardState = calculateBlizzardState(initialBlizzards, width, rows);

            int part1 = part1(blizzardState, walls, width, rows);
            System.out.println("Part 1 is: " + part1);

            int part2 = part2(blizzardState, walls, width, rows);
            System.out.println("Part 2 is: " + part2);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // Pre-compute all the possible states that the blizzards could be in for fast lookup later.
    // Returns a HashMap of the blizzard positions keyed on each minute in time.
    private static Map<Integer, Set<Point>> calculateBlizzardState(List<Blizzard> blizzards, int width, int rows) {
        Map<Integer, Set<Point>> blizzardState = new HashMap<>();

        // Instead of using the width and height arguments directly, the size of the actual blizzard area is
        // width - 2 and height - 2 because the topmost, bottommost, leftmost and rightmost sides are walls.
        // Note: We currently use the product of the width and height of the grid but in reality, this can be further
        // optimized as the least-common multiple of width and height. At some point in time (a multiple of the
        // width and height), the blizzard positions begin repeating, so we don't need to calculate them all over again.
        for (int i = 0; i < (width - 2) * (rows - 2); i++) {
            Set<Point> blizzardPositions = getBlizzardPositions(blizzards);
            blizzardState.put(i, blizzardPositions);
            blizzards = stepBlizzards(blizzards, width, rows);
        }

        return blizzardState;
    }

    // Prints the grid of walls, ground, and blizzard directions to the console.
    // Used for debugging purposes.
    private static void printGrid(List<Blizzard> blizzards, int width, int rows) {
        Map<Point, List<Blizzard>> map = new HashMap<>();

        for (Blizzard b : blizzards) {
            if (map.containsKey(b.position())) {
                map.get(b.position()).add(b);
            } else {
                List<Blizzard> blizz = new ArrayList<>();
                blizz.add(b);
                map.put(b.position(), blizz);
            }
        }

        // Print the top wall.
        for (int x = 0; x < width; x++) {
            if (x == 1) System.out.print(".");
            else System.out.print("#");
        }

        System.out.println();

        // Print the blizzards
        for (int i = 1; i < rows-1; i++) {
            for (int j = 0; j < width; j++) {
                // Print the left or right walls.
                if (j == 0 || j == width - 1) {
                    System.out.print("#");
                    continue;
                }

                Point blizzardPos = new Point(j, i);
                // If there's a blizzard at this point, print either the direction of the blizzard if there's only
                // a single one or the number of blizzards in that position if there are more than one.
                if (map.containsKey(blizzardPos)) {
                    if (map.get(blizzardPos).size() > 1) {
                        System.out.print(map.get(blizzardPos).size());
                    } else {
                        BlizzardDirection dir = map.get(blizzardPos).get(0).direction();
                        switch(dir) {
                            case UP -> System.out.print("^");
                            case DOWN -> System.out.print("v");
                            case LEFT -> System.out.print("<");
                            case RIGHT -> System.out.print(">");
                        }
                    }
                }  else { // If there are no blizzards at this position, print a dot.
                    System.out.print(".");
                }
            }
            System.out.println();
        }

        // Print the bottom wall.
        for (int x = 0; x < width; x++) {
            if (x == width - 2) System.out.print(".");
            else System.out.print("#");
        }

        System.out.println();
    }

    // Simulates one step of the blizzard movements, wrapping around if hitting a wall.
    private static List<Blizzard> stepBlizzards(List<Blizzard> blizzards, int width, int rows) {
        List<Blizzard> newList = new ArrayList<>();

        for (Blizzard blizzard : blizzards) {
            Point p = blizzard.position();
            BlizzardDirection dir = blizzard.direction();

            if (dir.equals(BlizzardDirection.LEFT)) {
                // If a left movement, moves the blizzard into the left-column,
                // we recreate the blizzard at the rightmost wall.
                if (p.x - 1 <= 0) {
                    newList.add(new Blizzard(new Point(width - 2, p.y), BlizzardDirection.LEFT));
                    //blizzard.setBlizzard(width - 2, p.y);
                } else {
                    newList.add(new Blizzard(new Point(p.x - 1, p.y), BlizzardDirection.LEFT));

                    //blizzard.setBlizzard(p.x - 1, p.y);
                }
            } else if (dir.equals(BlizzardDirection.RIGHT)) {
                // If a right movement, moves the blizzard into the right-column,
                // we recreate the blizzard at the leftmost wall.
                if (p.x + 1 >= width - 1) {
                    newList.add(new Blizzard(new Point(1, p.y), BlizzardDirection.RIGHT));

                    //blizzard.setBlizzard(1, p.y);
                } else {
                    newList.add(new Blizzard(new Point(p.x + 1, p.y), BlizzardDirection.RIGHT));

                    //blizzard.setBlizzard(p.x + 1, p.y);
                }
            } else if (dir.equals(BlizzardDirection.UP)) {
                // If an upwards movement, moves the blizzard into the top wall,
                // we recreate the blizzard at the bottom wall.
                if (p.y - 1 <= 0) {
                    newList.add(new Blizzard(new Point(p.x, rows-2), BlizzardDirection.UP));

                    //blizzard.setBlizzard(p.x, rows - 2);
                } else {
                    newList.add(new Blizzard(new Point(p.x, p.y-1), BlizzardDirection.UP));

                    //blizzard.setBlizzard(p.x, p.y - 1);
                }
            } else if (dir.equals(BlizzardDirection.DOWN)) {
                // If a downwards movement, moves the blizzard into the bottom wall,
                // we recreate the blizzard at the top wall.
                if (p.y + 1 >= rows - 1) {
                    newList.add(new Blizzard(new Point(p.x, 1), BlizzardDirection.DOWN));

                    //blizzard.setBlizzard(p.x, 1);
                } else {
                    newList.add(new Blizzard(new Point(p.x, p.y+1), BlizzardDirection.DOWN));

                    //blizzard.setBlizzard(p.x, p.y + 1);
                }
            }
        }

        return newList;
    }

    // Returns a list of valid adjacent positions to move to (including staying the same position if valid).
    // A valid position is one that is not a wall or occupied by a blizzard.
    private static List<Point> getNextPoints(Set<Point> blizzardPositions, Set<Point> walls, Point p, int rows) {
        List<Point> next = new ArrayList<>();

        Point upNeighbor = new Point(p.x, p.y - 1);
        Point downNeighbor = new Point(p.x, p.y + 1);
        Point leftNeighbor = new Point(p.x - 1, p.y);
        Point rightNeighbor = new Point(p.x + 1, p.y);

        // Upwards neighbor is valid if it isn't a wall and if there isn't a blizzard at that point.
        // Also, we prevent ourselves from past the bounds of the grid in the case of the starting position.
        if (!walls.contains(upNeighbor) && !blizzardPositions.contains(upNeighbor) && p.y - 1 >= 0) {
            next.add(upNeighbor);
        }

        // Downwards neighbor is valid if it isn't a wall and if there isn't a blizzard at that point.
        if (!walls.contains(downNeighbor) && !blizzardPositions.contains(downNeighbor) && p.y + 1 < rows) {
            next.add(downNeighbor);
        }

        // Left neighbor is valid if it isn't a wall and if there isn't a blizzard at that point.
        if (!walls.contains(leftNeighbor) && !blizzardPositions.contains(leftNeighbor)) {
            next.add(leftNeighbor);
        }

        // Right neighbor is valid if it isn't a wall and if there isn't a blizzard at that point.
        if (!walls.contains(rightNeighbor) && !blizzardPositions.contains(rightNeighbor)) {
            next.add(rightNeighbor);
        }

        // Finally, it's possible to remain in the current location without moving if there are no blizzards.
        if (!blizzardPositions.contains(p)) {
            next.add(p);
        }


        return next;
    }

    // Returns a set of all the blizzard positions.
    private static Set<Point> getBlizzardPositions(List<Blizzard> blizzards) {
        Set<Point> pos = new HashSet<>();

        for (Blizzard b : blizzards) {
            pos.add(b.position());
        }

        return pos;
    }

    // Part 1: Implement a breadth-first search (BFS) through the grid. We precompute the positions of the blizzards
    // because the observation is that after a certain amount of time, the blizzard positions start to repeat so
    // there is no need to constantly calculate it. We are given a map of these blizzard positions keyed on a certain
    // minute in time. We need to keep track of the position we're in as well as what time it is.
    // Then, we simply run a BFS because on an unweighted graph like this problem is, the very first path you find from
    // the start to the exit will also be the shortest one.
    private static int part1(Map<Integer, Set<Point>> blizzards, Set<Point> walls, int width, int rows) {
        Point start = new Point(1, 0);
        Point end = new Point(width - 2,rows - 1);

        BFSState startState = new BFSState(0, start);
        Queue<BFSState> queue = new LinkedList<>();
        queue.add(startState);
        Set<BFSState> visited = new HashSet<>();

        while (true) {
            // We are at this position at this minute in time.
            BFSState current = queue.poll();
            visited.add(current);

            // If we've reached the end, we terminate.
            if (current.position().equals(end)) {
                return current.minute();
            }

            int currentMinute = current.minute();
            int nextMinute = currentMinute + 1;
            Point currentPosition = current.position();

            // At the next minute in time, get the blizzard positions.
            Set<Point> blizzardPositions = blizzards.get(nextMinute % ((width - 2) * (rows - 2)));

            List<Point> neighbors = getNextPoints(blizzardPositions, walls, currentPosition, rows);

            for (Point neighbor : neighbors) {
                BFSState nextState = new BFSState(nextMinute, neighbor);
                if (!visited.contains(nextState)) {
                    queue.add(nextState);
                    visited.add(nextState);
                }
            }
        }
    }

    // Returns true if the given point is a terminal location. A terminal location is defined as the bottom right position
    // during the first traversal, the top left position during the second traversal, and the bottom right position during
    // the third traversal.
    private static boolean isTerminalLocation(Point p, Point start, Point end, int traversals) {
        return (p.equals(end) && traversals != 2) || (p.equals(start) && traversals != 1 && traversals != 3);
    }

    // Part 2: In this part, we simply run the same algorithm as in part1 but with a total of three traversals.
    // If we've reached the end, we go back to the start and then go back to the end.
    // We return the total time it takes for those three traversals.
    private static int part2(Map<Integer, Set<Point>> blizzards, Set<Point> walls, int width, int rows) {
        Point start = new Point(1, 0);
        Point end = new Point(width - 2,rows - 1);

        BFSState startState = new BFSState(0, start);
        Queue<BFSState> queue = new LinkedList<>();
        queue.add(startState);
        Set<BFSState> visited = new HashSet<>();

        int traversals = 1; // Start with the first traversal (from start to end).

        while (true) {
            // We are at this position at this minute in time.
            BFSState current = queue.poll();
            visited.add(current);

            // If we've reached the end, we go back to the start and then go back to the end.
            // We return the sum of all those times.
            if (isTerminalLocation(current.position(), start, end, traversals)) {
                traversals++;
                visited.clear();
                queue.clear();

                // If we're starting the second traversal (from end back to start), start at the end position.
                if (traversals == 2) {
                    queue.add(new BFSState(current.minute(), end));
                } else if (traversals == 3) {
                    // If we're starting the third traversal (from start back to the end)
                    // start at the start position again.
                    queue.add(new BFSState(current.minute(), start));
                }

                // Once we've completed all three traversals, we terminate.
                if (traversals == 4) {
                    return current.minute();
                }

                continue;
            }

            int currentMinute = current.minute();
            int nextMinute = currentMinute + 1;
            Point currentPosition = current.position();

            // At the next minute in time, get the blizzard positions.
            Set<Point> blizzardPositions = blizzards.get(nextMinute % ((width - 2) * (rows - 2)));

            List<Point> neighbors = getNextPoints(blizzardPositions, walls, currentPosition, rows);

            for (Point neighbor : neighbors) {
                BFSState nextState = new BFSState(nextMinute, neighbor);
                if (!visited.contains(nextState)) {
                    queue.add(nextState);
                    visited.add(nextState);
                }
            }
        }
    }
}