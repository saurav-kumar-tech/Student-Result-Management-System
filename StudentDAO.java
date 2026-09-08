import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object: the ONLY class that talks SQL / JDBC.
 * Converts between the relational tables (students, marks) and
 * plain Student objects, so the rest of the app never sees SQL.
 *
 * Schema (see schema.sql):
 *   students(roll_no VARCHAR PK, name VARCHAR)
 *   marks(id INT PK AUTO_INCREMENT, roll_no VARCHAR FK, subject VARCHAR, marks INT)
 */
public class StudentDAO {

    /** Insert a student and all their subject marks in one transaction. */
    public void addStudent(Student s) throws SQLException {
        String insertStudent = "INSERT INTO students (roll_no, name) VALUES (?, ?)";
        String insertMark = "INSERT INTO marks (roll_no, subject, marks) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psStudent = conn.prepareStatement(insertStudent);
                 PreparedStatement psMark = conn.prepareStatement(insertMark)) {

                psStudent.setString(1, s.getRollNo());
                psStudent.setString(2, s.getName());
                psStudent.executeUpdate();

                List<String> subjects = s.getSubjectNames();
                List<Integer> marksList = s.getSubjectMarksList();
                for (int i = 0; i < subjects.size(); i++) {
                    psMark.setString(1, s.getRollNo());
                    psMark.setString(2, subjects.get(i));
                    psMark.setInt(3, marksList.get(i));
                    psMark.addBatch();
                }
                psMark.executeBatch();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public boolean isRollNoTaken(String rollNo) throws SQLException {
        String sql = "SELECT 1 FROM students WHERE roll_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Fetch every student with all their marks, in one JOIN query. */
    public List<Student> getAllStudents() throws SQLException {
        String sql = "SELECT s.roll_no, s.name, m.subject, m.marks " +
                "FROM students s LEFT JOIN marks m ON s.roll_no = m.roll_no " +
                "ORDER BY s.roll_no, m.id";
        return runJoinQuery(sql, ps -> {});
    }

    public Student searchByRollNo(String rollNo) throws SQLException {
        String sql = "SELECT s.roll_no, s.name, m.subject, m.marks " +
                "FROM students s LEFT JOIN marks m ON s.roll_no = m.roll_no " +
                "WHERE s.roll_no = ? ORDER BY m.id";
        List<Student> result = runJoinQuery(sql, ps -> ps.setString(1, rollNo));
        return result.isEmpty() ? null : result.get(0);
    }

    public List<Student> searchByName(String namePart) throws SQLException {
        String sql = "SELECT s.roll_no, s.name, m.subject, m.marks " +
                "FROM students s LEFT JOIN marks m ON s.roll_no = m.roll_no " +
                "WHERE LOWER(s.name) LIKE ? ORDER BY s.roll_no, m.id";
        return runJoinQuery(sql, ps -> ps.setString(1, "%" + namePart.toLowerCase() + "%"));
    }

    /** Deletes the student row; marks are removed automatically via ON DELETE CASCADE. */
    public boolean deleteByRollNo(String rollNo) throws SQLException {
        String sql = "DELETE FROM students WHERE roll_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------- shared helper: run a students+marks join query, rebuild Student objects ----------
    private List<Student> runJoinQuery(String sql, SqlParamSetter paramSetter) throws SQLException {
        Map<String, Student> byRollNo = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            paramSetter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String rollNo = rs.getString("roll_no");
                    Student student = byRollNo.computeIfAbsent(rollNo,
                            r -> new Student(r, rsNameSafe(rs)));

                    String subject = rs.getString("subject");
                    int marks = rs.getInt("marks");
                    // LEFT JOIN can produce a null subject if a student has zero marks rows
                    if (subject != null && !rs.wasNull()) {
                        student.addSubjectMark(subject, marks);
                    }
                }
            }
        }
        return new ArrayList<>(byRollNo.values());
    }

    private String rsNameSafe(ResultSet rs) {
        try {
            return rs.getString("name");
        } catch (SQLException e) {
            return "";
        }
    }

    @FunctionalInterface
    private interface SqlParamSetter {
        void set(PreparedStatement ps) throws SQLException;
    }
}
