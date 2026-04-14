package com.company.mathapp_backend_04.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "practice_progress")
public class PracticeProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    Boolean isCorrect;
    LocalDateTime answeredAt;
    Integer totalXP;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "practice_question_id")
    PracticeQuestion practiceQuestion;

    @ManyToOne
    @JoinColumn(name = "practice_answer_id")
    PracticeAnswer practiceAnswer;
}
