package com.company.mathapp_backend_04.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "lesson_completion")
public class LessonCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;


    @ManyToOne
    @JoinColumn(name = "lesson_id")
    Lesson lesson;

    Integer totalXp;

    Integer bestFlashcardXp;
    Integer bestQuizXp;
    Integer bestMatchXp;

    Boolean isCompleted;

    LocalDateTime completedAt;
    LocalDateTime updatedAt;

}
