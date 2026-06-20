package com.fooddelivery.restaurantservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ImageUploadRequest {

    @NotNull(message = "Image file is required")
    private MultipartFile image;
}