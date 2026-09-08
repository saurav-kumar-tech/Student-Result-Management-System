# Student Result Management System (Java + MySQL)

Data ab **MySQL database** me store hota hai (pehle sirf memory me tha,
program band karte hi delete ho jaata tha — ab permanent rehta hai).

## Files
- `schema.sql` — MySQL database + table structure (run this ONCE first).
- `src/Student.java` — one student's data + calculations (total, %, average, grade, highest/lowest subject). Pure OOP, koi SQL nahi.
- `src/DBConfig.java` — **connection settings yahin edit karo** (URL / username / password).
- `src/DBConnection.java` — JDBC connection helper + startup connectivity check.
- `src/StudentDAO.java` — saara SQL/JDBC code (INSERT/SELECT/DELETE) yahin hai.
- `src/StudentManager.java` — pehle jaisa hi API, ab andar se database use karta hai.
- `src/StudentResultManagementSystem.java` — console (menu-driven) version.
- `src/StudentResultGUI.java` — Swing GUI version.

---

## Setup (ek baar karna hai)

### 1. MySQL install karo
XAMPP sabse aasaan hai (Windows): https://www.apachefriends.org/ — install karke
Control Panel se **MySQL "Start"** karo.
(Agar standalone MySQL install kiya hai to bas MySQL service running honi chahiye.)

### 2. Database banao
`schema.sql` ko phpMyAdmin (XAMPP ke saath aata hai, `http://localhost/phpmyadmin`) me
import karo — ya command line se:
```
mysql -u root -p < schema.sql
```
Isse `student_result_db` database aur `students` / `marks` tables ban jayenge,
saath me ek sample student bhi (Roll No 101) taaki turant test kar sako.

### 3. `DBConfig.java` check karo
```java
public static final String URL = "jdbc:mysql://localhost:3306/student_result_db?useSSL=false&serverTimezone=UTC";
public static final String USERNAME = "root";
public static final String PASSWORD = "";   // XAMPP me default empty hoti hai
```
Agar tumne apna password set kiya hai to yahan daal do.

### 4. MySQL JDBC driver (Connector/J) download karo
Ye ek `.jar` file hai jo Java ko MySQL se baat karna sikhati hai. Yahan se download karo:
**https://dev.mysql.com/downloads/connector/j/** → "Platform Independent" ZIP/TAR chuno →
usme se `mysql-connector-j-x.x.x.jar` file nikaal ke apne project folder me daal do
(e.g. `StudentResultManagementSystem/lib/mysql-connector-j.jar`).

---

## Compile & Run

Command Prompt / Terminal me `src` folder ke andar jaake:

### Console version
```
javac -cp .;../lib/mysql-connector-j.jar *.java
java  -cp .;../lib/mysql-connector-j.jar StudentResultManagementSystem
```
(Mac/Linux par `;` ki jagah `:` use karo — `-cp .:../lib/mysql-connector-j.jar`)

### GUI version
```
javac -cp .;../lib/mysql-connector-j.jar *.java
java  -cp .;../lib/mysql-connector-j.jar StudentResultGUI
```

Startup pe app automatically MySQL se connect hone ki koshish karta hai — agar
connection fail hui (server band hai / password galat / jar missing) to
ek clear error message dikhega batate hue kya check karna hai.

---

## VS Code me chalane ke liye
`.vscode/settings.json` me classpath add kar sakte ho, ya simpler: VS Code ke
"Java Projects" panel me right-click karke `lib/mysql-connector-j.jar` ko
"Add to Referenced Libraries" kar do — phir normal ▶️ Run button se chalega.

---

## Menu options (console) / Buttons (GUI) — same as before
1. Add student (roll no, name, subjects + marks 0-100)
2. Search (by Roll No or partial Name)
3. Display all students (ranked by percentage)
4. Full result summary for one student
5. Class statistics (topper, class average, pass/fail count)
6. Delete a student record
7. Export all results to `student_results.csv` (extra backup, DB is now the real storage)

## Database schema
```
students(roll_no VARCHAR PK, name VARCHAR)
marks(id INT PK AUTO_INCREMENT, roll_no VARCHAR FK -> students, subject VARCHAR, marks INT)
```
Deleting a student automatically deletes their marks too (`ON DELETE CASCADE`).

## Note
Is sandbox me maine ye poora setup ek asli MariaDB (MySQL-compatible) server pe
test kiya — schema apply hua, sample data insert hua, aur saara Java code
(Student, DBConfig, DBConnection, StudentDAO, StudentManager, console app, GUI)
bina kisi error ke compile hua. Sirf `mysql-connector-j.jar` download karna baaki
hai kyunki wo binary file hai jo is sandbox ke restricted network se download nahi
ho payi — upar diya gaya official link use karo.
