package com.openclassrooms.etudiant.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.openclassrooms.etudiant.dto.StudentGetDTO;
import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.exception.StudentNotFoundException;
import com.openclassrooms.etudiant.mapper.StudentDtoMapper;
import com.openclassrooms.etudiant.repository.StudentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final StudentDtoMapper studentDtoMapper;
    
    public void createStudent(Student student) {
        Assert.notNull(student, "Student must not be null");

        if (studentRepository.existsByEmail(student.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        studentRepository.save(student);
    }
    
    public StudentGetDTO getStudentById(Long id) {
        Assert.notNull(id, "ID must not be null");

        return studentRepository
            .findById(id)
            .map(studentDtoMapper::toDto)
            .orElseThrow(() -> new StudentNotFoundException(id));
    }   

    public StudentGetDTO getStudentByEmail(String email) {
        Assert.notNull(email, "Email must not be null");
        
        return studentRepository
            .findByEmail(email)
            .map(studentDtoMapper::toDto)
            .orElseThrow(() -> new StudentNotFoundException(email));
    }   

    public List<StudentGetDTO> getAllStudents() {
        return studentRepository
            .findAll()
            .stream()
            .map(studentDtoMapper::toDto)
            .toList();
    }

    public void deleteStudent(Long id) {
        Assert.notNull(id, "ID must not be null");

        if (!studentRepository.existsById(id)) {
            throw new StudentNotFoundException(id);
        }

        studentRepository.deleteById(id);
    }

    public StudentGetDTO updateAll(Long id, StudentGetDTO student) {
        Assert.notNull(id, "ID must not be null");
        Assert.notNull(student, "Student data must not be null");

        Student existingStudent = studentRepository.findById(id)
            .orElseThrow(() -> new StudentNotFoundException(id));

        existingStudent.setFirstName(student.getFirstName());
        existingStudent.setLastName(student.getLastName());
        existingStudent.setEmail(student.getEmail());
        existingStudent.setUpdated_at(java.time.LocalDateTime.now());

        Student updatedStudent = studentRepository.save(existingStudent);
        return studentDtoMapper.toDto(updatedStudent);
    }
}
