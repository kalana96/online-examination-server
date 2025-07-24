package com.examination.online_examination_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "student") // Specifies the name of the table in the database
@SQLDelete(sql = "UPDATE student SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Student {
    @Id // Primary key annotation for auto-incremented ID
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generated primary key
    private int id; // Auto-incrementing primary key

    @Column(name = "registration_number", nullable = false, unique = true)
    private String registrationNumber; // Unique registration number (not the primary key)

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "nic", nullable = false, unique = true)
    private String nic;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "contact_no", nullable = false)
    private String contactNo;

    @Column(name = "address")
    private String address;

    @Column(name = "age")
    private Integer age;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "gender")
    private String gender;

    @Lob
    @Column(name = "profile_photo", columnDefinition = "LONGBLOB")
    private byte[] profilePhoto;

    @Column(name = "grade_id",nullable = false)
    private int gradeId;
    
    
    //Relationships

//    // Add ManyToOne relationship with Grade
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "grade_id")
//    @ToString.Exclude
//    @EqualsAndHashCode.Exclude
//    private Grade grade;

    // Many-to-Many relationship with Classes
    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "student_class",
            joinColumns = @JoinColumn(name = "student_id", referencedColumnName = "id"), // Reference to auto-incremented ID
            inverseJoinColumns = @JoinColumn(name = "class_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Class> classes = new ArrayList<>();

    // Bidirectional relationship with User
    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExamResult> examResults = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExamRegistration> examRegistrations = new ArrayList<>();



    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // This field will store the deletion timestamp if soft deleted

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false; // Soft delete flag

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
