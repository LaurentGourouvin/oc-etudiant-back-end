package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.dto.StudentGetDTO;
import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.mapper.StudentDtoMapper;
import com.openclassrooms.etudiant.repository.StudentRepository;
import com.openclassrooms.etudiant.service.StudentService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.openclassrooms.etudiant.exception.StudentNotFoundException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentDtoMapper studentDtoMapper;

    @InjectMocks
    private StudentService studentService;

    @Test
    void createStudent_validStudent_shouldSave() {
        // GIVEN
        Student student = new Student();
        student.setFirstName("Ana");
        student.setLastName("Kim");
        student.setEmail("ana@ex.com");

        when(studentRepository.existsByEmail("ana@ex.com")).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN + THEN : la création ne doit pas lever d'exception et doit appeler save()
        assertDoesNotThrow(() -> studentService.createStudent(student));

        verify(studentRepository, times(1)).existsByEmail("ana@ex.com");

        // vérifier l'objet réellement passé à save()
        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository, times(1)).save(captor.capture());

        Student saved = captor.getValue();
        assertEquals("Ana", saved.getFirstName());
        assertEquals("Kim", saved.getLastName());
        assertEquals("ana@ex.com", saved.getEmail());

        verifyNoMoreInteractions(studentRepository);
    }

    @Test
    void getStudentById_existingStudent_shouldReturnDto() {
        // GIVEN
        Long id = 1L;

        Student student = new Student();
        student.setId(id);
        student.setFirstName("Ana");
        student.setLastName("Kim");
        student.setEmail("ana@ex.com");

        StudentGetDTO dto = new StudentGetDTO();
        dto.setId(id);
        dto.setFirstName("Ana");
        dto.setLastName("Kim");
        dto.setEmail("ana@ex.com");

        when(studentRepository.findById(id)).thenReturn(Optional.of(student));
        when(studentDtoMapper.toDto(student)).thenReturn(dto);

        // WHEN on récupère par id
        StudentGetDTO result = studentService.getStudentById(id);

        // THEN : on obtient le DTO et les bons appels sont effectués
        assertEquals(dto, result);

        verify(studentRepository, times(1)).findById(id);
        verify(studentDtoMapper, times(1)).toDto(student);
        verifyNoMoreInteractions(studentRepository, studentDtoMapper);
    }

    @Test
    void getStudentByEmail_existingStudent_shouldReturnDto() {
        // GIVEN
        String email = "ana@ex.com";

        Student student = new Student();
        student.setId(1L);
        student.setFirstName("Ana");
        student.setLastName("Kim");
        student.setEmail(email);

        StudentGetDTO dto = new StudentGetDTO();
        dto.setId(1L);
        dto.setFirstName("Ana");
        dto.setLastName("Kim");
        dto.setEmail(email);

        when(studentRepository.findByEmail(email)).thenReturn(Optional.of(student));
        when(studentDtoMapper.toDto(student)).thenReturn(dto);

        // WHEN :on récupère par email
        StudentGetDTO result = studentService.getStudentByEmail(email);

        // THEN on obtient le DTO et les bons appels sont effectués
        assertEquals(dto, result);

        verify(studentRepository, times(1)).findByEmail(email);
        verify(studentDtoMapper, times(1)).toDto(student);
        verifyNoMoreInteractions(studentRepository, studentDtoMapper);
    }

     @Test
    void getAllStudents_shouldReturnListOfDtos() {
        // GIVEN
        Student student1 = new Student();
        student1.setId(1L);
        student1.setFirstName("Ana");

        Student student2 = new Student();
        student2.setId(2L);
        student2.setFirstName("Tom");

        StudentGetDTO dto1 = new StudentGetDTO();
        dto1.setId(1L);
        dto1.setFirstName("Ana");

        StudentGetDTO dto2 = new StudentGetDTO();
        dto2.setId(2L);
        dto2.setFirstName("Tom");

        when(studentRepository.findAll()).thenReturn(List.of(student1, student2));
        when(studentDtoMapper.toDto(student1)).thenReturn(dto1);
        when(studentDtoMapper.toDto(student2)).thenReturn(dto2);

        // WHEN on récupère tous les étudiants
        List<StudentGetDTO> result = studentService.getAllStudents();

        // THEN liste de taille 2 et contenu attendu
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(dto1, result.get(0));
        assertEquals(dto2, result.get(1));

        verify(studentRepository, times(1)).findAll();
        verify(studentDtoMapper, times(1)).toDto(student1);
        verify(studentDtoMapper, times(1)).toDto(student2);
        verifyNoMoreInteractions(studentRepository, studentDtoMapper);
    }

    @Test
    void deleteStudent_existingStudent_shouldDelete() {
        // GIVEN
        Long id = 1L;
        when(studentRepository.existsById(id)).thenReturn(true);

        // WHEN + THEN suppression sans exception et deleteById appelé
        assertDoesNotThrow(() -> studentService.deleteStudent(id));

        verify(studentRepository, times(1)).existsById(id);
        verify(studentRepository, times(1)).deleteById(id);
        verifyNoMoreInteractions(studentRepository);
    }

    @Test
    void updateAll_existingStudent_shouldUpdateAndReturnDto() {
        // GIVEN
        Long id = 1L;

        Student existingStudent = new Student();
        existingStudent.setId(id);
        existingStudent.setFirstName("Old");
        existingStudent.setLastName("Name");
        existingStudent.setEmail("old@ex.com");

        StudentGetDTO updateDTO = new StudentGetDTO();
        updateDTO.setFirstName("New");
        updateDTO.setLastName("Name");
        updateDTO.setEmail("new@ex.com");

        Student savedStudent = new Student();
        savedStudent.setId(id);
        savedStudent.setFirstName("New");
        savedStudent.setLastName("Name");
        savedStudent.setEmail("new@ex.com");

        StudentGetDTO dtoResult = new StudentGetDTO();
        dtoResult.setId(id);
        dtoResult.setFirstName("New");
        dtoResult.setLastName("Name");
        dtoResult.setEmail("new@ex.com");

        when(studentRepository.findById(id)).thenReturn(Optional.of(existingStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);
        when(studentDtoMapper.toDto(savedStudent)).thenReturn(dtoResult);

        // WHEN mise à jour complète
        StudentGetDTO result = studentService.updateAll(id, updateDTO);

        // THEN
        // Vérifier que les champs ont bien été modifiés avant save
        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(captor.capture());

        Student updated = captor.getValue();
        assertEquals("New", updated.getFirstName());
        assertEquals("Name", updated.getLastName());
        assertEquals("new@ex.com", updated.getEmail());

        assertEquals(dtoResult, result);

        verify(studentRepository, times(1)).findById(id);
        verify(studentRepository, times(1)).save(existingStudent);
        verify(studentDtoMapper, times(1)).toDto(savedStudent);
        verifyNoMoreInteractions(studentRepository, studentDtoMapper);
    }

    @Test
    void getStudentById_notFound_shouldThrowStudentNotFoundException() {
        Long id = 1L;
        when(studentRepository.findById(id)).thenReturn(Optional.empty());

        // THEN exception + message attendu
        StudentNotFoundException ex = assertThrows(
                StudentNotFoundException.class,
                () -> studentService.getStudentById(id)
        );

        assertEquals("Student with ID 1 not found", ex.getMessage());

        verify(studentRepository, times(1)).findById(id);
        verifyNoInteractions(studentDtoMapper);
    }

    @Test
    void getStudentByEmail_notFound_shouldThrowStudentNotFoundException() {
        String email = "ana@ex.com";
        when(studentRepository.findByEmail(email)).thenReturn(Optional.empty());

        // WHEN + THEN: exception + message attendu
        StudentNotFoundException ex = assertThrows(
                StudentNotFoundException.class,
                () -> studentService.getStudentByEmail(email)
        );

        assertEquals("Student with email ana@ex.com not found", ex.getMessage());

        verify(studentRepository, times(1)).findByEmail(email);
        verifyNoInteractions(studentDtoMapper);
    }

    @Test
    void deleteStudent_notFound_shouldThrowStudentNotFoundException() {
        Long id = 1L;
        when(studentRepository.existsById(id)).thenReturn(false);

        // WHEN + THEN: exception + aucune suppression effectuée
        StudentNotFoundException ex = assertThrows(
                StudentNotFoundException.class,
                () -> studentService.deleteStudent(id)
        );

        assertEquals("Student with ID 1 not found", ex.getMessage());

        verify(studentRepository, times(1)).existsById(id);
        verify(studentRepository, never()).deleteById(anyLong());
        verifyNoInteractions(studentDtoMapper);
    }

    @Test
    void updateAll_studentNotFound_shouldThrowStudentNotFoundException() {
        Long id = 1L;
        StudentGetDTO input = new StudentGetDTO();
        input.setFirstName("New");
        input.setLastName("Name");
        input.setEmail("new@ex.com");

        when(studentRepository.findById(id)).thenReturn(Optional.empty());
        
        // WHEN + THEN: exception + aucun save effectué
        StudentNotFoundException ex = assertThrows(
                StudentNotFoundException.class,
                () -> studentService.updateAll(id, input)
        );

        assertEquals("Student with ID 1 not found", ex.getMessage());

        verify(studentRepository, times(1)).findById(id);
        verify(studentRepository, never()).save(any(Student.class));
        verifyNoInteractions(studentDtoMapper);
    }

}
