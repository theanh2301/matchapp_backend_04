package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.UserDailyChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserDailyChallengeRepository extends JpaRepository<UserDailyChallenge, Integer> {

    @Query("""
    SELECT udc
    FROM UserDailyChallenge udc
    WHERE udc.user.id = :userId
    AND udc.dailyChallenges.date = :date
    """)
    List<UserDailyChallenge> findByUserAndDate(Integer userId, LocalDate date);

    Optional<UserDailyChallenge> findByUserIdAndDailyChallengesId(Integer userId, Integer challengeId);
}