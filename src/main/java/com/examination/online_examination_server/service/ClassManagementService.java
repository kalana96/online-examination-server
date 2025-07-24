package com.examination.online_examination_server.service;

import com.examination.online_examination_server.Utility.VarList;
import com.examination.online_examination_server.dto.*;
import com.examination.online_examination_server.entity.Class;
import com.examination.online_examination_server.entity.Subject;
import com.examination.online_examination_server.entity.Grade;
import com.examination.online_examination_server.entity.Teacher;
import com.examination.online_examination_server.repository.ClassRepository;
import com.examination.online_examination_server.repository.SubjectRepository;
import com.examination.online_examination_server.repository.GradeRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class ClassManagementService {

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private ModelMapper modelMapper;

    // Method to validate Class input
    private String validateClassInput(ClassDTO classDTO) {
        if (classDTO == null || classDTO.getClassName() == null || classDTO.getClassName().trim().isEmpty()) {
            log.warn("Invalid class data provided - class name is required");
            return VarList.RES_ERROR;
        }

        if (classDTO.getClassDate() == null) {
            log.warn("Class date is required");
            return VarList.RES_ERROR;
        }

        if (classDTO.getStartTime() == null) {
            log.warn("Start time is required");
            return VarList.RES_ERROR;
        }

        if (classDTO.getGradeId() <= 0) {
            log.warn("Valid grade ID is required");
            return VarList.RES_ERROR;
        }

        return VarList.RES_SUCCESS;
    }

    // Method to check for duplicates when saving
    private String checkForDuplicates(ClassDTO classDTO) {
        try {
            if (classRepository.existsByClassNameAndClassDateAndStartTime(
                    classDTO.getClassName(), classDTO.getClassDate(), classDTO.getStartTime())) {
                log.warn("Class with name '{}' at date '{}' and time '{}' already exists",
                        classDTO.getClassName(), classDTO.getClassDate(), classDTO.getStartTime());
                return VarList.RES_DUPLICATE;
            }
            return VarList.RES_SUCCESS;
        } catch (Exception ex) {
            log.error("Error checking for duplicates: ", ex);
            return VarList.RES_ERROR;
        }
    }

    // Method to check for duplicates when updating (excluding current class)
    private String checkForUpdateDuplicates(ClassDTO classDTO, Class existingClass) {
        if (!classDTO.getClassName().equals(existingClass.getClassName()) ||
                !classDTO.getClassDate().equals(existingClass.getClassDate()) ||
                !classDTO.getStartTime().equals(existingClass.getStartTime())) {

            if (classRepository.existsByClassNameAndClassDateAndStartTimeAndIdNot(
                    classDTO.getClassName(), classDTO.getClassDate(), classDTO.getStartTime(), classDTO.getId())) {
                log.warn("Another class with name '{}' at date '{}' and time '{}' already exists",
                        classDTO.getClassName(), classDTO.getClassDate(), classDTO.getStartTime());
                return VarList.RES_DUPLICATE;
            }
        }
        return VarList.RES_SUCCESS;
    }

    // Method to validate subject
    private Optional<Subject> validateSubject(Integer subjectId) {
        if (subjectId == null || subjectId <= 0) {
            return Optional.empty(); // Subject is optional
        }

        try {
            Optional<Subject> subjectOpt = subjectRepository.findById(subjectId);
            if (!subjectOpt.isPresent()) {
                log.warn("Subject with ID {} not found or is deleted", subjectId);
            }
            return subjectOpt;
        } catch (Exception ex) {
            log.error("Error validating subject with ID {}: ", subjectId, ex);
            return Optional.empty();
        }
    }

    // Method to validate grade
    private Optional<Grade> validateGrade(Integer gradeId) {
        if (gradeId == null || gradeId <= 0) {
            log.warn("Grade ID is required");
            return Optional.empty();
        }

        try {
            Optional<Grade> gradeOpt = gradeRepository.findById(gradeId);
            if (!gradeOpt.isPresent()) {
                log.warn("Grade with ID {} not found", gradeId);
            }
            return gradeOpt;
        } catch (Exception ex) {
            log.error("Error validating grade with ID {}: ", gradeId, ex);
            return Optional.empty();
        }
    }

    // Method to save a new class
    public String saveClass(ClassDTO classDTO) {
        try {
            // Validate input
            String validationResult = validateClassInput(classDTO);
            if (!VarList.RES_SUCCESS.equals(validationResult)) {
                return validationResult;
            }

            // Check for duplicates
            String duplicateCheck = checkForDuplicates(classDTO);
            if (!VarList.RES_SUCCESS.equals(duplicateCheck)) {
                return duplicateCheck;
            }

            // Validate grade
            Optional<Grade> gradeOpt = validateGrade(classDTO.getGradeId());
            if (!gradeOpt.isPresent()) {
                return VarList.RES_GRADE_NOT_FOUND;
            }

            Grade grade = gradeOpt.get();

            // Validate subject (optional)
            Subject subject = null;
            if (classDTO.getSubjectId() != null && classDTO.getSubjectId() > 0) {
                Optional<Subject> subjectOpt = validateSubject(classDTO.getSubjectId());
                if (!subjectOpt.isPresent()) {
                    return VarList.RES_NO_SUBJECTS;
                }
                subject = subjectOpt.get();
            }

            // Create and save class
            Class classEntity = createClassEntity(classDTO, grade, subject);
            classRepository.save(classEntity);

            log.info("Class '{}' saved successfully with ID {}", classEntity.getClassName(), classEntity.getId());
            return VarList.RES_SUCCESS;

        } catch (DataAccessException ex) {
            log.error("Database error while saving class: ", ex);
            return VarList.RES_ERROR;
        } catch (Exception ex) {
            log.error("Unexpected error while saving class: ", ex);
            return VarList.RES_ERROR;
        }
    }

    // Helper method to create Class entity
    private Class createClassEntity(ClassDTO classDTO, Grade grade, Subject subject) {
        Class classEntity = new Class();
        classEntity.setClassName(classDTO.getClassName());
        classEntity.setDescription(classDTO.getDescription());
        classEntity.setClassDate(classDTO.getClassDate());
        classEntity.setStartTime(classDTO.getStartTime());
        classEntity.setGrade(grade);
        classEntity.setSubject(subject);

        return classEntity;
    }

    // Method to update an existing class
    public String updateClass(ClassDTO classDTO) {
        try {
            // Validate input
            String validationResult = validateClassInput(classDTO);
            if (!VarList.RES_SUCCESS.equals(validationResult)) {
                return validationResult;
            }

            if (classDTO.getId() <= 0) {
                log.warn("Invalid class ID for update");
                return VarList.RES_ERROR;
            }

            // Find existing class
            Optional<Class> existingClassOpt = classRepository.findActiveById(classDTO.getId());
            if (!existingClassOpt.isPresent()) {
                log.warn("Class with ID {} not found or is deleted", classDTO.getId());
                return VarList.RES_NO_DATE_FOUND;
            }
            Class existingClass = existingClassOpt.get();

            // Check for duplicates (excluding current class)
            String duplicateCheck = checkForUpdateDuplicates(classDTO, existingClass);
            if (!VarList.RES_SUCCESS.equals(duplicateCheck)) {
                return duplicateCheck;
            }

            // Validate grade
            Optional<Grade> gradeOpt = validateGrade(classDTO.getGradeId());
            if (!gradeOpt.isPresent()) {
                return VarList.RES_GRADE_NOT_FOUND;
            }

            Grade grade = gradeOpt.get();

            // Validate subject (optional)
            Subject subject = null;
            if (classDTO.getSubjectId() != null && classDTO.getSubjectId() > 0) {
                Optional<Subject> subjectOpt = validateSubject(classDTO.getSubjectId());
                if (!subjectOpt.isPresent()) {
                    return VarList.RES_NO_SUBJECTS;
                }
                subject = subjectOpt.get();
            }

            // Update class entity
            updateClassEntity(existingClass, classDTO, grade, subject);

            // Save updated class
            classRepository.save(existingClass);
            log.info("Class with ID {} updated successfully", classDTO.getId());
            return VarList.RES_SUCCESS;

        } catch (DataAccessException ex) {
            log.error("Database error while updating class: ", ex);
            return VarList.RES_ERROR;
        } catch (Exception ex) {
            log.error("Unexpected error while updating class: ", ex);
            return VarList.RES_ERROR;
        }
    }

    // Method to update class entity
    private void updateClassEntity(Class existingClass, ClassDTO classDTO, Grade grade, Subject subject) {
        existingClass.setClassName(classDTO.getClassName());
        existingClass.setDescription(classDTO.getDescription());
        existingClass.setClassDate(classDTO.getClassDate());
        existingClass.setStartTime(classDTO.getStartTime());
        existingClass.setGrade(grade);
        existingClass.setSubject(subject);
    }

    //Single getAllClasses method with proper implementation
    @Transactional(readOnly = true)
    public List<ClassDTO> getAllClasses() {
        try {
            log.info("Fetching all active classes");
            List<Class> classes = classRepository.findAllActive();
            log.info("Found {} active classes", classes.size());

            if (classes.isEmpty()) {
                log.warn("No active classes found in database");
                return new ArrayList<>();
            }

            List<ClassDTO> classDTOs = classes.stream()
                    .map(this::mapClassToDTO)
                    .collect(Collectors.toList());

            log.info("Successfully mapped {} classes to DTOs with student counts", classDTOs.size());
            return classDTOs;

        } catch (Exception ex) {
            log.error("Error fetching all classes: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to fetch classes: " + ex.getMessage(), ex);
        }
    }

    // mapClassToDTO method with student count calculation
    private ClassDTO mapClassToDTO(Class classEntity) {
        try {
            ClassDTO classDTO = modelMapper.map(classEntity, ClassDTO.class);

            // Handle grade information
            if (classEntity.getGrade() != null) {
                classDTO.setGradeId(classEntity.getGrade().getId());
                classDTO.setGradeName(classEntity.getGrade().getGradeName());

                GradeDTO gradeDTO = modelMapper.map(classEntity.getGrade(), GradeDTO.class);
                classDTO.setGrade(gradeDTO);
            } else {
                log.warn("Class {} has no grade assigned", classEntity.getId());
            }

            // Handle subject information
            if (classEntity.getSubject() != null && !classEntity.getSubject().isDeleted()) {
                classDTO.setSubjectId(classEntity.getSubject().getId());
                classDTO.setSubjectName(classEntity.getSubject().getSubjectName());

                SubjectDTO subjectDTO = modelMapper.map(classEntity.getSubject(), SubjectDTO.class);
                classDTO.setSubject(subjectDTO);
            } else if (classEntity.getSubject() != null && classEntity.getSubject().isDeleted()) {
                classDTO.setSubjectName("Subject Deleted");
                classDTO.setSubjectId(null); // Clear the subject ID for deleted subjects
                log.warn("Class {} has deleted subject", classEntity.getId());
            } else {
                classDTO.setSubjectName("No Subject");
                log.warn("Class {} has no subject assigned", classEntity.getId());
            }

            // NEW: Handle teacher information
            if (classEntity.getTeachers() != null && !classEntity.getTeachers().isEmpty()) {
                List<String> teacherNames = new ArrayList<>();
                List<Integer> teacherIds = new ArrayList<>();
                List<TeacherDTO> teacherDTOs = new ArrayList<>();

                for (Teacher teacher : classEntity.getTeachers()) {
                    // Check if teacher is not deleted (assuming Teacher entity has isDeleted field)
                    if (!teacher.isDeleted()) {
                        // Add teacher name (you might need to adjust based on your Teacher entity structure)
                        String teacherName = buildTeacherName(teacher);
                        teacherNames.add(teacherName);
                        teacherIds.add(teacher.getId());

                        // Optionally map full teacher information
                        TeacherDTO teacherDTO = modelMapper.map(teacher, TeacherDTO.class);
                        teacherDTOs.add(teacherDTO);
                    }
                }

                classDTO.setTeacherNames(teacherNames);
                classDTO.setTeacherIds(teacherIds);
                classDTO.setTeachers(teacherDTOs);

                // Create comma-separated string for easy display
                classDTO.setTeacherNamesString(String.join(", ", teacherNames));

                log.debug("Class {} has {} teachers assigned", classEntity.getId(), teacherNames.size());
            } else {
                classDTO.setTeacherNames(new ArrayList<>());
                classDTO.setTeacherIds(new ArrayList<>());
                classDTO.setTeachers(new ArrayList<>());
                classDTO.setTeacherNamesString("No Teachers Assigned");
                log.warn("Class {} has no teachers assigned", classEntity.getId());
            }

            // Calculate and set student count
            Long studentCount = calculateStudentCount(classEntity);
            classDTO.setStudentCount(studentCount);

            log.debug("Class {} mapped successfully with {} students",
                    classEntity.getId(), studentCount);

            return classDTO;

        } catch (Exception ex) {
            log.error("Error mapping class {} to DTO: {}",
                    classEntity.getId(), ex.getMessage(), ex);
            throw new RuntimeException("Failed to map class to DTO: " + ex.getMessage(), ex);
        }
    }

    // Helper method to build teacher name based on your Teacher entity structure
    private String buildTeacherName(Teacher teacher) {
        try {
            // Option 1: If Teacher has firstName and lastName fields
            if (teacher.getFirstName() != null && teacher.getLastName() != null) {
                return teacher.getFirstName() + " " + teacher.getLastName();
            }

            // Option 2: If Teacher has a single name field
            if (teacher.getFirstName() != null) {
                return teacher.getFirstName();
            }

            // Option 3: If Teacher has username or email as fallback
            if (teacher.getEmail() != null) {
                return teacher.getEmail();
            }

            // Fallback to ID if no name fields available
            return "Teacher ID: " + teacher.getId();

        } catch (Exception ex) {
            log.error("Error building teacher name for teacher {}: {}", teacher.getId(), ex.getMessage());
            return "Teacher ID: " + teacher.getId();
        }
    }

    // NEW: Helper method to calculate student count for a class
    private Long calculateStudentCount(Class classEntity) {
        try {
            // Option 1: Use repository query for better performance (recommended)
            Long count = classRepository.getStudentCountByClassId(classEntity.getId());
            return count != null ? count : 0L;

            // Option 2: Use entity relationships (less efficient for large datasets)
            // if (classEntity.getStudents() != null) {
            //     return (long) classEntity.getStudents().size();
            // }
            // return 0L;

        } catch (Exception ex) {
            log.error("Error calculating student count for class {}: {}",
                    classEntity.getId(), ex.getMessage());
            return 0L; // Return 0 if calculation fails
        }
    }


    // Method to get class by ID
    public ClassDTO getClassById(int classId) {
        try {
            Optional<Class> classOpt = classRepository.findActiveById(classId);

            if (classOpt.isPresent()) {
                return mapClassToDTO(classOpt.get());
            }
            log.warn("Class with ID {} not found", classId);
            return null;
        } catch (Exception ex) {
            log.error("Error fetching class by ID {}: ", classId, ex);
            return null;
        }
    }

    // Method to get classes by subject
    public List<ClassDTO> getClassesBySubject(Integer subjectId) {
        try {
            List<Class> classes = classRepository.findBySubjectId(subjectId);
            return classes.stream()
                    .map(this::mapClassToDTO)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Error fetching classes by subject ID {}: ", subjectId, ex);
            return new ArrayList<>();
        }
    }

    // Method to get classes by grade
    public List<ClassDTO> getClassesByGrade(Integer gradeId) {
        try {
            List<Class> classes = classRepository.findByGradeId(gradeId);
            return classes.stream()
                    .map(this::mapClassToDTO)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Error fetching classes by grade ID {}: ", gradeId, ex);
            return new ArrayList<>();
        }
    }

    // Method to soft delete a class
    public String deleteClass(int id) {
        try {
            Optional<Class> classOptional = classRepository.findActiveById(id);
            if (classOptional.isPresent()) {
                classRepository.deleteById(id); // This will trigger the soft delete SQL
                log.info("Class with ID {} soft deleted successfully", id);
                return VarList.RES_SUCCESS;
            } else {
                return VarList.RES_NO_DATE_FOUND;
            }
        } catch (Exception ex) {
            log.error("Error deleting class with ID {}: ", id, ex);
            return VarList.RES_ERROR;
        }
    }

    // Method to search class by name
    public List<ClassDTO> searchClassesByName(String className) {
        try {
            List<Class> classes = classRepository.findByClassNameContainingIgnoreCase(className);
            return classes.stream()
                    .map(this::mapClassToDTO)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Error searching classes by name '{}': ", className, ex);
            return new ArrayList<>();
        }
    }


    /**
     * Method to get classes associated with a specific teacher
     * @param teacherId The ID of the teacher
     * @return List of ClassDTO objects associated with the teacher
     */
    @Transactional(readOnly = true)
    public List<ClassDTO> getClassesByTeacher(Integer teacherId) {
        try {
            log.info("Fetching classes for teacher with ID: {}", teacherId);

            // Validate teacher ID
            if (teacherId == null || teacherId <= 0) {
                log.warn("Invalid teacher ID provided: {}", teacherId);
                return new ArrayList<>();
            }

            // Find classes associated with the teacher
            List<Class> classes = classRepository.findByTeachersId(teacherId);

            if (classes.isEmpty()) {
                log.info("No classes found for teacher with ID: {}", teacherId);
                return new ArrayList<>();
            }

            log.info("Found {} classes for teacher with ID: {}", classes.size(), teacherId);

            // Map entities to DTOs
            List<ClassDTO> classDTOs = classes.stream()
                    .map(this::mapClassToDTO)
                    .collect(Collectors.toList());

            log.info("Successfully mapped {} classes to DTOs for teacher", classDTOs.size());
            return classDTOs;

        } catch (Exception ex) {
            log.error("Error fetching classes for teacher with ID {}: {}", teacherId, ex.getMessage(), ex);
            return new ArrayList<>();
        }
    }


    /**
     * Method to get classes associated with a specific student
     * @param studentId The ID of the teacher
     * @return List of ClassDTO objects associated with the student
     */
    @Transactional(readOnly = true)
    public List<ClassDTO> getClassesByStudent(Integer studentId) {
        try {
            log.info("Fetching classes for student with ID: {}", studentId);

            // Validate teacher ID
            if (studentId == null || studentId <= 0) {
                log.warn("Invalid student ID provided: {}", studentId);
                return new ArrayList<>();
            }

            // Find classes associated with the student
            List<Class> classes = classRepository.findByStudentId(studentId);

            if (classes.isEmpty()) {
                log.info("No classes found for student with ID: {}", studentId);
                return new ArrayList<>();
            }

            log.info("Found {} classes for student with ID: {}", classes.size(), studentId);

            // Map entities to DTOs
            List<ClassDTO> classDTOs = classes.stream()
                    .map(this::mapClassToDTO)
                    .collect(Collectors.toList());

            log.info("Successfully mapped {} classes to DTOs for student", classDTOs.size());
            return classDTOs;

        } catch (Exception ex) {
            log.error("Error fetching classes for student with ID {}: {}", studentId, ex.getMessage(), ex);
            return new ArrayList<>();
        }
    }


    /**
     * Alternative method to get classes by teacher using teacher code
     * @param teacherCode The teacher code
     * @return List of ClassDTO objects associated with the teacher
     */
    @Transactional(readOnly = true)
    public List<ClassDTO> getClassesByTeacherCode(String teacherCode) {
        try {
            log.info("Fetching classes for teacher with code: {}", teacherCode);

            // Validate teacher code
            if (teacherCode == null || teacherCode.trim().isEmpty()) {
                log.warn("Invalid teacher code provided: {}", teacherCode);
                return new ArrayList<>();
            }

            // Find classes associated with the teacher by teacher code
            List<Class> classes = classRepository.findByTeachersTeacherCode(teacherCode);

            if (classes.isEmpty()) {
                log.info("No classes found for teacher with code: {}", teacherCode);
                return new ArrayList<>();
            }

            log.info("Found {} classes for teacher with code: {}", classes.size(), teacherCode);

            // Map entities to DTOs
            List<ClassDTO> classDTOs = classes.stream()
                    .map(this::mapClassToDTO)
                    .collect(Collectors.toList());

            log.info("Successfully mapped {} classes to DTOs for teacher", classDTOs.size());
            return classDTOs;

        } catch (Exception ex) {
            log.error("Error fetching classes for teacher with code {}: {}", teacherCode, ex.getMessage(), ex);
            return new ArrayList<>();
        }
    }

    /**
     * Method to get classes by teacher with additional filtering options
     * @param teacherId The ID of the teacher
     * @param gradeId Optional grade filter (can be null)
     * @param subjectId Optional subject filter (can be null)
     * @return List of ClassDTO objects associated with the teacher with applied filters
     */
    @Transactional(readOnly = true)
    public List<ClassDTO> getClassesByTeacherWithFilters(Integer teacherId, Integer gradeId, Integer subjectId) {
        try {
            log.info("Fetching classes for teacher ID: {} with filters - Grade ID: {}, Subject ID: {}",
                    teacherId, gradeId, subjectId);

            // Validate teacher ID
            if (teacherId == null || teacherId <= 0) {
                log.warn("Invalid teacher ID provided: {}", teacherId);
                return new ArrayList<>();
            }

            // Start with all classes for the teacher
            List<Class> classes = classRepository.findByTeachersId(teacherId);

            // Apply grade filter if provided
            if (gradeId != null && gradeId > 0) {
                classes = classes.stream()
                        .filter(c -> c.getGrade() != null && c.getGrade().getId() == (gradeId))
                        .collect(Collectors.toList());
                log.info("Applied grade filter, {} classes remaining", classes.size());
            }

            // Apply subject filter if provided
            if (subjectId != null && subjectId > 0) {
                classes = classes.stream()
                        .filter(c -> c.getSubject() != null && c.getSubject().getId() == (subjectId))
                        .collect(Collectors.toList());
                log.info("Applied subject filter, {} classes remaining", classes.size());
            }

            if (classes.isEmpty()) {
                log.info("No classes found for teacher with applied filters");
                return new ArrayList<>();
            }

            // Map entities to DTOs
            List<ClassDTO> classDTOs = classes.stream()
                    .map(this::mapClassToDTO)
                    .collect(Collectors.toList());

            log.info("Successfully retrieved {} filtered classes for teacher", classDTOs.size());
            return classDTOs;

        } catch (Exception ex) {
            log.error("Error fetching filtered classes for teacher with ID {}: {}", teacherId, ex.getMessage(), ex);
            return new ArrayList<>();
        }
    }

    /**
     * Get classes with student count for a specific teacher
     * @param teacherId The ID of the teacher
     * @return List of ClassWithStudentCountDTO
     */
    public List<ClassWithStudentCountDTO> getClassesWithStudentCountByTeacher(Integer teacherId) {
        List<Object[]> results = classRepository.findClassesWithStudentCountByTeacherId(teacherId);

        return results.stream()
                .map(result -> new ClassWithStudentCountDTO(
                        (Integer) result[0],    // classId
                        (String) result[1],     // className
                        (String) result[2],     // description
                        (java.time.LocalDate) result[3], // classDate
                        (java.time.LocalTime) result[4], // startTime
                        (String) result[5],     // gradeName
                        (String) result[6],     // subjectName
                        (Long) result[7]        // studentCount
                ))
                .collect(Collectors.toList());
    }

    /**
     * Alternative method: Get classes with student count using entity approach
     * This fetches full entities and calculates student count separately
     * @param teacherId The ID of the teacher
     * @return List of ClassWithStudentCountDTO
     */
    public List<ClassWithStudentCountDTO> getClassesWithStudentCountByTeacherAlternative(Integer teacherId) {
        List<Class> classes = classRepository.findClassesWithDetailsById(teacherId);

        return classes.stream()
                .map(classEntity -> {
                    Long studentCount = classRepository.getStudentCountByClassId(classEntity.getId());
                    return new ClassWithStudentCountDTO(
                            classEntity.getId(),
                            classEntity.getClassName(),
                            classEntity.getDescription(),
                            classEntity.getClassDate(),
                            classEntity.getStartTime(),
                            classEntity.getGrade() != null ? classEntity.getGrade().getGradeName() : null,
                            classEntity.getSubject() != null ? classEntity.getSubject().getSubjectName() : null,
                            studentCount
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Get student count for a specific class
     * @param classId The ID of the class
     * @return Student count
     */
    public Long getStudentCountForClass(Integer classId) {
        return classRepository.getStudentCountByClassId(classId);
    }

    /**
     * Get class count for a specific teacher
     * @param teacherId The ID of the teacher
     * @return Count of classes associated with the teacher
     */
    @Transactional(readOnly = true)
    public Long getClassCountByTeacher(Integer teacherId) {
        try {
            log.info("Fetching class count for teacher with ID: {}", teacherId);

            // Validate teacher ID
            if (teacherId == null || teacherId <= 0) {
                log.warn("Invalid teacher ID provided: {}", teacherId);
                return 0L;
            }

            // Use repository method to get count directly
            Long classCount = classRepository.countByTeachersId(teacherId);

            if (classCount == null) {
                classCount = 0L;
            }

            log.info("Found {} classes for teacher with ID: {}", classCount, teacherId);
            return classCount;

        } catch (Exception ex) {
            log.error("Error fetching class count for teacher with ID {}: {}", teacherId, ex.getMessage(), ex);
            return 0L;
        }
    }


}