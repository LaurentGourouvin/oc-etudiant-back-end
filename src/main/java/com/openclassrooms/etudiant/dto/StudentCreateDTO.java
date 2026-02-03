package com.openclassrooms.etudiant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudentCreateDTO {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @NotBlank private String email;
}
