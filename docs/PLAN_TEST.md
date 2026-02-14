# 🧪 Plan de tests – Application Student (Backend)

## Si problème de cache MAVEN :

- mvn clean
- mvn clean package
- mvn spring-boot:run
- mvn clean test

> Essayer d'ajouter @ComponentScan("com.openclassrooms.etudiant") à la class de test
> Supprimer le dossier target
> Utiliser l'extension VSCode JAVA pour rebuild

## 🎯 Objectif

Définir les cas de tests à écrire en respectant :

- La pyramide des tests
- Les comportements attendus
- Aucun cas d’erreur
- Aucun test d’effet de bord
- Priorité aux tests simples

---

# 🔺 Pyramide des tests

1. Tests unitaires (majoritaires)
2. Tests d’intégration backend (HTTP).

---

# 🧩 Backend – Tests Unitaires (StudentService)

## 1. createStudent(Student)

### Cas : création d’un étudiant valide

**Entrée :**

- Student("Ana", "Kim", "ana@ex.com")

**Mocks :**

- existsByEmail("ana@ex.com") → false
- save(student) → student

**Résultat attendu :**

- La méthode se termine sans exception
- existsByEmail appelé une fois
- save appelé une fois

---

## 2. getStudentById(Long)

### Cas : récupération d’un étudiant existant

**Entrée :**

- id = 1

**Mocks :**

- findById(1) → Optional.of(student)
- toDto(student) → dto

**Résultat attendu :**

- Retourne dto
- findById appelé
- toDto appelé

---

## 3. getStudentByEmail(String)

### Cas : récupération par email

**Entrée :**

- email = "ana@ex.com"

**Mocks :**

- findByEmail("ana@ex.com") → Optional.of(student)
- toDto(student) → dto

**Résultat attendu :**

- Retourne dto

---

## 4. getAllStudents()

### Cas : récupération de tous les étudiants

**Mocks :**

- findAll() → [student1, student2]
- toDto(student1) → dto1
- toDto(student2) → dto2

**Résultat attendu :**

- Retourne [dto1, dto2]
- Taille de la liste = 2

---

## 5. deleteStudent(Long)

### Cas : suppression d’un étudiant existant

**Entrée :**

- id = 1

**Mocks :**

- existsById(1) → true

**Résultat attendu :**

- La méthode se termine sans exception
- deleteById(1) appelé une fois

---

## 6. updateAll(Long, StudentGetDTO)

### Cas : mise à jour complète des données

**Entrées :**

- id = 1
- StudentGetDTO("New", "Name", "new@ex.com")

**Mocks :**

- findById(1) → Optional.of(existingStudent)
- save(existingStudent) → savedStudent
- toDto(savedStudent) → dtoResult

**Résultat attendu :**

- firstName, lastName et email mis à jour
- Retourne dtoResult
- save appelé une fois

---

## 7. getStudentById(Long)

### Cas : étudiant introuvable

**Entrée :**

- id = 1

**Mocks :**

- findById(1) → Optional.empty()

**Résultat attendu :**

- Lance StudentNotFoundException
- Message = "Student with ID 1 not found"
- studentDtoMapper n’est jamais appelé.

---

## 8. getStudentByEmail(String)

### Cas : email introuvable

**Entrée :**

- email = "ana@ex.com"

**Mocks :**

- findByEmail("ana@ex.com") → Optional.empty()

**Résultat attendu :**

- Lance StudentNotFoundException
- Message = "Student with email ana@ex.com not found"
- studentDtoMapper n’est jamais appelé.

---

## 9. deleteStudent(Long)

### Cas : suppression d’un étudiant introuvable

**Entrée :**

- id = 1

**Mocks :**

- existsById(1) → false

**Résultat attendu :**

- Lance StudentNotFoundException
- Message = "Student with ID 1 not found"
- deleteById n’est jamais appelé

---

## 10. updateAll(Long, StudentGetDTO)

### Cas : mise à jour d’un étudiant introuvable

**Entrées :**

- id = 1
- StudentGetDTO("New", "Name", "new@ex.com")

**Mocks :**

- findById(1) → Optional.empty()

**Résultat attendu :**

- Lance StudentNotFoundException
- Message = "Student with ID 1 not found"
- save n’est jamais appelé
- studentDtoMapper n’est jamais appelé

---

# 🌐 Backend – Tests d’Intégration (Controller + HTTP)

---

## 1. POST /api/student

### Cas : création d’un étudiant

**Requête :**

```json
{
  "firstName": "Ana",
  "lastName": "Kim",
  "email": "ana@ex.com"
}
```

**Résultat attendu**

- Status: `201 Created`

---

## 2) GET `/api/student`

### Cas : récupérer la liste des étudiants

**Précondition**

- Deux étudiants existent (ex: via setup repository ou via deux POST)

**Requête**

- Method: `GET`
- URL: `/api/student`

**Résultat attendu**

- Status: `200 OK`
- Body: tableau JSON de taille `2`
- Chaque élément contient au minimum :
  - `firstName`
  - `lastName`
  - `email`

---

## 3) GET `/api/student/{id}`

### Cas : récupérer un étudiant par id

**Précondition**

- Un étudiant existe avec `id = 1` (ou un id récupéré au setup)

**Requête**

- Method: `GET`
- URL: `/api/student/1`

**Résultat attendu**

- Status: `200 OK`
- Body JSON contenant :
  - `firstName`
  - `lastName`
  - `email`

---

## 4) GET `/api/student/by-email?email=ana@ex.com`

### Cas : récupérer un étudiant par email

**Précondition**

- Un étudiant existe avec `email = "ana@ex.com"`

**Requête**

- Method: `GET`
- URL: `/api/student/by-email?email=ana@ex.com`

**Résultat attendu**

- Status: `200 OK`
- Body :
  - `email` = `"ana@ex.com"`

---

## 5) PUT `/api/student/{id}`

### Cas : mettre à jour toutes les données d’un étudiant

**Précondition**

- Un étudiant existe avec `id = 1`

**Requête**

- Method: `PUT`
- URL: `/api/student/1`
- Body JSON :
  {
  "firstName": "New",
  "lastName": "Name",
  "email": "new@ex.com"
  }

**Résultat attendu**

- Status: `200 OK`
- Body :
  - `firstName` = `"New"`
  - `lastName` = `"Name"`
  - `email` = `"new@ex.com"`

---

## 6) DELETE `/api/student/{id}`

### Cas : supprimer un étudiant

**Précondition**

- Un étudiant existe avec `id = 1`

**Requête**

- Method: `DELETE`
- URL: `/api/student/1`

**Résultat attendu**

- Status: `204 No Content`
