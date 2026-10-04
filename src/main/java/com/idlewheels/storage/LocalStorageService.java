package com.idlewheels.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Profile({"dev", "test"})
public class LocalStorageService implements StorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_PHOTO_BYTES = 5L * 1024 * 1024;
    private final Path storageDirectory;

    public LocalStorageService(
            @Value("${idlewheels.storage.local-dir:uploads}") String storageDirectory
    ) {
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null
                ? "photo" : file.getOriginalFilename());
        int dot = originalName.lastIndexOf('.');
        String extension = dot < 0 ? "" : originalName.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Photo must be JPG, PNG, or WEBP.");
        }

        if (file.getSize() > MAX_PHOTO_BYTES) {
            throw new IllegalArgumentException("Photo must be no larger than 5 MB.");
        }

        try {
            byte[] contents = file.getBytes();
            if (!matchesImageType(extension, contents)) {
                throw new IllegalArgumentException("The selected file is not a valid " + extension.toUpperCase(Locale.ROOT) + " image.");
            }

            String key = UUID.randomUUID() + "." + extension;
            Files.createDirectories(storageDirectory);
            Files.write(storageDirectory.resolve(key), contents);
            return key;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save the photo.", exception);
        }
    }

    private boolean matchesImageType(String extension, byte[] contents) {
        boolean jpeg = contents.length >= 3
                && (contents[0] & 0xff) == 0xff && (contents[1] & 0xff) == 0xd8 && (contents[2] & 0xff) == 0xff;
        byte[] pngSignature = {(byte) 0x89, 'P', 'N', 'G', 0x0d, 0x0a, 0x1a, 0x0a};
        boolean png = contents.length >= pngSignature.length
                && Arrays.equals(Arrays.copyOf(contents, pngSignature.length), pngSignature);
        boolean webp = contents.length >= 12
                && contents[0] == 'R' && contents[1] == 'I' && contents[2] == 'F' && contents[3] == 'F'
                && contents[8] == 'W' && contents[9] == 'E' && contents[10] == 'B' && contents[11] == 'P';
        return switch (extension) {
            case "jpg", "jpeg" -> jpeg;
            case "png" -> png;
            case "webp" -> webp;
            default -> false;
        };
    }

    @Override
    public String getUrl(String key) {
        return key == null || key.isBlank() ? null : "/uploads/" + key;
    }

    @Override
    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        Path file = storageDirectory.resolve(key).normalize();
        if (!file.startsWith(storageDirectory)) {
            throw new IllegalArgumentException("Invalid photo key.");
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not delete the old photo.", exception);
        }
    }
}
