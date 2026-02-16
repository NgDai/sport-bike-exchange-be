package com.bicycle.marketplace.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CloudinaryService {

    Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) throws IOException {
        // upload(file.getBytes(), options)
        // options: ObjectUtils.emptyMap() nghĩa là dùng cấu hình mặc định
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

        // Trả về đường dẫn ảnh (secure_url là đường dẫn https)
        return uploadResult.get("secure_url").toString();
    }

    // (Tùy chọn) Hàm xóa ảnh, publicId là ID của ảnh trên Cloudinary
    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}