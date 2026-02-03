package com.openclassrooms.etudiant.mapper;

import com.openclassrooms.etudiant.dto.StudentCreateDTO;
import com.openclassrooms.etudiant.dto.StudentGetDTO;
import com.openclassrooms.etudiant.entities.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface StudentDtoMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created_at", ignore = true)
    @Mapping(target = "updated_at", ignore = true)
    Student toEntity(StudentCreateDTO studentDTO);

    StudentGetDTO toDto(Student student);
}
