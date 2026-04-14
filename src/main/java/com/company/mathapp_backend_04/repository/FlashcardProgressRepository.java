package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.Flashcard;
import com.company.mathapp_backend_04.entity.FlashcardProgress;
import com.company.mathapp_backend_04.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardProgressRepository extends JpaRepository<FlashcardProgress, Integer> {

    Optional<FlashcardProgress> findByFlashcardIdAndUserId(Integer flashcardId, Integer userId);

    Optional<FlashcardProgress> findByFlashcardAndUser(Flashcard flashcard, User user);

    Optional<FlashcardProgress> findByUserIdAndFlashcardId(Integer id, Integer id1);

    List<FlashcardProgress> findByUserIdAndFlashcardIdIn(Integer userId, List<Integer> flashcardIds);

    boolean existsByUserIdAndFlashcardIdAndIsKnownTrue(Integer id, Integer id1);

    @Query("""
    SELECT fp FROM FlashcardProgress fp
    WHERE fp.user.id = :userId
    AND fp.flashcard.id IN :flashcardIds
""")
    List<FlashcardProgress> findByUserIdAndFlashcardIds(
            Integer userId,
            List<Integer> flashcardIds
    );
}
