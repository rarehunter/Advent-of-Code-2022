import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Day14_Regolith_Reservoir {
    private static final char SAND_AT_REST = 'o';
    private static final char EMPTY = '.';
    private static final char ROCK = '#';

    public static void main(String[] args) {
        File file = new File("./inputs/day14/day14.txt");
        int maxX = 0;
        int maxY = 0;
        List<List<Point>> rockPaths = new ArrayList<>();

        try {
            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                List<Point> points = new ArrayList<>();

                String[] tokens = line.split(" -> ");
                for (String token : tokens) {
                    String[] pointRep = token.split(",");
                    int x = Integer.parseInt(pointRep[0]);
                    int y = Integer.parseInt(pointRep[1]);

                    maxX = Math.max(x, maxX);
                    maxY = Math.max(y, maxY);
                    Point p = new Point(x, y);
                    points.add(p);
                }

                rockPaths.add(points);
            }

            char[][] grid = constructGrid(rockPaths, maxX, maxY);

            // For part 1, there is an infinite abyss at the edge of the grid.
            int part1 = part1(grid);
            System.out.println("Part 1 is: " + part1);

            // For part 2, we discover an infinite rock floor at the bottom of the grid.
            List<Point> infiniteRockFloor = new ArrayList<>();
            infiniteRockFloor.add(new Point(0, maxY + 2));
            infiniteRockFloor.add(new Point(maxX + maxY, maxY + 2)); // Stretch the rock across the bottom
            rockPaths.add(infiniteRockFloor);

            // A sand can at most travel the distance of maxY so let's overshoot
            // and set the width of the grid to maxX + maxY;
            grid = constructGrid(rockPaths, maxX + maxY, maxY + 2);

            int part2 = part2(grid);
            System.out.println("Part 2 is: " + part2);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // Helper method to initialize a grid with rock paths.
    private static char[][] constructGrid(List<List<Point>> rockPaths, int maxX, int maxY) {
        char[][] grid = new char[maxY + 1][maxX + 1];
        for (char[] row : grid) {
            Arrays.fill(row, EMPTY);
        }

        for (List<Point> points : rockPaths) {
            for (int i = 0; i < points.size() - 1; i++) {
                Point p1 = points.get(i);
                Point p2 = points.get(i + 1);
                drawLine(grid, p1, p2);
            }
        }

        return grid;
    }

    // Given two points, fills in the cells of the grid with '#' character.
    private static void drawLine(char[][] grid, Point p1, Point p2) {
        // Draws a vertical line
        if (p1.x == p2.x) {
            for (int y = Math.min(p1.y, p2.y); y <= Math.max(p1.y, p2.y); y++) {
                grid[y][p1.x] = ROCK;
            }
        } else if (p1.y == p2.y) { // Draws a horizontal line
            for (int x = Math.min(p1.x, p2.x); x <= Math.max(p1.x, p2.x); x++) {
                grid[p1.y][x] = ROCK;
            }
        }
    }

    // Helper method to print a given grid to the console.
    private static void printGrid(char[][] grid) {
        for (char[] row : grid) {
            for (char c : row) {
                System.out.print(c + " ");
            }
            System.out.println();
        }
    }

    // Returns true if the point immediately below the given sand point
    // is either a rock or a piece of sand at rest. The return type is a boxed Boolean
    // so that if the sand exceeds the bounds of the grid, we return null.
    private static Boolean isBlockedBelow(char[][] grid, Point sand) {
        if (sand.y + 1 >= grid.length)
            return null;

        char below = grid[sand.y + 1][sand.x];
        return below == ROCK || below == SAND_AT_REST;
    }

    // Returns true if the point immediately to the bottom left of the given sand point
    // is either a rock or a piece of sand at rest. The return type is a boxed Boolean
    // so that if the sand exceeds the bounds of the grid, we return null.
    private static Boolean isBlockedBottomLeft(char[][] grid, Point sand) {
        if (sand.y + 1 >= grid.length || sand.x - 1 < 0)
            return null;

        char bottomLeft = grid[sand.y + 1][sand.x - 1];
        return bottomLeft == ROCK || bottomLeft == SAND_AT_REST;
    }

    // Returns true if the point immediately to the bottom right of the given sand point
    // is either a rock or a piece of sand at rest. The return type is a boxed Boolean
    // so that if the sand exceeds the bounds of the grid, we return null.
    private static Boolean isBlockedBottomRight(char[][] grid, Point sand) {
        if (sand.y + 1 >= grid.length || sand.x + 1 >= grid[0].length)
            return null;

        char bottomRight = grid[sand.y + 1][sand.x + 1];
        return bottomRight == ROCK || bottomRight == SAND_AT_REST;
    }

    // Returns true if the sand particle is off the grid.
    // Return false otherwise.
    private static boolean isSandOffGrid(char[][] grid, Point newSand) {
        if (isBlockedBelow(grid, newSand) == null ||
            isBlockedBottomLeft(grid, newSand) == null ||
            isBlockedBottomRight(grid, newSand) == null) {
            return true;
        }
        return false;
    }

    // Returns true if the sand is blocked immediately below it, to its bottom-left, and to its
    // bottom-right, indicating that it is at rest. Returns false otherwise.
    private static boolean isSandCompletelyBlocked(char[][] grid, Point newSand) {
        if (isBlockedBelow(grid, newSand) != null && isBlockedBelow(grid, newSand) &&
                isBlockedBottomLeft(grid, newSand) != null && isBlockedBottomLeft(grid, newSand) &&
            isBlockedBottomRight(grid, newSand) != null && isBlockedBottomRight(grid, newSand)) {
            return true;
        }

        return false;
    }

    // Simulate the dropping of a grain of sand into the grid. Returns true if the grain of sand
    // successfully ended up at rest. Returns null if the grain of sand has fallen off the grid boundaries.
    private static Boolean dropSand(char[][] grid) {
        Point newSand = new Point(500, 0);

        // As long as the grain of sand has not fallen off the grid and is at rest with no other place to go,
        // move it downward if it has an open space below it. Otherwise, we move it to its bottom left if
        // that space is open. Otherwise, we move it to its bottom right if that space is open.
        while (!isSandOffGrid(grid, newSand) && !isSandCompletelyBlocked(grid, newSand)) {
            if (!isBlockedBelow(grid, newSand)) {
                newSand.y++;
                continue;
            }

            if (!isBlockedBottomLeft(grid, newSand)) {
                newSand.x--;
                newSand.y++;
                continue;
            }

            if (!isBlockedBottomRight(grid, newSand)) {
                newSand.x++;
                newSand.y++;
            }
        }

        if (isSandOffGrid(grid, newSand)) {
            return null;
        }

        // At this point, the bottom, bottom-left, and bottom-right are
        // all blocked so the sand is at rest.
        grid[newSand.y][newSand.x] = SAND_AT_REST;
        return true;
    }

    // Part 1: Repeatedly drop a grain of sand into grid starting at (500, 0) simulating its cascading
    // effects. Returns the number of grains of sand that it takes before the sand starts falling into
    // the infinite abyss.
    private static int part1(char[][] grid) {
        int numSand = 0;

        while(true) {
            Boolean status = dropSand(grid);

            // A null status indicates that the sand is off the grid. At this point, it has fallen
            // into the infinite abyss, so we're done.
            if (status == null)
                break;

            numSand++;
        }

        return numSand;
    }

    // Simulate the dropping of a grain of sand into the grid. Returns true if the grain of sand
    // successfully ended up at rest. Returns null if the sand entrance at (500, 0) is plugged up.
    private static Boolean dropSandWithInfiniteFloor(char[][] grid) {
        Point newSand = new Point(500, 0);

        // If the sand entrance is plugged up, we're done.
        if (grid[0][500] == SAND_AT_REST) {
            return null;
        }

        // As long as the grain of sand has not fallen off the grid and is at rest with no other place to go,
        // move it downward if it has an open space below it. Otherwise, we move it to its bottom left if
        // that space is open. Otherwise, we move it to its bottom right if that space is open.
        while (!isSandOffGrid(grid, newSand) && !isSandCompletelyBlocked(grid, newSand)) {
            if (!isBlockedBelow(grid, newSand)) {
                newSand.y++;
                continue;
            }

            if (!isBlockedBottomLeft(grid, newSand)) {
                newSand.x--;
                newSand.y++;
                continue;
            }

            if (!isBlockedBottomRight(grid, newSand)) {
                newSand.x++;
                newSand.y++;
            }
        }

        // At this point, the bottom, bottom-left, and bottom-right are
        // all blocked so the sand is at rest.
        grid[newSand.y][newSand.x] = SAND_AT_REST;
        return true;
    }

    // Part 2: Repeatedly drop a grain of sand into grid with an infinite rock floor starting at (500, 0)
    // simulating its cascading effects. Returns the number of grains of sand it takes until the starting entrance
    // of (500, 0) is plugged up.
    private static int part2(char[][] grid) {
        int numSand = 0;

        while(true) {
            Boolean status = dropSandWithInfiniteFloor(grid);

            // A null status indicates that the entrance (500, 0) is plugged up (i.e. has a grain in
            // its location), so we're done.
            if (status == null)
                break;

            numSand++;
        }

        return numSand;
    }
}