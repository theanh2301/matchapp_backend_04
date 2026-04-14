package com.company.mathapp_backend_04.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "chapters")
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String chapterName;
    String description;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    Subject subject;

}
