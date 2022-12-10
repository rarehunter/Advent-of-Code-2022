import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

enum Direction {
    UP, DOWN, LEFT, RIGHT
}

// Represents a knot in the rope
class Knot {
    private final Point position;

    public Knot(Point p) {
        this.position = p;
    }

    public Point getPosition() { return position; }
    public void updatePosition(int x, int y) {
        position.x = x;
        position.y = y;
    }
}

// Represents a rope. A rope has a list of knots along with a series of methods to
// simulate a step in the transformation of the rope in a given direction.
class Rope {
    private final List<Knot> knots;

    public Rope(int numKnots) {
        knots = new ArrayList<>();

        // Initialize each knot to start a position (0,0)
        for (int i = 0; i < numKnots; i++) {
            knots.add(new Knot(new Point(0,0)));
        }
    }

    public Knot getHead() { return knots.get(0); }
    public Knot getTail() { return knots.get(knots.size() - 1); }
    public Knot getKnotN(int n) { return knots.get(n); }

    // Returns true oif the two knots are touching.
    // Touching is defined as if the two knots are adjacent to each other (including diagonal adjacency)
    // or are overlapping each other.
    public boolean areKnotsTouching(Knot knot1, Knot knot2) {
        Point pos1 = knot1.getPosition();
        Point pos2 = knot2.getPosition();

        // If they are overlapping each other
        if (pos1.x == pos2.x && pos1.y == pos2.y) {
            return true;
        }

        // If they are diagonally touching
        if (Math.abs(pos1.x - pos2.x) == 1 && Math.abs(pos1.y - pos2.y) == 1) {
            return true;
        }

        // If they are in the same column (x direction) and
        // only one away from each other in the rows (y-direction)
        if (pos1.x == pos2.x && Math.abs(pos1.y - pos2.y) == 1) {
            return true;
        }

        // If they are in the same row (y direction) and
        // only one away from each other in the columns (x-direction)
        return pos1.y == pos2.y && Math.abs(pos1.x - pos2.x) == 1;
    }

    // Returns true if the two knots are in the same line, exactly two steps away from each other.
    // Returns false otherwise.
    private boolean isKnotTwoStepsAwayInTheSameDirection(Knot k1, Knot k2) {
        // If the knots are in the same column, check that the difference in y value is exactly 2.
        if (k1.getPosition().x == k2.getPosition().x) {
            return Math.abs(k1.getPosition().y - k2.getPosition().y) == 2;
        } else if (k1.getPosition().y == k2.getPosition().y) {
            // If the knots are in the same row, check that the difference in x value is exactly 2.
            return Math.abs(k1.getPosition().x - k2.getPosition().x) == 2;
        }

        return false;
    }

    // Update a given knot's position by one step in a given direction.
    private void moveKnotInSingleDirection(Knot knot, Direction direction) {
        Point knotPosition = knot.getPosition();
        if (direction.equals(Direction.UP))
            knot.updatePosition(knotPosition.x, knotPosition.y + 1);
        else if (direction.equals(Direction.DOWN))
            knot.updatePosition(knotPosition.x, knotPosition.y - 1);
        else if (direction.equals(Direction.LEFT))
            knot.updatePosition(knotPosition.x - 1, knotPosition.y);
        else if (direction.equals(Direction.RIGHT))
            knot.updatePosition(knotPosition.x + 1, knotPosition.y);
    }

    // Given two knots in a line, a lead and a follower, moves the follower towards the lead.
    private void moveFollowerTowardsLeadInSingleDirection(Knot lead, Knot follower) {
        Point leadPosition = lead.getPosition();
        Point followerPosition = follower.getPosition();

        if (leadPosition.x == followerPosition.x) {
            // Follower needs to move up
            if (leadPosition.y > followerPosition.y) {
                follower.updatePosition(followerPosition.x, followerPosition.y + 1);
            } else { // Follower needs to move down
                follower.updatePosition(followerPosition.x, followerPosition.y - 1);
            }
        } else if (leadPosition.y == followerPosition.y) {
            // Follower needs to move left
            if (leadPosition.x < followerPosition.x) {
                follower.updatePosition(followerPosition.x - 1, followerPosition.y);
            } else { // Follower needs to move right
                follower.updatePosition(followerPosition.x + 1, followerPosition.y);
            }
        }
    }

    // Given two knots (a lead and a follower) that are neither touching nor are in the same row or column,
    // move the follower towards the lead.
    private void moveKnotDiagonally(Knot lead, Knot follower) {
        Point leadPosition = lead.getPosition();
        Point followerPosition = follower.getPosition();

        // If the lead knot is in the top-right quadrant relative to the follower,
        // then move the follower one step to the top-right.
        if (leadPosition.x > followerPosition.x && leadPosition.y > followerPosition.y) {
            follower.updatePosition(followerPosition.x + 1, followerPosition.y + 1);
            return;
        }

        // If the lead knot is in the top-left quadrant relative to the follower,
        // then move the follower one step to the top-left.
        if (leadPosition.x < followerPosition.x && leadPosition.y > followerPosition.y) {
            follower.updatePosition(followerPosition.x - 1, followerPosition.y + 1);
            return;
        }

        // If the lead knot is in the bottom-left quadrant relative to the follower,
        // then move the follower one step to the bottom-left.
        if (leadPosition.x < followerPosition.x && leadPosition.y < followerPosition.y) {
            follower.updatePosition(followerPosition.x - 1, followerPosition.y - 1);
            return;
        }

        // If the lead knot is in the bottom-right quadrant relative to the follower,
        // then move the follower one step to the bottom-right.
        if (leadPosition.x > followerPosition.x && leadPosition.y < followerPosition.y) {
            follower.updatePosition(followerPosition.x + 1, followerPosition.y - 1);
        }
    }

    // Performs a single rope transformation in the given direction
    public void step(Direction direction) {
        Knot headKnot = getHead();

        // Move the head knot's location one step in the current direction.
        moveKnotInSingleDirection(headKnot, direction);

        for (int i = 1; i < knots.size(); i++) {
            Knot leadKnot = getKnotN(i - 1);
            Knot followerKnot = getKnotN(i);

            // If the lead knot is ever two steps directly up, down, left, or right from the follower knot,
            // the follower must also move one step in that direction, so it remains close enough.
            if (isKnotTwoStepsAwayInTheSameDirection(followerKnot, leadKnot)) {
                moveFollowerTowardsLeadInSingleDirection(leadKnot, followerKnot);
                continue;
            }
            // Otherwise, if the lead and follower knots aren't touching and aren't in the same row or column,
            // the follower always moves one step diagonally to keep up.
            else if (!areKnotsTouching(followerKnot, leadKnot) &&
                    leadKnot.getPosition().x != followerKnot.getPosition().x &&
                    leadKnot.getPosition().y != followerKnot.getPosition().y){
                moveKnotDiagonally(leadKnot, followerKnot);
                continue;
            }

            // If no movement happened, there's no need to move any other further knots.
            break;
        }
    }
}

// Represents an entry in the input file, recording a direction and a magnitude.
record Motion(Direction direction, int steps) { }

public class Day9_Rope_Bridge {
    public static void main(String[] args) {
        File file = new File("./inputs/day9/day9.txt");

        try {
            Scanner sc = new Scanner(file);
            List<Motion> motions = new ArrayList<>();

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] tokens = line.split(" ");

                Direction direction = null;

                switch (tokens[0]) {
                    case "U" -> direction = Direction.UP;
                    case "D" -> direction = Direction.DOWN;
                    case "L" -> direction = Direction.LEFT;
                    case "R" -> direction = Direction.RIGHT;
                    default -> {
                    }
                }
                motions.add(new Motion(direction, Integer.parseInt(tokens[1])));
            }

            int part1 = part1(motions);
            System.out.println("Part 1 is: " + part1);

            Rope rope = new Rope(10);

            int part2 = part2(rope, motions);
            System.out.println("Part 2 is: " + part2);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private static boolean areHeadTailTouching(Point head, Point tail) {
        // If they are overlapping each other
        if (head.x == tail.x && head.y == tail.y) {
            return true;
        }

        // If they are diagonally touching
        if (Math.abs(head.x - tail.x) == 1 && Math.abs(head.y - tail.y) == 1) {
            return true;
        }

        // If they are in the same column (x direction) and
        // only one away from each other in the rows (y-direction)
        if (head.x == tail.x && Math.abs(head.y - tail.y) == 1) {
            return true;
        }

        // If they are in the same row (y direction) and
        // only one away from each other in the columns (x-direction)
        return head.y == tail.y && Math.abs(head.x - tail.x) == 1;
    }

    // Part 1: Simulates the movement of a rope with two knots, a head and a tail.
    // Stores the positions that the tail visited in a set. This algorithm assumes that
    // the tail will follow the last position of the hand as the rope travels around the grid,
    // an assumption that works for part 1 of the problem but does not work for part 2.
    private static int part1(List<Motion> motions) {
        Set<Point> set = new HashSet<>();
        Point head = new Point(0,0);
        Point tail = new Point(0,0);
        set.add(tail);

        for (Motion motion : motions) {
            Direction currentDirection = motion.direction();

            Point prevHeadLocation = new Point(0,0);
            for (int i = 0; i < motion.steps(); i++) {
                // Save the location of the head as the tail may move
                // to that location in order to follow the head around.
                prevHeadLocation.x = head.x;
                prevHeadLocation.y = head.y;

                // Update the head location accordingly by direction.
                if (currentDirection.equals(Direction.UP)) head.y++;
                else if (currentDirection.equals(Direction.DOWN)) head.y--;
                else if (currentDirection.equals(Direction.LEFT)) head.x--;
                else if (currentDirection.equals(Direction.RIGHT)) head.x++;

                // If the head and tail are still touching, tail doesn't change.
                // But if the head has moved away from the tail, update the tail accordingly.
                if (!areHeadTailTouching(head, tail)) {
                    tail.x = prevHeadLocation.x;
                    tail.y = prevHeadLocation.y;
                }

                // Save the new location of the tail into our set. We must create a new Point object
                // since we'll be modifying tail's location in our loop.
                set.add(new Point(tail.x, tail.y));
            }
        }

        return set.size();
    }

    // Part 2: Simulates the movement of a rope with ten knots. Store the positions that the tail
    // visited in a set. We rewrote the rope behavior to be more in line with the problem description
    // of how the tail moves (i.e. it does not necessarily always move to the last position of the head).
    // In fact, after this re-write using a Rope and Knot class, part 1 of this problem can be solved
    // using the same code below, just replacing the rope argument with a rope with 2 knots instead of 10.
    private static int part2(Rope rope, List<Motion> motions) {
        // We use a set to store unique positions that the last knot of the rope (i.e. tail)
        // has visited. We start by adding the initial position of the tail to the set.
        Set<Point> set = new HashSet<>();
        set.add(rope.getTail().getPosition());

        // For each motion (direction and magnitude), simulate a step in the rope transformation
        // and store the resulting tail position in our set.
        for (Motion motion : motions) {
            Direction currentDirection = motion.direction();

            for (int i = 0; i < motion.steps(); i++) {
                rope.step(currentDirection);
                Point tail = rope.getTail().getPosition();
                set.add(new Point(tail.x, tail.y));
            }
        }

        return set.size();
    }
}