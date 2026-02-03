package com.openclassrooms.etudiant.dto;

import lombok.Data;

@Data
public class StudentGetDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String created_at;
    private String updated_at;
}
