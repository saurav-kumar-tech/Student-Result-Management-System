-- Run this once in MySQL (via phpMyAdmin, MySQL Workbench, or the mysql CLI)
-- before starting the Java application.

CREATE DATABASE IF NOT EXISTS student_result_db;
USE student_result_db;

CREATE TABLE IF NOT EXISTS students (
    roll_no VARCHAR(20)  PRIMARY KEY,
    name    VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS marks (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    roll_no VARCHAR(20) NOT NULL,
    subject VARCHAR(50) NOT NULL,
    marks   INT NOT NULL,
    FOREIGN KEY (roll_no) REFERENCES students(roll_no) ON DELETE CASCADE
);

-- Optional: a couple of sample rows so you can see it working immediately.
-- Comment these out if you'd rather start empty.
INSERT INTO students (roll_no, name) VALUES ('101', 'Amit Sharma');
INSERT INTO marks (roll_no, subject, marks) VALUES
    ('101', 'Mathematics', 88),
    ('101', 'Science', 92),
    ('101', 'English', 76);
