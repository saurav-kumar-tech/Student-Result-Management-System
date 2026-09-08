public class DBConfig {
    public static final String URL =
            "jdbc:mysql://localhost:3306/student_result_db?useSSL=false&serverTimezone=UTC";

    public static final String USERNAME = System.getenv("DB_USERNAME");
    public static final String PASSWORD = System.getenv("DB_PASSWORD");
}