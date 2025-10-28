package ru.hogwarts.school.service;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repositories.AvatarRepository;
import ru.hogwarts.school.repositories.StudentRepository;

@Service
public class StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);


    @Value("${avatars.dir.path}")
    private String avatarsDir;

    private final StudentRepository studentRepository;
    private final AvatarRepository avatarRepository;

    public StudentService(StudentRepository studentRepository, AvatarRepository avatarRepository) {
        this.studentRepository = studentRepository;
        this.avatarRepository = avatarRepository;
        logger.info("StudentService initialized with avatars directory: {}", avatarsDir);
    }

    public Student addStudent(Student student) {
        logger.info("Was invoked method for create student");
        logger.debug("Creating student with data: name={}, age={}", student.getName(), student.getAge());
        student.setId(null);
        Student savedStudent = studentRepository.save(student);

        logger.debug("Successfully created student with id: {}", savedStudent.getId());
        return savedStudent;
    }

    public Student findStudent(long id) {
        logger.info("Was invoked method for get student by id: {}", id);
        logger.debug("Searching for student with id: {}", id);

        Student student = studentRepository.findById(id).orElse(null);

        if (student == null) {
            logger.error("Student with id = {} was not found", id);
            logger.warn("Attempt to access non-existent student with id: {}", id);
        } else {
            logger.debug("Found student: id={}, name={}, age={}", student.getId(), student.getName(), student.getAge());
        }

        return student;
    }

    public Student editStudent(Student student) {
        logger.info("Was invoked method for edit student with id: {}", student.getId());
        logger.debug("Editing student with new data: name={}, age={}", student.getName(), student.getAge());

        if (!studentRepository.existsById(student.getId())) {
            logger.error("Cannot edit. Student with id = {} was not found", student.getId());
            logger.warn("Attempt to edit non-existent student with id: {}", student.getId());
            return null;
        }

        Student updatedStudent = studentRepository.save(student);
        logger.info("Student with id {} was successfully updated", student.getId());
        logger.debug("Updated student data: name={}, age={}", updatedStudent.getName(), updatedStudent.getAge());

        return updatedStudent;
    }

    public void deleteStudent(long id) {
        logger.info("Was invoked method for delete student with id: {}", id);

        if (!studentRepository.existsById(id)) {
            logger.error("Cannot delete. Student with id = {} was not found", id);
            logger.warn("Attempt to delete non-existent student with id: {}", id);
            return;
        }

        studentRepository.deleteById(id);
        logger.info("Student with id {} was successfully deleted", id);
        logger.debug("Student deletion completed for id: {}", id);
    }


    public Collection<Student> findByAge(int age) {
        logger.info("Was invoked method for find students by age: {}", age);
        logger.debug("Searching for students with age: {}", age);

        Collection<Student> students = studentRepository.findAllByAge(age);

        logger.debug("Found {} students with age {}", students.size(), age);
        if (students.isEmpty()) {
            logger.warn("No students found with age: {}", age);
        }

        return students;
    }

    public Avatar findAvatar(long studentId) {
        logger.info("Was invoked method for find avatar by student id: {}", studentId);
        logger.debug("Searching for avatar of student with id: {}", studentId);

        Avatar avatar = avatarRepository.findByStudentId(studentId).orElse(null);

        if (avatar == null) {
            logger.warn("Avatar for student with id = {} was not found", studentId);
            logger.debug("No avatar record found in database for student id: {}", studentId);
        } else {
            logger.debug("Found avatar: id={}, filePath={}, fileSize={} bytes",
                    avatar.getId(), avatar.getFilePath(), avatar.getFileSize());
        }

        return avatar;
    }

    public void uploadAvatar(Long studentId, MultipartFile file) throws IOException {
        logger.info("Was invoked method for upload avatar for student id: {}", studentId);
        logger.debug("Uploading avatar file: originalFilename={}, size={} bytes, contentType={}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());
        Student student = findStudent(studentId);

        Path filePath = Path.of(avatarsDir, studentId + "." + getExtension(file.getOriginalFilename()));
        Files.createDirectories(filePath.getParent());
        Files.deleteIfExists(filePath);

        try (InputStream is = file.getInputStream();
             OutputStream os = Files.newOutputStream(filePath, CREATE_NEW);
             BufferedInputStream bis = new BufferedInputStream(is, 1024);
             BufferedOutputStream bos = new BufferedOutputStream(os, 1024)
        ) {
            bis.transferTo(bos);
            logger.debug("Avatar file successfully written to disk: {}", filePath);
        } catch (IOException e) {
            logger.error("Error while saving avatar file for student id {}: {}", studentId, e.getMessage());
            throw e;
        }

        Avatar avatar = avatarRepository.findByStudentId(studentId).orElseGet(Avatar::new);
        avatar.setStudent(student);
        avatar.setFilePath(filePath.toString());
        avatar.setFileSize(file.getSize());
        avatar.setMediaType(file.getContentType());
        avatar.setData(file.getBytes());

        Avatar savedAvatar = avatarRepository.save(avatar);
        logger.info("Avatar successfully uploaded for student id: {}", studentId);
        logger.debug("Avatar saved with id: {}, filePath: {}", savedAvatar.getId(), savedAvatar.getFilePath());
    }

    private String getExtension(String fileName) {
        logger.debug("Extracting extension from filename: {}", fileName);
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            logger.warn("Filename '{}' has no extension or is null", fileName);
            return "bin";
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        logger.debug("Extracted extension: {}", extension);
        return extension;
    }

    public Integer getTotalCountOfStudents() {
        logger.info("Was invoked method for get total count of students");

        Integer count = studentRepository.getTotalCountOfStudents();
        logger.debug("Total student count: {}", count);

        if (count == 0) {
            logger.warn("No students found in database");
        }

        return count;
    }

    public Double getAverageAgeOfStudents() {
        logger.info("Was invoked method for get average age of students");

        Double averageAge = studentRepository.getAverageAgeOfStudents();
        logger.debug("Calculated average age: {}", averageAge);

        if (averageAge == null) {
            logger.warn("Cannot calculate average age - no students in database");
        }

        return averageAge;
    }

    public List<Student> getLastFiveStudents() {
        logger.info("Was invoked method for get last five students");

        List<Student> students = studentRepository.findLastFiveStudents();
        logger.debug("Retrieved {} last students", students.size());

        if (students.isEmpty()) {
            logger.warn("No students found when retrieving last five");
        } else if (students.size() < 5) {
            logger.debug("Retrieved only {} students (less than 5)", students.size());
        }

        return students;
    }

    public List<String> getStudentNamesStartingWithASorted() {
        logger.info("Was invoked method for get student names starting with A");

        List<String> result = studentRepository.findAll().stream()
                .map(Student::getName)
                .map(String::toUpperCase)
                .filter(upperCase -> upperCase.startsWith("A"))
                .sorted()
                .toList();

        logger.debug("Found {} students with names starting with A", result.size());
        return result;
    }

    public Double getAverageAgeOfAllStudents() {
        logger.info("Was invoked method for get average age of all students");

        Double averageAge = studentRepository.findAll().stream()
                .mapToInt(Student::getAge)
                .average()
                .orElse(0.0);

        logger.debug("Calculated average age: {}", averageAge);
        return averageAge;
    }

    public Integer calculateSumOptimized() {
        logger.info("Was invoked method for calculate optimized sum");

        long startTime = System.currentTimeMillis();

        int sum = Stream.iterate(1, a -> a + 1)
                .limit(1_000_000)
                .parallel()
                .reduce(0, Integer::sum);

        long endTime = System.currentTimeMillis();
        logger.debug("Optimized sum calculation took {} ms, result: {}", (endTime - startTime), sum);

        return sum;
    }

    public Integer calculateSumSlow() {
        logger.info("Was invoked method for calculate slow sum");

        long startTime = System.currentTimeMillis();

        int sum = Stream.iterate(1, a -> a + 1)
                .limit(1_000_000)
                .reduce(0, (a, b) -> a + b);

        long endTime = System.currentTimeMillis();
        logger.debug("Slow sum calculation took {} ms, result: {}", (endTime - startTime), sum);

        return sum;
    }

    public void printStudentsParallel() {
        logger.info("Was invoked method for print students in parallel mode");

        List<Student> students = studentRepository.findAll();

        if (students.size() < 6) {
            logger.warn("Not enough students for parallel printing. Required: 6, available: {}", students.size());
            return;
        }

        System.out.println("Main Thread: " + students.get(0).getName());
        System.out.println("Main Thread: " + students.get(1).getName());

        Thread thread1 = new Thread(() -> {
            System.out.println("Thread-1: " + students.get(2).getName());
            System.out.println("Thread-1: " + students.get(3).getName());
        });

        Thread thread2 = new Thread(() -> {
            System.out.println("Thread-2: " + students.get(4).getName());
            System.out.println("Thread-2: " + students.get(5).getName());
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            logger.error("Thread was interrupted", e);
            Thread.currentThread().interrupt();
        }

        logger.debug("Parallel printing completed");
    }

    private final Object lock = new Object();

    public void printStudentsSynchronized() {
        logger.info("Was invoked method for print students in synchronized mode");

        List<Student> students = studentRepository.findAll();

        if (students.size() < 6) {
            logger.warn("Not enough students for synchronized printing. Required: 6, available: {}", students.size());
            return;
        }

        printStudentNameSynchronized(students.get(0).getName(), "Main Thread");
        printStudentNameSynchronized(students.get(1).getName(), "Main Thread");

        Thread thread1 = new Thread(() -> {
            printStudentNameSynchronized(students.get(2).getName(), "Sync-Thread-1");
            printStudentNameSynchronized(students.get(3).getName(), "Sync-Thread-1");
        });

        Thread thread2 = new Thread(() -> {
            printStudentNameSynchronized(students.get(4).getName(), "Sync-Thread-2");
            printStudentNameSynchronized(students.get(5).getName(), "Sync-Thread-2");
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            logger.error("Thread was interrupted", e);
            Thread.currentThread().interrupt();
        }

        logger.debug("Synchronized printing completed");
    }

    private void printStudentNameSynchronized(String studentName, String threadName) {
        synchronized (lock) {
            System.out.println(threadName + ": " + studentName);
        }
    }

    public List<Student> getFirstSixStudents() {
        List<Student> students = studentRepository.findAll();
        return students.stream()
                .limit(6)
                .collect(Collectors.toList());
    }
}