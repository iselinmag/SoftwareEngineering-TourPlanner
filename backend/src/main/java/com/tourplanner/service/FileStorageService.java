package com.tourplanner.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.tourplanner.exception.NotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

// this service is the only place that touches image files on disk.
// it saves uploaded files and hands back the stored file name.
@Service
public class FileStorageService {

    private static final Logger logger = LogManager.getLogger(FileStorageService.class);

    // the folder where images are kept, read from config so it is not hardcoded
    private final Path uploadDir;

    public FileStorageService(@Value("${tourplanner.upload.dir}") String dir) {
        this.uploadDir = Paths.get(dir).toAbsolutePath().normalize();
        try {
            // make sure the folder exists when the app starts
            Files.createDirectories(this.uploadDir);
            logger.info("Upload directory ready at {}", this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create the upload directory", e);
        }
    }

    // save an uploaded image and return the unique name we stored it under
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new NotFoundException("No image file was provided");
        }

        // keep the original extension so the browser knows the image type
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) {
            extension = original.substring(dot);
        }

        // give every file a unique name so two photos called the same thing do not clash
        String storedName = UUID.randomUUID() + extension;
        Path target = this.uploadDir.resolve(storedName);

        try {
            Files.copy(file.getInputStream(), target);
            logger.info("Stored image as {}", storedName);
        } catch (IOException e) {
            throw new RuntimeException("Could not save the image file", e);
        }

        return storedName;
    }
}
