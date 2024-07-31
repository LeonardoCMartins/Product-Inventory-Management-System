package com.example.testando_thymeleaf.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProductDto {

    private Long id;

    @NotEmpty(message = "the name is required")
    private String name;

    @NotEmpty(message = "the brand is required")
    private String brand;

    @NotEmpty(message = "the category is required")
    private String category;

    @Min(0)
    private double price;

    @Size(min = 10, message = "min 10 characteres")
    @Size(max = 100, message = "max 100 characters")
    private String description;

    private MultipartFile imageFile;

}
