package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.PracticeCompletion;
import com.company.mathapp_backend_04.model.enums.PracticeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPracticeRepository extends JpaRepository<PracticeCompletion, Integer> {
    @Query("""
    SELECT COUNT(up)
    FROM PracticeCompletion up
    WHERE up.practice.practiceType = :practiceType
      AND up.practice.grade.id = :gradeId
      AND up.isCompleted = true
      AND up.userId = :userId
""")
    Integer countCompletedByPracticeTypeAndUserId(
            @Param("practiceType") PracticeType practiceType,
            @Param("userId") Integer userId,
            @Param("gradeId") Integer gradeId
    );

    Optional<PracticeCompletion> findByUserIdAndPracticeId(Integer userId, Integer practiceId);
}