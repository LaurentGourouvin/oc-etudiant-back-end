package com.openclassrooms.etudiant.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.etudiant.dto.LoginRequestDTO;
import com.openclassrooms.etudiant.dto.StudentCreateDTO;
import com.openclassrooms.etudiant.dto.StudentGetDTO;
import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.entities.User;
import com.openclassrooms.etudiant.repository.StudentRepository;
import com.openclassrooms.etudiant.repository.UserRepository;
import com.openclassrooms.etudiant.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class StudentControllerTest {

    private static final String STUDENT_BASE_URL = "/api/student";
    private static final String LOGIN_URL = "/api/login";

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String LOGIN = "login";
    private static final String PASSWORD = "password";

    @Container
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0");

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private StudentRepository studentRepository;

    private String token; 

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        // Configuration dynamique de la BDD MySQL Testcontainers pour les tests d'intégration
        registry.add("spring.datasource.url", () -> mySQLContainer.getJdbcUrl());
        registry.add("spring.datasource.username", () -> mySQLContainer.getUsername());
        registry.add("spring.datasource.password", () -> mySQLContainer.getPassword());
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

    @BeforeEach
    void beforeEach() throws Exception {
        // nettoyage
        studentRepository.deleteAll();
        userRepository.deleteAll();

        // créer un user et récupérer token
        User user = new User();
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);
        userService.register(user);

        token = loginAndGetToken(LOGIN, PASSWORD);
    }

    @AfterEach
    void afterEach() {
        // Nettoyage après chaque test pour éviter les effets de bord entre tests
        studentRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String loginAndGetToken(String login, String password) throws Exception {
        LoginRequestDTO body = new LoginRequestDTO();
        body.setLogin(login);
        body.setPassword(password);

        String response = mockMvc.perform(MockMvcRequestBuilders.post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("token").asText();
    }

    private String bearer() {
        return "Bearer " + token;
    }

    @Test
    void createStudent_shouldReturn201() throws Exception {
        // on appelle POST /api/student avec un token valide
        // l'API doit répondre 201 Created
        StudentCreateDTO body = new StudentCreateDTO();
        body.setFirstName("Ana");
        body.setLastName("Kim");
        body.setEmail("ana@ex.com");

        mockMvc.perform(MockMvcRequestBuilders.post(STUDENT_BASE_URL)
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());
    }

    @Test
    void getAllStudents_shouldReturnListOf2() throws Exception {

        // on appelle GET /api/student
        // on récupère une liste JSON de taille 2, contenant au minimum firstName/lastName/email
        Student s1 = new Student();
        s1.setFirstName("Ana");
        s1.setLastName("Kim");
        s1.setEmail("ana@ex.com");

        Student s2 = new Student();
        s2.setFirstName("Tom");
        s2.setLastName("Lee");
        s2.setEmail("tom@ex.com");

        studentRepository.save(s1);
        studentRepository.save(s2);

        mockMvc.perform(MockMvcRequestBuilders.get(STUDENT_BASE_URL)
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName").exists())
                .andExpect(jsonPath("$[0].lastName").exists())
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[1].firstName").exists())
                .andExpect(jsonPath("$[1].lastName").exists())
                .andExpect(jsonPath("$[1].email").exists());
    }

    @Test
    void getStudentById_shouldReturn200() throws Exception {
        // on appelle GET /api/student/{id}
        // on récupère 200 OK + les champs attendus

        Student s = new Student();
        s.setFirstName("Ana");
        s.setLastName("Kim");
        s.setEmail("ana@ex.com");
        Student saved = studentRepository.save(s);

        mockMvc.perform(MockMvcRequestBuilders.get(STUDENT_BASE_URL + "/" + saved.getId())
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Ana")))
                .andExpect(jsonPath("$.lastName", is("Kim")))
                .andExpect(jsonPath("$.email", is("ana@ex.com")));
    }

    @Test
    void getStudentByEmail_shouldReturn200() throws Exception {
         // on appelle GET /api/student/by-email?email=...
        // on récupère 200 OK et l'email correspondant
        Student s = new Student();
        s.setFirstName("Ana");
        s.setLastName("Kim");
        s.setEmail("ana@ex.com");
        studentRepository.save(s);

        mockMvc.perform(MockMvcRequestBuilders.get(STUDENT_BASE_URL + "/by-email")
                        .header("Authorization", bearer())
                        .queryParam("email", "ana@ex.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("ana@ex.com")));
    }

    @Test
    void updateAll_shouldReturn200AndUpdatedStudent() throws Exception {
        // on appelle PUT /api/student/{id}
        // on récupère 200 OK + le JSON mis à jour
        Student s = new Student();
        s.setFirstName("Ana");
        s.setLastName("Kim");
        s.setEmail("ana@ex.com");
        Student saved = studentRepository.save(s);

        StudentGetDTO body = new StudentGetDTO();
        body.setFirstName("New");
        body.setLastName("Name");
        body.setEmail("new@ex.com");

        mockMvc.perform(MockMvcRequestBuilders.put(STUDENT_BASE_URL + "/" + saved.getId())
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("New")))
                .andExpect(jsonPath("$.lastName", is("Name")))
                .andExpect(jsonPath("$.email", is("new@ex.com")));
    }

    @Test
    void deleteStudent_shouldReturn204() throws Exception {
        // on appelle DELETE /api/student/{id}
        // l'API doit répondre 204 No Content
        Student s = new Student();
        s.setFirstName("Ana");
        s.setLastName("Kim");
        s.setEmail("ana@ex.com");
        Student saved = studentRepository.save(s);

        mockMvc.perform(MockMvcRequestBuilders.delete(STUDENT_BASE_URL + "/" + saved.getId())
                        .header("Authorization", bearer()))
                .andExpect(status().isNoContent());
    }

    
    @Test
    void createStudent_invalidBody_shouldReturn400() throws Exception {
        // @Valid doit déclencher un 400 si StudentCreateDTO a @NotBlank/@Email
        // on appelle POST /api/student avec un DTO invalide
        // l'API doit répondre 400 Bad Request
        StudentCreateDTO body = new StudentCreateDTO();

        mockMvc.perform(MockMvcRequestBuilders.post(STUDENT_BASE_URL)
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void createStudent_existingEmail_shouldReturn4xx() throws Exception {
        // GIVEN: un étudiant existe déjà
        // on appelle POST /api/student avec un email déjà existant
        // l'API doit renvoyer une erreur 4xx
        Student existing = new Student();
        existing.setFirstName("Ana");
        existing.setLastName("Kim");
        existing.setEmail("ana@ex.com");
        studentRepository.save(existing);

        StudentCreateDTO body = new StudentCreateDTO();
        body.setFirstName("Ana");
        body.setLastName("Kim");
        body.setEmail("ana@ex.com");

        mockMvc.perform(MockMvcRequestBuilders.post(STUDENT_BASE_URL)
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void getStudentById_notFound_shouldReturn404() throws Exception {
        // on appelle GET /api/student/999999
        // ㅣ'API doit répondre 404 Not Found
        mockMvc.perform(MockMvcRequestBuilders.get(STUDENT_BASE_URL + "/999999")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getStudentByEmail_notFound_shouldReturn404() throws Exception {
        // on appelle GET /api/student/by-email?email=missing@ex.com
        // l'API doit répondre 404 Not Found
        mockMvc.perform(MockMvcRequestBuilders.get(STUDENT_BASE_URL + "/by-email")
                        .header("Authorization", bearer())
                        .queryParam("email", "missing@ex.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteStudent_notFound_shouldReturn404() throws Exception {
        // on appelle DELETE /api/student/999999
        // l'API doit répondre 404 Not Found
        mockMvc.perform(MockMvcRequestBuilders.delete(STUDENT_BASE_URL + "/999999")
                        .header("Authorization", bearer()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAll_notFound_shouldReturn404() throws Exception {
        // on appelle PUT /api/student/999999
        // l'API doit répondre 404 Not Found
        StudentGetDTO body = new StudentGetDTO();
        body.setFirstName("New");
        body.setLastName("Name");
        body.setEmail("new@ex.com");

        mockMvc.perform(MockMvcRequestBuilders.put(STUDENT_BASE_URL + "/999999")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    
}
