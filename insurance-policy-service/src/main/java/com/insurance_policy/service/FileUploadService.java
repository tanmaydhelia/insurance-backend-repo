package com.insurance_policy.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileUploadService {
    
    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) {
        try {
            @SuppressWarnings("rawtypes")
            Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(), 
                ObjectUtils.asMap("resource_type", "auto")
            );
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }
    }
    
    public String uploadFile(MultipartFile file, String folder) {
        try {
            @SuppressWarnings("rawtypes")
            Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(), 
                ObjectUtils.asMap(
                    "resource_type", "auto",
                    "folder", folder
                )
            );
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }
    }
}
