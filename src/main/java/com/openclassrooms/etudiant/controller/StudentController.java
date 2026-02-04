package com.openclassrooms.etudiant.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.etudiant.dto.StudentCreateDTO;
import com.openclassrooms.etudiant.dto.StudentGetDTO;
import com.openclassrooms.etudiant.mapper.StudentDtoMapper;
import com.openclassrooms.etudiant.service.StudentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;




@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;
    private final StudentDtoMapper studentDtoMapper;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody StudentCreateDTO studentCreateDTO) {
        studentService.createStudent(studentDtoMapper.toEntity(studentCreateDTO));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<StudentGetDTO>> getAllStudents() {
        return new ResponseEntity<List<StudentGetDTO>>(studentService.getAllStudents(), HttpStatus.OK);
    }
    
    @GetMapping("/by-email")
    public ResponseEntity<StudentGetDTO> getStudentByEmail(@RequestParam String email) {
        return new ResponseEntity<StudentGetDTO>(studentService.getStudentByEmail(email), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentGetDTO> getStudentById(@PathVariable Long id) {
        return new ResponseEntity<StudentGetDTO>(studentService.getStudentById(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentGetDTO> updateAllDataOfStudent(@PathVariable(required = true) Long id, @RequestBody StudentGetDTO studentGetDTO) {
        return new ResponseEntity<StudentGetDTO>(studentService.updateAll(id, studentGetDTO), HttpStatus.OK);
    }
}
