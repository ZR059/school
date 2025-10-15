package ru.hogwarts.school.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repositories.AvatarRepository;
import ru.hogwarts.school.repositories.StudentRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class AvatarService {
    private final AvatarRepository avatarRepository;
    private final StudentRepository studentRepository;

    public AvatarService(AvatarRepository avatarRepository, StudentRepository studentRepository) {
        this.avatarRepository = avatarRepository;
        this.studentRepository = studentRepository;
    }

    public Optional<Avatar> findAvatarByStudentId(Long id) {
        return avatarRepository.findByStudentId(id);
    }

    public Page<Avatar> getAllAvatars(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return avatarRepository.findAll(pageable);
    }

    public Avatar saveAvatar(Avatar avatar) {
        return avatarRepository.save(avatar);
    }

    public void uploadAvatar(Long studentId, MultipartFile avatarFile) throws IOException {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Avatar avatar = findAvatarByStudentId(studentId)
                .orElse(new Avatar());

        avatar.setStudent(student);
        avatar.setMediaType(avatarFile.getContentType());
        avatar.setFileSize(avatarFile.getSize());
        avatar.setData(avatarFile.getBytes());

        String fileName = "avatar_" + studentId + "_" + System.currentTimeMillis() +
                getFileExtension(avatarFile.getOriginalFilename());
        Path filePath = Path.of("avatars", fileName);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, avatarFile.getBytes());
        avatar.setFilePath(filePath.toAbsolutePath().toString());

        saveAvatar(avatar);
    }

    private String getFileExtension(String fileName) {
        return fileName != null && fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf("."))
                : "";
    }
}
