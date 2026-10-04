package com.idlewheels.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String upload(MultipartFile file);
    String getUrl(String key);
    void delete(String key);
}
