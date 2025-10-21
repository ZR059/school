SELECT s.name, s.age, f.name as faculty_name
FROM Student s
LEFT JOIN Faculty f ON s.faculty_id = f.id;

SELECT s.name, s.age
FROM Student s
INNER JOIN Avatar a ON s.id = a.student_id;