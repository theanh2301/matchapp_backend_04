package com.company.mathapp_backend_04.entity;

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
@Table(name = "user_stats")
public class UserStat {
    @Id
    Integer userId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "user_id")
    User user;

    Integer totalXP;
    Integer totalLesson;
    Integer totalPractice;
    Integer streakDay;
    Integer totalStudyDay;
    LocalDateTime lastStudyDate;
}
