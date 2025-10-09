package ru.hogwarts.school.service;

import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repositories.FacultyRepository;

import java.util.*;

@Service
public class FacultyService {
    private final FacultyRepository facultyRepository;

    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public Faculty createFaculty(Faculty faculty) {
        return facultyRepository.save(faculty);
    }

    public Faculty findFaculty(Long id) {
        // ИСПРАВЛЕНО: Добавляем проверку existsById
        if (id == null || !facultyRepository.existsById(id)) {
            return null;
        }
        return facultyRepository.findById(id).orElse(null);
    }

    public Faculty editFaculty(Faculty faculty) {
        // ИСПРАВЛЕНО: Проверяем существование перед сохранением
        if (faculty.getId() == null || !facultyRepository.existsById(faculty.getId())) {
            return null;
        }
        return facultyRepository.save(faculty);
    }

    public void deleteFaculty(Long id) {
        facultyRepository.deleteById(id);
    }

    public List<Faculty> getFacultiesByColor(String color) {
        return facultyRepository.findByColor(color);
    }

    public Collection<Faculty> getAllFaculties() {
        return facultyRepository.findAll();
    }

    public List<Faculty> findFacultiesByNameOrColor(String nameOrColor) {
        return facultyRepository.findByNameContainingIgnoreCaseOrColorContainingIgnoreCase(nameOrColor, nameOrColor);
    }

    public List<Student> getFacultyStudents(Long facultyId) {
        Optional<Faculty> faculty = facultyRepository.findById(facultyId);
        return faculty.map(Faculty::getStudents).orElse(List.of());
    }
}