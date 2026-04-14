package com.company.mathapp_backend_04.entity;

import com.company.mathapp_backend_04.model.enums.Difficulty;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "practice_questions")
public class PracticeQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String content;
    Integer xpReward;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    Difficulty difficulty;

    @ManyToOne
    @JoinColumn(name = "practice_id")
    Practice practice;

}
