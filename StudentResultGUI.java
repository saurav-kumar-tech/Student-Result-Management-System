import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Swing GUI front-end for the same Student / StudentManager backend
 * used by the console version. Demonstrates the same OOP model with
 * a graphical interface instead of a text menu.
 */
public class StudentResultGUI extends JFrame {

    private final StudentManager manager = new StudentManager();

    // Form fields
    private final JTextField rollField = new JTextField(10);
    private final JTextField nameField = new JTextField(15);
    private final JTextField subjectField = new JTextField(10);
    private final JTextField marksField = new JTextField(5);

    // Temporary holder for subjects being entered for the "current" new student
    private Student pendingStudent;

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"Roll No", "Name", "Total", "Percentage", "Grade", "Result"}, 0);
    private final JTable table = new JTable(tableModel);

// Center align table headers and data
{
    DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
    headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

    for (int i = 0; i < table.getColumnCount(); i++) {
        table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
    }

    DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
    cellRenderer.setHorizontalAlignment(SwingConstants.CENTER);

    for (int i = 0; i < table.getColumnCount(); i++) {
        table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
    }
}

    private final JTextArea summaryArea = new JTextArea(12, 40);
    private final JTextField searchField = new JTextField(10);
    private final JLabel statsLabel = new JLabel(" ");

    public StudentResultGUI() {
        super("Student Result Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        add(buildTopFormPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        setSize(950, 620);
        setLocationRelativeTo(null);
    }

    // ---------------- TOP: Add student + subject entry form ----------------
    private JPanel buildTopFormPanel() {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBorder(BorderFactory.createTitledBorder("Add Student"));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row1.add(new JLabel("Roll No:"));
        row1.add(rollField);
        row1.add(new JLabel("Name:"));
        row1.add(nameField);
        JButton startBtn = new JButton("Start / New Student");
        startBtn.addActionListener(this::onStartStudent);
        row1.add(startBtn);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2.add(new JLabel("Subject:"));
        row2.add(subjectField);
        row2.add(new JLabel("Marks (0-100):"));
        row2.add(marksField);
        JButton addSubjectBtn = new JButton("Add Subject");
        addSubjectBtn.addActionListener(this::onAddSubject);
        row2.add(addSubjectBtn);
        JButton saveBtn = new JButton("Save Student");
        saveBtn.addActionListener(this::onSaveStudent);
        row2.add(saveBtn);

        outer.add(row1);
        outer.add(row2);
        return outer;
    }

    // ---------------- CENTER: table + summary ----------------
    private JPanel buildCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(10, 10));

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("All Students (ranked by %)"));
        tableScroll.setPreferredSize(new Dimension(500, 300));

        summaryArea.setEditable(false);
        summaryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane summaryScroll = new JScrollPane(summaryArea);
        summaryScroll.setBorder(BorderFactory.createTitledBorder("Result Summary"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, summaryScroll);
        split.setResizeWeight(0.5);

        center.add(split, BorderLayout.CENTER);
        return center;
    }

    // ---------------- BOTTOM: search / delete / export / stats ----------------
    private JPanel buildBottomPanel() {
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionRow.add(new JLabel("Roll No / Name:"));
        actionRow.add(searchField);

        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(this::onSearch);
        actionRow.add(searchBtn);

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(this::onDelete);
        actionRow.add(deleteBtn);

        JButton refreshBtn = new JButton("Refresh Table");
        refreshBtn.addActionListener(e -> refreshTable());
        actionRow.add(refreshBtn);

        JButton statsBtn = new JButton("Class Statistics");
        statsBtn.addActionListener(this::onStats);
        actionRow.add(statsBtn);

        JButton exportBtn = new JButton("Export CSV");
        exportBtn.addActionListener(this::onExport);
        actionRow.add(exportBtn);

        bottom.add(actionRow);
        statsLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 8, 10));
        bottom.add(statsLabel);
        return bottom;
    }

    // ---------------- Event handlers ----------------
    private void onStartStudent(ActionEvent e) {
        String roll = rollField.getText().trim();
        String name = nameField.getText().trim();
        if (roll.isEmpty() || name.isEmpty()) {
            showError("Enter both Roll No and Name first.");
            return;
        }
        if (manager.isRollNoTaken(roll)) {
            showError("Roll No already exists.");
            return;
        }
        pendingStudent = new Student(roll, name);
        JOptionPane.showMessageDialog(this,
                "Now add subjects one by one using 'Add Subject', then click 'Save Student'.");
    }

    private void onAddSubject(ActionEvent e) {
        if (pendingStudent == null) {
            showError("Click 'Start / New Student' first.");
            return;
        }
        String subject = subjectField.getText().trim();
        String marksText = marksField.getText().trim();
        if (subject.isEmpty()) {
            showError("Enter a subject name.");
            return;
        }
        int marks;
        try {
            marks = Integer.parseInt(marksText);
        } catch (NumberFormatException ex) {
            showError("Marks must be a whole number.");
            return;
        }
        if (marks < 0 || marks > 100) {
            showError("Marks must be between 0 and 100.");
            return;
        }
        pendingStudent.addSubjectMark(subject, marks);
        subjectField.setText("");
        marksField.setText("");
        summaryArea.setText("Subjects added so far: " + pendingStudent.getSubjectCount()
                + "\nClick 'Save Student' when done.");
    }

    private void onSaveStudent(ActionEvent e) {
        if (pendingStudent == null || pendingStudent.getSubjectCount() == 0) {
            showError("Add at least one subject before saving.");
            return;
        }
        manager.addStudent(pendingStudent);
        summaryArea.setText(pendingStudent.getResultSummary());
        refreshTable();
        pendingStudent = null;
        rollField.setText("");
        nameField.setText("");
    }

    private void onSearch(ActionEvent e) {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            showError("Enter a Roll No or Name to search.");
            return;
        }
        Optional<Student> byRoll = manager.searchByRollNo(query);
        if (byRoll.isPresent()) {
            summaryArea.setText(byRoll.get().getResultSummary());
            return;
        }
        List<Student> byName = manager.searchByName(query);
        if (!byName.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            byName.forEach(s -> sb.append(s.getResultSummary()).append("\n"));
            summaryArea.setText(sb.toString());
        } else {
            summaryArea.setText("No student found for: " + query);
        }
    }

    private void onDelete(ActionEvent e) {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            showError("Enter a Roll No to delete.");
            return;
        }
        if (manager.deleteByRollNo(query)) {
            summaryArea.setText("Deleted student with Roll No: " + query);
            refreshTable();
        } else {
            showError("No student found with Roll No: " + query);
        }
    }

    private void onStats(ActionEvent e) {
        if (manager.getStudentCount() == 0) {
            statsLabel.setText("No records yet.");
            return;
        }
        String topper = manager.getTopper()
                .map(t -> String.format("%s (%s) - %.2f%%", t.getName(), t.getRollNo(), t.getPercentage()))
                .orElse("N/A");
        statsLabel.setText(String.format(
                "Students: %d | Class Avg: %.2f%% | Passed: %d | Failed: %d | Topper: %s",
                manager.getStudentCount(), manager.getClassAveragePercentage(),
                manager.countPassed(), manager.countFailed(), topper));
    }

    private void onExport(ActionEvent e) {
        if (manager.getStudentCount() == 0) {
            showError("No records to export.");
            return;
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter("student_results.csv"))) {
            pw.println("RollNo,Name,TotalMarks,Percentage,Average,Grade,Result");
            for (Student s : manager.getAllStudents()) {
                pw.printf("%s,%s,%d,%.2f,%.2f,%s,%s%n",
                        s.getRollNo(), s.getName(), s.getTotalMarks(), s.getPercentage(),
                        s.getAverage(), s.getGrade(), s.isPass() ? "PASS" : "FAIL");
            }
            JOptionPane.showMessageDialog(this, "Exported to student_results.csv");
        } catch (IOException ex) {
            showError("Export failed: " + ex.getMessage());
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Student s : manager.getRanking()) {
            tableModel.addRow(new Object[]{
                    s.getRollNo(), s.getName(), s.getTotalMarks(),
                    String.format("%.2f%%", s.getPercentage()), s.getGrade(),
                    s.isPass() ? "PASS" : "FAIL"
            });
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        if (!DBConnection.testConnection()) {
            JOptionPane.showMessageDialog(null,
                    "Could not connect to MySQL database.\n" +
                            "Check console output for details, verify MySQL is running,\n" +
                            "schema.sql has been run, and DBConfig.java credentials are correct.",
                    "Database Connection Failed", JOptionPane.ERROR_MESSAGE);
            System.out.println("Startup aborted: fix the database connection and re-run.");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            StudentResultGUI gui = new StudentResultGUI();
            gui.refreshTable();
            gui.setVisible(true);
        });
    }
}
