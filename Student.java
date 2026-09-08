import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single student record.
 * Encapsulates data (roll no, name, subject marks) and the logic
 * needed to compute results for that student (OOP: encapsulation).
 */
public class Student {

    private final String rollNo;      // unique identifier used for searching
    private final String name;
    private final List<String> subjects = new ArrayList<>();
    private final List<Integer> marks = new ArrayList<>();
    private static final int MAX_MARKS_PER_SUBJECT = 100;

    public Student(String rollNo, String name) {
        this.rollNo = rollNo;
        this.name = name;
    }

    public void addSubjectMark(String subject, int mark) {
        subjects.add(subject);
        marks.add(mark);
    }

    public String getRollNo() {
        return rollNo;
    }

    public String getName() {
        return name;
    }

    public int getSubjectCount() {
        return subjects.size();
    }

    /** Read-only view of subject names — used by StudentDAO to persist marks. */
    public List<String> getSubjectNames() {
        return java.util.Collections.unmodifiableList(subjects);
    }

    /** Read-only view of marks (same order as getSubjectNames()) — used by StudentDAO. */
    public List<Integer> getSubjectMarksList() {
        return java.util.Collections.unmodifiableList(marks);
    }

    public int getTotalMarks() {
        int total = 0;
        for (int m : marks) total += m;
        return total;
    }

    public double getPercentage() {
        if (subjects.isEmpty()) return 0.0;
        return (getTotalMarks() * 100.0) / (subjects.size() * MAX_MARKS_PER_SUBJECT);
    }

    public double getAverage() {
        if (subjects.isEmpty()) return 0.0;
        return (double) getTotalMarks() / subjects.size();
    }

    /**
     * Grade is derived from percentage using a simple, common Indian
     * academic scale. Easy to tweak in one place.
     */
    public String getGrade() {
        double pct = getPercentage();
        if (pct >= 90) return "A+";
        if (pct >= 80) return "A";
        if (pct >= 70) return "B";
        if (pct >= 60) return "C";
        if (pct >= 50) return "D";
        if (pct >= 40) return "E (Pass)";
        return "F (Fail)";
    }

    public boolean isPass() {
        // Simple rule: fail overall if percentage < 40 OR any single subject < 33 (typical passing mark)
        if (getPercentage() < 40) return false;
        for (int m : marks) {
            if (m < 33) return false;
        }
        return true;
    }

    public String getHighestSubject() {
        return extremeSubject(true);
    }

    public String getLowestSubject() {
        return extremeSubject(false);
    }

    private String extremeSubject(boolean highest) {
        if (subjects.isEmpty()) return "N/A";
        int idx = 0;
        for (int i = 1; i < marks.size(); i++) {
            if (highest ? marks.get(i) > marks.get(idx) : marks.get(i) < marks.get(idx)) {
                idx = i;
            }
        }
        return subjects.get(idx) + " (" + marks.get(idx) + ")";
    }

    /**
     * Full formatted result summary for this student.
     */
    public String getResultSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append(String.format("Roll No : %s%n", rollNo));
        sb.append(String.format("Name    : %s%n", name));
        sb.append("-----------------------------------------\n");
        sb.append(String.format("%-20s %s%n", "Subject", "Marks"));
        for (int i = 0; i < subjects.size(); i++) {
            sb.append(String.format("%-20s %d%n", subjects.get(i), marks.get(i)));
        }
        sb.append("-----------------------------------------\n");
        sb.append(String.format("Total Marks   : %d / %d%n", getTotalMarks(), subjects.size() * MAX_MARKS_PER_SUBJECT));
        sb.append(String.format("Percentage    : %.2f%%%n", getPercentage()));
        sb.append(String.format("Average       : %.2f%n", getAverage()));
        sb.append(String.format("Grade         : %s%n", getGrade()));
        sb.append(String.format("Result        : %s%n", isPass() ? "PASS" : "FAIL"));
        sb.append(String.format("Highest Score : %s%n", getHighestSubject()));
        sb.append(String.format("Lowest Score  : %s%n", getLowestSubject()));
        sb.append("=========================================\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("%-10s %-15s Total:%-4d %%:%-6.2f Grade:%s",
                rollNo, name, getTotalMarks(), getPercentage(), getGrade());
    }
}
