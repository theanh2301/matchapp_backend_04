package com.company.mathapp_backend_04.entity;

import com.company.mathapp_backend_04.model.enums.Source;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "session")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    Integer totalXp;

    @Enumerated(EnumType.STRING)
    Source source;

    LocalDateTime startedAt;
    LocalDateTime completedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "lesson_id")
    Lesson lesson;
}
