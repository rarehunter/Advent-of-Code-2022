import java.io.File;
import java.io.IOException;
import java.util.*;

record DeviceFile(String name, long size) { }

class Directory {
    private final String name;
    private final List<Directory> directories;
    private final List<DeviceFile> files;
    private Directory parent;
    private long size; // The size of a directory is the sum of the sizes of all of its children files

    public Directory(String name) {
        this.name = name;
        directories = new ArrayList<>();
        files = new ArrayList<>();
    }

    public String getName() { return this.name; }
    public List<Directory> getDirectories() { return this.directories; }
    public List<DeviceFile> getFiles() { return this.files; }
    public Directory getParent() { return this.parent; }
    public long getSize() { return this.size; }
    public void setParent(Directory parent) { this.parent = parent; }
    public void addSize(long size) { this.size += size; }

    public String toString() {
        return this.name + ": " + this.size;
    }
}

public class Day7_No_Space_Left_On_Device {
    private static final String CHANGE_DIRECTORY_COMMAND = "cd";
    private static final String LIST_COMMAND = "ls";
    private static final String BACK_DIRECTORY = "..";
    private static final String ROOT_DIRECTORY = "/";
    private static final String CONSOLE_PREFIX = "$";
    private static final String DIRECTORY_IDENTIFIER = "dir";
    private static final long FILE_SYSTEM_DISK_SPACE = 70000000;
    private static final long SPACE_REQUIRED_TO_UPDATE = 30000000;

    public static void main(String[] args) {
        File file = new File("./inputs/day7/day7.txt");
        Directory root = new Directory(ROOT_DIRECTORY);

        try {
            Scanner sc = new Scanner(file);
            sc.nextLine(); // read the first line which is "$ cd /" and move on.
            Directory currentActiveDirectory = root;

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] tokens = line.split(" ");

                // If we're given a command, figure out what command it is. (e.g. string starts with '$')
                if (tokens[0].equals(CONSOLE_PREFIX)) {
                    if (tokens[1].equals(CHANGE_DIRECTORY_COMMAND)) { // "cd" command
                        if (tokens[2].equals(BACK_DIRECTORY)) { // "cd .."
                            currentActiveDirectory = currentActiveDirectory.getParent();
                        } else { // Going down into a directory (e.g. "cd <directory name>")
                            currentActiveDirectory = currentActiveDirectory.getDirectories()
                                    .stream()
                                    .filter(d -> d.getName().equals(tokens[2]))
                                    .findFirst()
                                    .orElse(null);
                        }
                    } else if (tokens[1].equals(LIST_COMMAND)) {
                        // For "ls" commands, no action needs to be done so move on.
                        // We expect that the next few lines will contain the contents of the directory.
                        continue;
                    }
                } else if (tokens[0].equals(DIRECTORY_IDENTIFIER)) {
                    // We found a directory.
                    Directory d = new Directory(tokens[1]);
                    d.setParent(currentActiveDirectory);
                    currentActiveDirectory.getDirectories().add(d);

                } else {
                    // We found a file
                    long fileSize = Long.parseLong(tokens[0]);
                    DeviceFile df = new DeviceFile(tokens[1], fileSize);
                    currentActiveDirectory.getFiles().add(df);
                }
            }

            // Populate the sizes of the directories / subdirectories.
            accumulateSize(root);

            long part1 = part1(root);
            System.out.println("Part 1 is: " + part1);

            long part2 = part2(root);
            System.out.println("Part 2 is: " + part2);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // Recursive function that when given a directory, calculates the size of the directory
    // but summing up its internal files and the size of any subdirectories.
    private static long accumulateSize(Directory d) {
        // Base case: reached a directory that has no subdirectories and only files.
        if (d.getDirectories().size() == 0) {
            for (DeviceFile file : d.getFiles()) {
                d.addSize(file.size());
            }
            return d.getSize();
        }

        // Start by accumulating the size of all the directory's files.
        for (DeviceFile file : d.getFiles()) {
            d.addSize(file.size());
        }

        // Then, accumulate the size of any subdirectories, recursively calculating their respective sizes as well.
        for (Directory sub : d.getDirectories()) {
            d.addSize(accumulateSize(sub));
        }

        return d.getSize();
    }

    // Part 1: Traverse the directory tree using DFS and sum up the sizes
    // of the directories with size of AT MOST 100,000.
    private static long part1(Directory root) {
        long sum = 0;
        Stack<Directory> stack = new Stack<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            Directory current = stack.pop();

            stack.addAll(current.getDirectories());

            if (current.getSize() <= 100000) {
                sum += current.getSize();
            }
        }

        return sum;
    }

    // Part 2: Traverse the directory tree using DFS and find the smallest directory
    // whose size is larger than the target deletion size.
    private static long part2(Directory root) {
        long usedSpace = root.getSize();
        long unusedSpace = FILE_SYSTEM_DISK_SPACE - usedSpace;
        long targetDirectoryDeletionSize = SPACE_REQUIRED_TO_UPDATE - unusedSpace;

        long smallestSoFar = Long.MAX_VALUE;
        Stack<Directory> stack = new Stack<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            Directory current = stack.pop();

            stack.addAll(current.getDirectories());

            if (current.getSize() >= targetDirectoryDeletionSize) {
                smallestSoFar = Math.min(smallestSoFar, current.getSize());
            }
        }

        return smallestSoFar;
    }
}