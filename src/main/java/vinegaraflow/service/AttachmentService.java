package vinegaraflow.service;

import vinegaraflow.entity.Attachment;
import vinegaraflow.entity.Task;
import vinegaraflow.entity.User;
import vinegaraflow.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public Attachment saveFile(MultipartFile file, Task task, User uploader) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String originalFileName = file.getOriginalFilename();
        String fileName = UUID.randomUUID() + "_" + originalFileName;
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        Attachment attachment = Attachment.builder()
                .fileName(originalFileName)
                .filePath(filePath.toString())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .task(task)
                .uploader(uploader)
                .build();
        return attachmentRepository.save(attachment);
    }

    public void deleteFile(Attachment attachment) throws IOException {
        Path filePath = Paths.get(attachment.getFilePath());
        Files.deleteIfExists(filePath);
        attachmentRepository.delete(attachment);
    }
}