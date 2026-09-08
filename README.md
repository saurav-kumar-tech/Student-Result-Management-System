🎓 Student Result Management System

A simple and user-friendly desktop application built with **Java Swing, JDBC, and MySQL** to manage student details, subject-wise marks, and academic results.

This project was created as a practical Java application to understand how a desktop GUI can work with a relational database and perform real-world CRUD operations.

---

📌 About the Project

Managing student results manually can become difficult when the number of students and subjects increases.

The **Student Result Management System** provides a simple interface where users can:

- Add student information
- Add subject-wise marks
- View student results
- Search student records
- Delete student records
- Refresh result data
- View result statistics
- Export results to CSV

The application stores all student and marks data in a **MySQL database**.

---

✨ Features

👨‍🎓 Student Management
- Add a new student using Roll Number and Name
- Search student records
- Delete student records
- Refresh the result table

📚 Marks Management
- Add subject-wise marks
- Store marks directly in MySQL
- Prevent duplicate subjects for the same student
- Maintain student-wise result records

📊 Result Management
The application calculates and displays:

- Total Marks
- Average Marks
- Percentage
- Grade
- Pass / Fail Result
- Highest Marks
- Lowest Marks

📁 Export
- Export result data to a CSV file

🖥️ Graphical Interface
- Built using Java Swing
- Simple and easy-to-use interface
- Organized result table
- Center-aligned table data for better readability

---

🛠️ Technologies Used

| Technology | Purpose |
|------------|---------|
| Java | Core programming and application logic |
| Java Swing | Graphical User Interface |
| JDBC | Connecting Java with MySQL |
| MySQL | Database management |
| MySQL Connector/J | JDBC driver |
| VS Code | Development environment |
| MySQL Workbench | Database creation and testing |
| CSV | Exporting result data |

---

📂 Project Structure

```text
Student-Result-Management-System/
│
├── DBConfig.java
├── DBConnection.java
├── Student.java
├── StudentDAO.java
├── StudentManager.java
├── StudentResultGUI.java
├── StudentResultManagementSystem.java
│
├── README.md
├── schema.sql
├── .gitignore
│
└── lib/
    └── mysql-connector-j-26.7.0.jar
