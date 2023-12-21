import java.io.InputStream;
import java.util.*;
import java.util.function.Function;

public class Main {
    public static void main(String[] args) {
        Object completedCourses = ReadFile("completedCourses.txt", Main::GetUniqueCourseList);
        Object degreePrograms = ReadFile("programs.txt", Main::GetDegreeProgramHashMap);
        PrintHashSet((HashSet<String>)completedCourses);
        PrintHashMap((HashMap<String, List<String>>)degreePrograms);
    }

    private static void PrintHashSet(HashSet<String> set) {
        System.out.println(set.toString());
    }

    private static void PrintHashMap(HashMap<String, List<String>> map) {
        for (String programName: map.keySet()) {
            System.out.println(programName + ": " + map.get(programName));
        }
    }

    private static Object ReadFile(String fileName, Function<Scanner, Object> callback) {
        Object result = null;

        try {
            InputStream fileStream = Main.class.getResourceAsStream(fileName);
            assert fileStream != null;
            Scanner fileScanner = new Scanner(fileStream);

            result = callback.apply(fileScanner);

            fileScanner.close();
        }
        catch(Exception ex) {
            System.out.println("Error accessing file: " + ex.getMessage());
        }

        return result;
    }

    private static HashSet<String> GetUniqueCourseList(Scanner fileScanner) {
        HashSet<String> courses = new HashSet<String>();

        while(fileScanner.hasNextLine()) {
            courses.add(fileScanner.nextLine());
        }

        return courses;
    }

    private static HashMap<String, HashSet<String>> GetDegreeProgramHashMap(Scanner fileScanner) {
        HashMap<String, HashSet<String>> degreePrograms = new HashMap<String, HashSet<String>>();
        String currentKey = null;
        HashSet<String> currentCourseList = new HashSet<String>();

        while(fileScanner.hasNextLine()) {
            String line = fileScanner.nextLine();

            if (line.contains("Program")) {
                if (currentKey != null) {
                    degreePrograms.put(currentKey, currentCourseList);
                }

                currentKey = line.substring(8);
                currentCourseList = new HashSet<String>();

                continue;
            }

            currentCourseList.add(line);
        }

        degreePrograms.put(currentKey, currentCourseList);

        return degreePrograms;
    }
}