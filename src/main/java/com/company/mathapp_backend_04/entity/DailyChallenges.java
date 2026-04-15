package com.company.mathapp_backend_04.entity;

import com.company.mathapp_backend_04.model.enums.Source;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "daily_challenges")
public class DailyChallenges {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String title;
    String description;
    Integer xpReward;
    LocalDate date;
    @Enumerated(EnumType.STRING)
    Source source;
    Integer TargetValue;
}
