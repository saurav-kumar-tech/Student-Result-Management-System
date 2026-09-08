import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centralizes opening a JDBC connection to MySQL.
 * Every DAO call goes through getConnection() (try-with-resources),
 * so connections are always opened fresh and closed properly —
 * no leaked/shared connections across the app.
 */
public class DBConnection {

    static {
        try {
            // Explicitly loading the driver class; with modern JDBC (4.0+)
            // this is technically optional if the driver JAR is on the
            // classpath, but keeping it makes setup errors much clearer.
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC driver not found on classpath.");
            System.out.println("Download 'mysql-connector-j-x.x.x.jar' and add it with -cp when compiling/running.");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DBConfig.URL, DBConfig.USERNAME, DBConfig.PASSWORD);
    }

    /**
     * Quick connectivity check used at application startup so the user
     * gets one clear error message instead of a stack trace on first click.
     */
    public static boolean testConnection() {
        try (Connection ignored = getConnection()) {
            return true;
        } catch (SQLException e) {
            System.out.println("=========================================================");
            System.out.println(" Could NOT connect to MySQL database.");
            System.out.println(" Details: " + e.getMessage());
            System.out.println();
            System.out.println(" Checklist:");
            System.out.println("  1. Is MySQL server running (e.g. XAMPP Control Panel)?");
            System.out.println("  2. Did you run schema.sql to create 'student_result_db'?");
            System.out.println("  3. Are DBConfig.USERNAME / PASSWORD correct?");
            System.out.println("  4. Is the mysql-connector-j JAR on your classpath?");
            System.out.println("=========================================================");
            return false;
        }
    }
}
