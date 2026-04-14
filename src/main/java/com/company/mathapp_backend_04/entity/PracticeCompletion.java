package com.company.mathapp_backend_04.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "practice_completion")
public class PracticeCompletion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    Integer userId;

    @ManyToOne
    @JoinColumn(name = "practice_id")
    Practice practice;

    Boolean isCompleted;
    Integer totalXp;
}
