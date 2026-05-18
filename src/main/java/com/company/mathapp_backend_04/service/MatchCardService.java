package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.Lesson;
import com.company.mathapp_backend_04.entity.MatchCard;
import com.company.mathapp_backend_04.exception.BadRequestException;
import com.company.mathapp_backend_04.exception.ConflictException;
import com.company.mathapp_backend_04.exception.NotFoundException;
import com.company.mathapp_backend_04.model.request.MatchCardPairRequest;
import com.company.mathapp_backend_04.model.response.MatchCardPairResponse;
import com.company.mathapp_backend_04.model.response.MatchCardResponse;
import com.company.mathapp_backend_04.repository.LessonRepository;
import com.company.mathapp_backend_04.repository.MatchCardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchCardService {

    private final MatchCardRepository matchCardRepository;
    private final LessonRepository lessonRepository;

    public List<MatchCardResponse> getMatchCard(Integer lessonId) {
        List<MatchCard> cards = matchCardRepository.findByLessonId(lessonId);

        return cards.stream()
                .map(matchCard -> new MatchCardResponse(
                        matchCard.getId(),
                        matchCard.getPairId(),
                        matchCard.getContent(),
                        matchCard.getXpReward()
                ))
                .toList();
    }

    public Page<MatchCardPairResponse> getAllCardPairs(String keyword, Pageable pageable) {
        List<MatchCard> cards = matchCardRepository.findAll();

        Map<String, List<MatchCard>> grouped = cards.stream()
                .filter(card -> card.getLesson() != null)
                .filter(card -> card.getPairId() != null)
                .filter(card -> card.getContent() != null)
                .sorted(Comparator
                        .comparing((MatchCard c) -> c.getLesson().getId())
                        .thenComparing(MatchCard::getPairId)
                        .thenComparing(MatchCard::getId))
                .collect(Collectors.groupingBy(
                        card -> card.getLesson().getId() + "_" + card.getPairId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();

        List<MatchCardPairResponse> allPairs = new ArrayList<>();

        for (List<MatchCard> pairCards : grouped.values()) {
            if (pairCards.size() != 2) {
                continue;
            }

            MatchCard c1 = pairCards.get(0);
            MatchCard c2 = pairCards.get(1);

            boolean matchKeyword = normalizedKeyword.isBlank()
                    || c1.getContent().toLowerCase().contains(normalizedKeyword)
                    || c2.getContent().toLowerCase().contains(normalizedKeyword)
                    || c1.getLesson().getLessonName().toLowerCase().contains(normalizedKeyword);

            if (!matchKeyword) {
                continue;
            }

            allPairs.add(new MatchCardPairResponse(
                    c1.getPairId(),
                    c1.getContent(),
                    c2.getContent(),
                    c1.getXpReward(),
                    c1.getLesson().getId(),
                    c1.getLesson().getLessonName()
            ));
        }

        if (pageable.isUnpaged()) {
            return new PageImpl<>(allPairs);
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allPairs.size());

        List<MatchCardPairResponse> pageContent =
                start >= allPairs.size() ? List.of() : allPairs.subList(start, end);

        return new PageImpl<>(pageContent, pageable, allPairs.size());
    }

    public List<MatchCardPairResponse> getAllCardPairsForExport() {
        return getAllCardPairs(null, Pageable.unpaged()).getContent();
    }

    public Integer generatePairId(Integer lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Lesson not found"));

        int pairId;
        do {
            pairId = ThreadLocalRandom.current().nextInt(10000, 100000);
        } while (matchCardRepository.existsByPairIdAndLesson(pairId, lesson));

        return pairId;
    }

    @Transactional
    public void addMatchCardPair(MatchCardPairRequest request) {
        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new BadRequestException("Lesson not found"));

        String content1 = normalizeContent(request.getContent1());
        String content2 = normalizeContent(request.getContent2());

        validatePairContent(content1, content2);

        if (request.getPairId() == null) {
            request.setPairId(generatePairId(request.getLessonId()));
        }

        List<MatchCard> existingCards = matchCardRepository.findByPairIdAndLesson(request.getPairId(), lesson);
        if (!existingCards.isEmpty()) {
            throw new ConflictException("Pair ID already exists in this lesson");
        }

        boolean content1Exists = matchCardRepository.existsByContentAndLesson(content1, lesson);
        boolean content2Exists = matchCardRepository.existsByContentAndLesson(content2, lesson);

        if (content1Exists || content2Exists) {
            throw new ConflictException("Content already exists in this lesson");
        }

        MatchCard card1 = MatchCard.builder()
                .pairId(request.getPairId())
                .content(content1)
                .xpReward(request.getXpReward())
                .lesson(lesson)
                .build();

        MatchCard card2 = MatchCard.builder()
                .pairId(request.getPairId())
                .content(content2)
                .xpReward(request.getXpReward())
                .lesson(lesson)
                .build();

        matchCardRepository.saveAll(List.of(card1, card2));
    }

    @Transactional
    public void updateMatchCardPair(MatchCardPairRequest request) {
        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new NotFoundException("Lesson not found"));

        if (request.getPairId() == null) {
            throw new BadRequestException("Pair ID is required for update");
        }

        List<MatchCard> cards = matchCardRepository.findByPairIdAndLesson(request.getPairId(), lesson)
                .stream()
                .sorted(Comparator.comparing(MatchCard::getId))
                .toList();

        if (cards.size() != 2) {
            throw new BadRequestException("Pair must have exactly 2 cards to update");
        }

        String content1 = normalizeContent(request.getContent1());
        String content2 = normalizeContent(request.getContent2());

        validatePairContent(content1, content2);

        boolean content1Exists = matchCardRepository
                .existsByContentAndLessonAndPairIdNot(content1, lesson, request.getPairId());

        boolean content2Exists = matchCardRepository
                .existsByContentAndLessonAndPairIdNot(content2, lesson, request.getPairId());

        if (content1Exists || content2Exists) {
            throw new ConflictException("Content already exists in this lesson");
        }

        List<String> oldContents = cards.stream()
                .map(MatchCard::getContent)
                .map(this::normalizeContent)
                .toList();

        boolean sameContents = oldContents.contains(content1) && oldContents.contains(content2);
        boolean sameXp = cards.stream().allMatch(card -> card.getXpReward().equals(request.getXpReward()));

        if (sameContents && sameXp) {
            throw new BadRequestException("No changes detected");
        }

        cards.get(0).setContent(content1);
        cards.get(1).setContent(content2);
        cards.get(0).setXpReward(request.getXpReward());
        cards.get(1).setXpReward(request.getXpReward());

        matchCardRepository.saveAll(cards);
    }

    @Transactional
    public void deleteMatchCardPair(Integer pairId, Integer lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Lesson not found"));

        List<MatchCard> cards = matchCardRepository.findByPairIdAndLesson(pairId, lesson);

        if (cards.isEmpty()) {
            throw new NotFoundException("Match card pair not found");
        }

        matchCardRepository.deleteAll(cards);
    }

    @Transactional
    public void deleteMultiple(List<Integer> pairIds, Integer lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Lesson not found"));

        if (pairIds == null || pairIds.isEmpty()) {
            return;
        }

        for (Integer pairId : pairIds) {
            List<MatchCard> cards = matchCardRepository.findByPairIdAndLesson(pairId, lesson);
            if (!cards.isEmpty()) {
                matchCardRepository.deleteAll(cards);
            }
        }
    }

    @Transactional
    public void deleteMultipleByTokens(List<String> selections) {
        if (selections == null || selections.isEmpty()) {
            return;
        }

        for (String token : selections) {
            if (token == null || !token.contains("-")) {
                continue;
            }
            String[] parts = token.split("-", 2);
            Integer pairId = Integer.valueOf(parts[0]);
            Integer lessonId = Integer.valueOf(parts[1]);
            deleteMatchCardPair(pairId, lessonId);
        }
    }

    private void validatePairContent(String content1, String content2) {
        if (content1.isBlank() || content2.isBlank()) {
            throw new BadRequestException("Content cannot be blank");
        }

        if (content1.equalsIgnoreCase(content2)) {
            throw new BadRequestException("Two cards in a pair must be different");
        }
    }

    private String normalizeContent(String content) {
        return content == null ? "" : content.trim();
    }
}
