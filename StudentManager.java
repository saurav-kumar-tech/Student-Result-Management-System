import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Same public API as the original ArrayList-based StudentManager, but now
 * every operation is backed by MySQL (through StudentDAO). This means the
 * console app and the Swing GUI did NOT need to change - they only ever
 * called these methods, not the storage details.
 *
 * SQLExceptions are caught here and turned into console messages / empty
 * results, so callers don't need try/catch everywhere.
 */
public class StudentManager {

    private final StudentDAO dao = new StudentDAO();

    public void addStudent(Student s) {
        try {
            dao.addStudent(s);
        } catch (SQLException e) {
            System.out.println("Database error while adding student: " + e.getMessage());
        }
    }

    public boolean isRollNoTaken(String rollNo) {
        try {
            return dao.isRollNoTaken(rollNo);
        } catch (SQLException e) {
            System.out.println("Database error while checking Roll No: " + e.getMessage());
            return false;
        }
    }

    public List<Student> getAllStudents() {
        try {
            return dao.getAllStudents();
        } catch (SQLException e) {
            System.out.println("Database error while fetching students: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public int getStudentCount() {
        return getAllStudents().size();
    }

    public Optional<Student> searchByRollNo(String rollNo) {
        try {
            return Optional.ofNullable(dao.searchByRollNo(rollNo));
        } catch (SQLException e) {
            System.out.println("Database error while searching: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<Student> searchByName(String namePart) {
        try {
            return dao.searchByName(namePart);
        } catch (SQLException e) {
            System.out.println("Database error while searching: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean deleteByRollNo(String rollNo) {
        try {
            return dao.deleteByRollNo(rollNo);
        } catch (SQLException e) {
            System.out.println("Database error while deleting: " + e.getMessage());
            return false;
        }
    }

    /** Rank list: highest percentage first. */
    public List<Student> getRanking() {
        List<Student> all = getAllStudents();
        all.sort(Comparator.comparingDouble(Student::getPercentage).reversed());
        return all;
    }

    public Optional<Student> getTopper() {
        return getAllStudents().stream().max(Comparator.comparingDouble(Student::getPercentage));
    }

    public double getClassAveragePercentage() {
        List<Student> all = getAllStudents();
        if (all.isEmpty()) return 0.0;
        double sum = 0;
        for (Student s : all) sum += s.getPercentage();
        return sum / all.size();
    }

    public long countPassed() {
        return getAllStudents().stream().filter(Student::isPass).count();
    }

    public long countFailed() {
        return getStudentCount() - countPassed();
    }
}
