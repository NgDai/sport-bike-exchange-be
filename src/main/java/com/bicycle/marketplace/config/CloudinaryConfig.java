package com.bicycle.marketplace.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dgle3xj6c",
                "api_key", "847658377135851",
                "api_secret", "uqaUOw18PiHFmoFiE9vDr_MxPWQ",
                "secure", true
        ));
    }
}