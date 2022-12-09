import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Day8_Treetop_Tree_House {
    public static void main(String[] args) {
        File file = new File("./inputs/day8/day8.txt");
        int rows = 0;
        int cols = 0;

        try {
            Scanner sc = new Scanner(file);

            // One pass through the input to get the size of the grid
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                cols = line.length();
                rows++;
            }

            // Initialize the grid
            int[][] grid = new int[rows][cols];

            Scanner sc2 = new Scanner(file);
            int row = 0;

            // Second pass through the input to store all the values in our grid.
            while (sc2.hasNextLine()) {
                String line = sc2.nextLine();

                for (int j = 0; j < grid[row].length; j++) {
                    grid[row][j] = Integer.parseInt(String.valueOf(line.charAt(j)));
                }

                row++;
            }

            int part1 = part1(grid);
            System.out.println("Part 1 is: " + part1);

            int part2 = part2(grid);
            System.out.println("Part 2 is: " + part2);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // Returns true if all trees north of the current tree location is shorter than our current tree.
    // Returns false otherwise.
    private static boolean isVisibleFromNorth(int[][] grid, int treeRow, int treeCol) {
        int treeHeight = grid[treeRow][treeCol];
        for (int r = treeRow-1; r >= 0; r--) {
            if (grid[r][treeCol] >= treeHeight) {
                return false;
            }
        }

        return true;
    }

    // Returns true if all trees south of the current tree location is shorter than our current tree.
    // Returns false otherwise.
    private static boolean isVisibleFromSouth(int[][] grid, int treeRow, int treeCol) {
        int treeHeight = grid[treeRow][treeCol];
        for (int r = treeRow+1; r < grid.length; r++) {
            if (grid[r][treeCol] >= treeHeight) {
                return false;
            }
        }

        return true;
    }

    // Returns true if all tress east of the current tree location is shorter than our current tree.
    // Returns false otherwise.
    private static boolean isVisibleFromEast(int[][] grid, int treeRow, int treeCol) {
        int treeHeight = grid[treeRow][treeCol];
        for (int c = treeCol+1; c < grid[0].length; c++) {
            if (grid[treeRow][c] >= treeHeight) {
                return false;
            }
        }

        return true;
    }

    // Returns true if all trees west of the current tree location is shorter than our current tree.
    // Returns false otherwise.
    private static boolean isVisibleFromWest(int[][] grid, int treeRow, int treeCol) {
        int treeHeight = grid[treeRow][treeCol];
        for (int c = treeCol-1; c >= 0; c--) {
            if (grid[treeRow][c] >= treeHeight) {
                return false;
            }
        }

        return true;
    }

    // Part 1: Calculates the number of trees visible from outside the grid.
    // The trees on the edge are automatically considered visible so we just count those up.
    // For all the trees in the interior, iterate through each one and cycle through the four cardinal directions,
    // determining if the tree is visible from that direction.
    private static int part1(int[][] grid) {
        int edgeTrees = grid.length * 2 + (grid[0].length - 2) * 2;
        int internalVisibleTrees = 0;

        for (int r = 1; r < grid.length - 1; r++) {
            for (int c = 1; c < grid[r].length - 1; c++) {
                if (isVisibleFromNorth(grid, r, c) ||
                    isVisibleFromSouth(grid, r, c) ||
                    isVisibleFromEast(grid, r, c) ||
                    isVisibleFromWest(grid, r, c)) {
                    internalVisibleTrees++;
                }
            }
        }

        return edgeTrees + internalVisibleTrees;
    }

    // Starting from the current tree, returns the number of trees to the north that are shorter than the current tree,
    // stopping when we hit a tree that is of equal height or taller.
    private static int scenicDistanceFromNorth(int[][] grid, int treeRow, int treeCol) {
        int scenicDistance = 0;
        int treeHeight = grid[treeRow][treeCol];
        for (int r = treeRow-1; r >= 0; r--) {
            scenicDistance++;
            if (grid[r][treeCol] >= treeHeight) {
                break;
            }
        }

        return scenicDistance;
    }

    // Starting from the current tree, returns the number of trees to the south that are shorter than the current tree,
    // stopping when we hit a tree that is of equal height or taller.
    private static int scenicDistanceFromSouth(int[][] grid, int treeRow, int treeCol) {
        int scenicDistance = 0;
        int treeHeight = grid[treeRow][treeCol];
        for (int r = treeRow+1; r < grid.length; r++) {
            scenicDistance++;
            if (grid[r][treeCol] >= treeHeight) {
                break;
            }
        }

        return scenicDistance;
    }

    // Starting from the current tree, returns the number of trees to the east that are shorter than the current tree,
    // stopping when we hit a tree that is of equal height or taller.
    private static int scenicDistanceFromEast(int[][] grid, int treeRow, int treeCol) {
        int scenicDistance = 0;
        int treeHeight = grid[treeRow][treeCol];
        for (int c = treeCol+1; c < grid[0].length; c++) {
            scenicDistance++;
            if (grid[treeRow][c] >= treeHeight) {
                break;
            }
        }

        return scenicDistance;
    }

    // Starting from the current tree, returns the number of trees to the west that are shorter than the current tree,
    // stopping when we hit a tree that is of equal height or taller.
    private static int scenicDistanceFromWest(int[][] grid, int treeRow, int treeCol) {
        int scenicDistance = 0;
        int treeHeight = grid[treeRow][treeCol];
        for (int c = treeCol-1; c >= 0; c--) {
            scenicDistance++;
            if (grid[treeRow][c] >= treeHeight) {
                break;
            }
        }

        return scenicDistance;
    }

    // Part 2: Finds the maximum scenic score of the tree grid. Since the scenic score is the product
    // of its respective values of the four cardinal directions, and trees on the edge must have one of its
    // values be 0, then the scenic score of trees on the edge is 0, so we don't bother calculating those.
    // We iterate through the tree grid of the trees in the interior, calculating the scenic distance
    // values and multiplying them all together.
    private static int part2(int[][] grid) {
        int maxScenicScore = 0;

        for (int r = 1; r < grid.length - 1; r++) {
            for (int c = 1; c < grid[r].length - 1; c++) {
                int scenicScore = scenicDistanceFromNorth(grid, r, c) *
                        scenicDistanceFromSouth(grid, r, c) *
                        scenicDistanceFromEast(grid, r, c) *
                        scenicDistanceFromWest(grid, r, c);
                maxScenicScore = Math.max(scenicScore, maxScenicScore);
            }
        }

        return maxScenicScore;
    }
}