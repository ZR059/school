-- liquibase formatted sql
-- changeset rzubairov:1

CREATE TABLE student (
    id INTEGER PRIMARY KEY,
    name VARCHAR(255),
    age INT NOT NULL,
    faculty_id INT,
FOREIGN KEY (faculty_id) REFERENCES faculty(id)
);

-- changeset rzubairov:2

CREATE INDEX student_name_index ON student (name);