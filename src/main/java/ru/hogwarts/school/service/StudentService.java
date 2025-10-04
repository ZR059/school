package ru.hogwarts.school.service;

import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repositories.StudentRepository;

import java.util.List;

@Service
public class StudentService {
    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    public Student findStudent(Long id) {
        return studentRepository.findById(id).get();
    }

    public Student editStudent(Student student) {
        return studentRepository.save(student);
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }

    public List<Student> getStudentsByAge(Integer age) {
        if (age == null) {
            return studentRepository.findAll();
        }
        return studentRepository.findByAge(age);
    }

    public List<Student> getAllStudents(){
        return studentRepository.findAll();
    }

    public List<Student> getStudentsByAgeBetween(int min, int max){
        return studentRepository.findByAgeBetween(min,max);
    }

    public Faculty getStudentFaculty(Long studentId){
        Student student = findStudent(studentId);
        return student !=null ? student.getFaculty() : null;
    }
}
