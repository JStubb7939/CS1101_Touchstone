import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;

public class Main {
    public static void main(String[] args) {
        try {
            HashSet<String> coursesToTransfer =
                    ReadFile("coursesToTransfer.txt", Main::GetUniqueCourseList);
            HashMap<String, HashSet<String>> degreePrograms =
                    ReadFile("programs.txt", Main::GetDegreeProgramCourseList);
            HashMap<String, Integer> degreeProgramTransferCounts =
                    GetDegreeProgramTransferCounts(degreePrograms, coursesToTransfer);

            GenerateCourseTransferReport(degreeProgramTransferCounts, coursesToTransfer);
        }
        catch(IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Returns file contents as transformed by the transformFunction callback.
     * <p>
     * The file must be within the `src` directory.
     *
     * @param  fileName  the name of the file to be read
     * @param  transformFunction a callback function to call on the file contents
     * @return      the file contents as transformed by the transformFunction callback
     */
    private static <T> T ReadFile(String fileName, Function<Scanner, T> transformFunction) {
        System.out.println("Reading file: " + fileName);

        T result = null;

        try {
            InputStream fileStream = Main.class.getResourceAsStream(fileName);
            assert fileStream != null;
            Scanner fileScanner = new Scanner(fileStream);

            result = transformFunction.apply(fileScanner);

            fileScanner.close();
        }
        catch(Exception ex) {
            System.out.println("Error accessing file: " + ex.getMessage());
        }

        return result;
    }

    /**
     * Generates a count of how many courses will transfer to each degree program.
     *
     * @param  degreePrograms  the degree programs with list of transferable courses
     * @param  coursesToTransfer courses to check against the degree programs course list for transferability
     * @return      a hash map of degree programs with the count of how many courses will transfer to each program
     * @see         HashMap
     */
    private static HashMap<String, Integer> GetDegreeProgramTransferCounts(HashMap<String, HashSet<String>> degreePrograms, HashSet<String> coursesToTransfer) {
        System.out.println("Generating a count of how many courses will transfer to each degree program...");

        HashMap<String, Integer> degreeProgramCounts = new HashMap<>();
        Set<String> degreeProgramNames = degreePrograms.keySet();

        // initializing each degree program transfer count to 0
        for (String programName: degreeProgramNames) {
            degreeProgramCounts.put(programName, 0);
        }

        int currentCount = 0;
        HashSet<String> currentCourseList = null;

        // iterating the course transfer list against the course list of each degree program
        for (String course: coursesToTransfer) {
            for (String programName: degreeProgramNames) {
                currentCourseList = degreePrograms.get(programName);

                for (String transferCourse: currentCourseList) {
                    // if the course is on this list, it is transferable to that degree program
                    if (transferCourse.equals(course)) {
                        currentCount = degreeProgramCounts.get(programName);
                        degreeProgramCounts.put(programName, currentCount + 1);
                    }
                }
            }
        }

        return degreeProgramCounts;
    }

    /**
     * Generates a course transfer report and saves that report to the `reports` directory.
     *
     * @param  degreeProgramTransferCounts  the degree programs with count of how many courses will transfer
     * @param  coursesToTransfer courses to check against the degree programs course list for transferability
     */
    private static void GenerateCourseTransferReport(HashMap<String, Integer> degreeProgramTransferCounts, HashSet<String> coursesToTransfer) throws IOException {
        System.out.println("Generating Course Transfer Report...");

        StringBuilder report = new StringBuilder();
        String reportHeader = String.format("Generated Transfer Report\nGenerated On: %s\n\nCourses to Transfer: %s\n\nTransferable Course Count by Degree Program:\n\n",
                LocalDateTime.now(),
                coursesToTransfer);

        report.append(reportHeader);

        List<String> degreePrograms = new ArrayList<>(degreeProgramTransferCounts.keySet());
        Collections.sort(degreePrograms);

        degreePrograms.forEach(program -> {
            report.append(String.format("%s: %s\n", program, degreeProgramTransferCounts.get(program)));
        });

        WriteReportToFile(report.toString());
    }

    /**
     * Writes the generated course transfer report to a file.
     *
     * @param  report  the generated course transfer report
     */
    private static void WriteReportToFile(String report) {
        try {
            String currentPath = System.getProperty("user.dir");
            LocalTime time = LocalTime.now();
            String fileName = String.format("GeneratedTransferReport-%s%s_%s.txt", time.getHour(), time.getMinute(), time.getSecond());
            Path filePath = Paths.get(currentPath, "reports", fileName);
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, report, StandardCharsets.UTF_8);
        }
        catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Generates a unique list of courses to check for transferability.
     *
     * @param  fileScanner  the file scanner for the course list file
     * @return      a hash set of the courses to check for transferability
     * @see         HashSet
     */
    private static HashSet<String> GetUniqueCourseList(Scanner fileScanner) {
        System.out.println("Generating course transfer list...");

        HashSet<String> courses = new HashSet<>();

        while(fileScanner.hasNextLine()) {
            courses.add(fileScanner.nextLine());
        }

        return courses;
    }

    /**
     * Generates a hash map of each degree program with their transferable courses.
     *
     * @param  fileScanner  the file scanner for the degree program file
     * @return      a hash map of the degree programs with transferable courses
     * @see         HashMap
     */
    private static HashMap<String, HashSet<String>> GetDegreeProgramCourseList(Scanner fileScanner) {
        System.out.println("Generating degree program hash map...");

        HashMap<String, HashSet<String>> degreePrograms = new HashMap<>();
        String currentKey = null;
        HashSet<String> currentDegreeProgramCourseList = new HashSet<>();

        while(fileScanner.hasNextLine()) {
            String line = fileScanner.nextLine();

            if (line.contains("Program")) {
                if (currentKey != null) {
                    // if current key is not null, then we have finished the previous section
                    degreePrograms.put(currentKey, currentDegreeProgramCourseList);
                }

                currentKey = line.substring(8);
                currentDegreeProgramCourseList = new HashSet<>();

                continue;
            }

            currentDegreeProgramCourseList.add(line);
        }

        degreePrograms.put(currentKey, currentDegreeProgramCourseList);

        return degreePrograms;
    }
}