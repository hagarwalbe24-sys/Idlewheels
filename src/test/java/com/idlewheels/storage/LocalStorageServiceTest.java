package com.idlewheels.storage;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalStorageServiceTest {
    @Test
    void rejectsFakeJpgWithNonImageMagicBytes() {
        LocalStorageService storage = new LocalStorageService(Path.of("target", "upload-tests", UUID.randomUUID().toString()).toString());
        MockMultipartFile fakePhoto = new MockMultipartFile(
                "photo", "not-an-image.jpg", "image/jpeg", "this is plain text".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> storage.upload(fakePhoto));
    }

    @Test
    void rejectsFilesLargerThanFiveMegabytes() {
        LocalStorageService storage = new LocalStorageService(Path.of("target", "upload-tests", UUID.randomUUID().toString()).toString());
        byte[] tooLarge = new byte[5 * 1024 * 1024 + 1];
        tooLarge[0] = (byte) 0xff;
        tooLarge[1] = (byte) 0xd8;
        tooLarge[2] = (byte) 0xff;
        MockMultipartFile photo = new MockMultipartFile("photo", "large.jpg", "image/jpeg", tooLarge);

        assertThrows(IllegalArgumentException.class, () -> storage.upload(photo));
    }
}
