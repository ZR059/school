ALTER TABLE Student ADD CONSTRAINT chk_student_age CHECK (age >= 16);

ALTER TABLE Student
ALTER COLUMN name SET NOT NULL;

ALTER TABLE Student
ADD CONSTRAINT uk_student_name UNIQUE (name);

ALTER TABLE Faculty
ADD CONSTRAINT uk_faculty_name_color UNIQUE (name, color);

ALTER TABLE Student
ALTER COLUMN age SET DEFAULT (20);