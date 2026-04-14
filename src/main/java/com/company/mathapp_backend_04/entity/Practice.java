package com.company.mathapp_backend_04.entity;

import com.company.mathapp_backend_04.model.enums.PracticeType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "practice")
public class Practice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String title;
    String description;
    Integer timeLimit;

    @Enumerated(EnumType.STRING)
    @Column(name = "practice_type")
    PracticeType practiceType;

    @ManyToOne
    @JoinColumn(name = "grade_id")
    Grade grade;
}
