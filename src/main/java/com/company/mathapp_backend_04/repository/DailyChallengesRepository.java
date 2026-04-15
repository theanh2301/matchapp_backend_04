package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.DailyChallenges;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyChallengesRepository extends JpaRepository<DailyChallenges, Integer> {

    List<DailyChallenges> findByDate(LocalDate date);
}