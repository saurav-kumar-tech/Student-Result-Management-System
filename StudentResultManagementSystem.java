import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Entry point: console menu that drives the StudentManager.
 *
 * Covers the required features:
 *  1. Add student details
 *  2. Enter marks for multiple subjects
 *  3. Calculate total marks
 *  4. Calculate percentage / average
 *  5. Determine grade
 *  6. Highest / lowest subject marks
 *  7. Full result summary
 *  8. Search student by identifier (roll no / name)
 *  9. Store multiple records (ArrayList via StudentManager)
 *
 * Extra ("advance") features added on top of the basic requirement:
 *  - Class-wide ranking / topper / class average / pass-fail count
 *  - Delete a record
 *  - Export all results to a CSV file (simple file handling / data persistence)
 *  - Input validation (marks range 0-100, no duplicate roll numbers)
 */
public class StudentResultManagementSystem {

    private static final Scanner sc = new Scanner(System.in);
    private static final StudentManager manager = new StudentManager();

    public static void main(String[] args) {
        boolean running = true;
        System.out.println("=====================================================");
        System.out.println("   STUDENT RESULT MANAGEMENT SYSTEM (MySQL backend)");
        System.out.println("=====================================================");

        if (!DBConnection.testConnection()) {
            System.out.println("Startup aborted: fix the database connection and re-run.");
            return;
        }
        System.out.println("Connected to MySQL successfully.\n");

        while (running) {
            printMenu();
            int choice = readInt("Enter your choice: ");
            switch (choice) {
                case 1 -> addStudentFlow();
                case 2 -> searchStudentFlow();
                case 3 -> displayAllStudents();
                case 4 -> displayResultSummary();
                case 5 -> displayClassStatistics();
                case 6 -> deleteStudentFlow();
                case 7 -> exportToCsv();
                case 0 -> {
                    running = false;
                    System.out.println("Exiting... Thank you!");
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
        sc.close();
    }

    private static void printMenu() {
        System.out.println("\n----------------- MAIN MENU -----------------");
        System.out.println("1. Add new student (with subject marks)");
        System.out.println("2. Search student (by Roll No / Name)");
        System.out.println("3. Display all students (summary table + ranking)");
        System.out.println("4. Display full result summary of a student");
        System.out.println("5. Class statistics (topper, average, pass/fail)");
        System.out.println("6. Delete a student record");
        System.out.println("7. Export all results to CSV file");
        System.out.println("0. Exit");
        System.out.println("----------------------------------------------");
    }

    // 1. ADD STUDENT ---------------------------------------------------
    private static void addStudentFlow() {
        System.out.println("\n-- Add New Student --");
        String rollNo;
        while (true) {
            rollNo = readLine("Enter Roll No: ").trim();
            if (rollNo.isEmpty()) {
                System.out.println("Roll No cannot be empty.");
            } else if (manager.isRollNoTaken(rollNo)) {
                System.out.println("A student with this Roll No already exists. Try another.");
            } else {
                break;
            }
        }

        String name = readLine("Enter Student Name: ").trim();
        Student student = new Student(rollNo, name);

        int subjectCount = readInt("How many subjects? ");
        while (subjectCount <= 0) {
            System.out.println("Number of subjects must be at least 1.");
            subjectCount = readInt("How many subjects? ");
        }

        for (int i = 1; i <= subjectCount; i++) {
            String subject = readLine("  Subject " + i + " name: ").trim();
            int mark = readIntInRange("  Marks in " + subject + " (0-100): ", 0, 100);
            student.addSubjectMark(subject.isEmpty() ? ("Subject" + i) : subject, mark);
        }

        manager.addStudent(student);
        System.out.println("\nStudent added successfully!");
        System.out.println(student.getResultSummary());
    }

    // 8. SEARCH ---------------------------------------------------------
    private static void searchStudentFlow() {
        System.out.println("\n-- Search Student --");
        if (manager.getStudentCount() == 0) {
            System.out.println("No records yet.");
            return;
        }
        System.out.println("Search by: 1) Roll No   2) Name");
        int mode = readInt("Choice: ");
        if (mode == 1) {
            String rollNo = readLine("Enter Roll No: ").trim();
            Optional<Student> found = manager.searchByRollNo(rollNo);
            if (found.isPresent()) {
                System.out.println(found.get().getResultSummary());
            } else {
                System.out.println("No student found with Roll No: " + rollNo);
            }
        } else if (mode == 2) {
            String name = readLine("Enter Name (or part of it): ").trim();
            List<Student> results = manager.searchByName(name);
            if (results.isEmpty()) {
                System.out.println("No student found matching: " + name);
            } else {
                results.forEach(s -> System.out.println(s.getResultSummary()));
            }
        } else {
            System.out.println("Invalid option.");
        }
    }

    // 3. DISPLAY ALL ------------------------------------------------------
    private static void displayAllStudents() {
        System.out.println("\n-- All Students (ranked by percentage) --");
        if (manager.getStudentCount() == 0) {
            System.out.println("No records yet.");
            return;
        }
        System.out.printf("%-10s %-15s %-10s %-10s %-6s%n", "RollNo", "Name", "Total", "Percent", "Grade");
        System.out.println("--------------------------------------------------------");
        int rank = 1;
        for (Student s : manager.getRanking()) {
            System.out.printf("#%-2d %-8s %-15s %-10d %-9.2f%% %-6s%n",
                    rank++, s.getRollNo(), s.getName(), s.getTotalMarks(), s.getPercentage(), s.getGrade());
        }
    }

    // 7. FULL RESULT SUMMARY ---------------------------------------------
    private static void displayResultSummary() {
        if (manager.getStudentCount() == 0) {
            System.out.println("No records yet.");
            return;
        }
        String rollNo = readLine("Enter Roll No to view full summary: ").trim();
        Optional<Student> found = manager.searchByRollNo(rollNo);
        if (found.isPresent()) {
            System.out.println(found.get().getResultSummary());
        } else {
            System.out.println("No student found with Roll No: " + rollNo);
        }
    }

    // 5. CLASS STATISTICS (advance feature) -------------------------------
    private static void displayClassStatistics() {
        if (manager.getStudentCount() == 0) {
            System.out.println("No records yet.");
            return;
        }
        System.out.println("\n-- Class Statistics --");
        System.out.println("Total students   : " + manager.getStudentCount());
        System.out.printf("Class average %%  : %.2f%n", manager.getClassAveragePercentage());
        System.out.println("Passed           : " + manager.countPassed());
        System.out.println("Failed           : " + manager.countFailed());
        manager.getTopper().ifPresent(t ->
                System.out.printf("Topper           : %s (%s) - %.2f%%%n", t.getName(), t.getRollNo(), t.getPercentage()));
    }

    // 6. DELETE (advance feature) -----------------------------------------
    private static void deleteStudentFlow() {
        if (manager.getStudentCount() == 0) {
            System.out.println("No records yet.");
            return;
        }
        String rollNo = readLine("Enter Roll No to delete: ").trim();
        if (manager.deleteByRollNo(rollNo)) {
            System.out.println("Record deleted successfully.");
        } else {
            System.out.println("No student found with Roll No: " + rollNo);
        }
    }

    // 7. EXPORT TO CSV (advance feature) -----------------------------------
    private static void exportToCsv() {
        if (manager.getStudentCount() == 0) {
            System.out.println("No records yet.");
            return;
        }
        String fileName = "student_results.csv";
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
            pw.println("RollNo,Name,TotalMarks,Percentage,Average,Grade,Result");
            for (Student s : manager.getAllStudents()) {
                pw.printf("%s,%s,%d,%.2f,%.2f,%s,%s%n",
                        s.getRollNo(), s.getName(), s.getTotalMarks(), s.getPercentage(),
                        s.getAverage(), s.getGrade(), s.isPass() ? "PASS" : "FAIL");
            }
            System.out.println("Exported to " + fileName + " (in current working directory).");
        } catch (IOException e) {
            System.out.println("Failed to export: " + e.getMessage());
        }
    }

    // ---------- Input helper methods (with validation) ----------
    private static String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int val = readInt(prompt);
            if (val < min || val > max) {
                System.out.println("Value must be between " + min + " and " + max + ".");
            } else {
                return val;
            }
        }
    }
}
