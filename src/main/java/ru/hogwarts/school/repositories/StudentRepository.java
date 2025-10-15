package ru.hogwarts.school.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.hogwarts.school.model.Student;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByAge(int age);

    List<Student> findByAgeBetween(int min, int max);

    List<Student> findAllByAge(int age);

    @Query("SELECT COUNT(s) FROM Student s")
    Integer getTotalCountOfStudents();

    @Query("SELECT AVG(s.age) FROM Student s")
    Double getAverageAgeOfStudents();

    @Query("SELECT s FROM Student s ORDER BY s.id DESC LIMIT 5")
    List<Student> findLastFiveStudents();


}
