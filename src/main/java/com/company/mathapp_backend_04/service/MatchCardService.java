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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchCardService {
    private final MatchCardRepository matchCardRepository;
    private final LessonRepository lessonRepository;

    public List<MatchCardResponse> getMatchCard(Integer id) {
        List<MatchCard> getMatchCard = matchCardRepository.findByLessonId(id);

        return getMatchCard.stream().map(matchCard -> new MatchCardResponse(
                matchCard.getId(),
                matchCard.getPairId(),
                matchCard.getContent(),
                matchCard.getXpReward()
        )).toList();
    }

    public List<MatchCardPairResponse> getMatchCardPair(Integer lessonId) {

        List<MatchCard> cards = matchCardRepository.findByLessonId(lessonId);

        return cards.stream()
                .collect(Collectors.groupingBy(MatchCard::getPairId))
                .entrySet()
                .stream()
                .map(entry -> {
                    List<MatchCard> pairCards = entry.getValue();

                    MatchCard c1 = pairCards.get(0);
                    MatchCard c2 = pairCards.size() > 1 ? pairCards.get(1) : null;

                    return new MatchCardPairResponse(
                            entry.getKey(),
                            c1.getContent(),
                            c2 != null ? c2.getContent() : null,
                            c1.getXpReward(),
                            lessonId,
                            c1.getLesson().getLessonName()
                    );
                })
                .toList();
    }

    @Transactional
    public void addMatchCardPair(MatchCardPairRequest request) {

        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new BadRequestException("Lesson not found"));

        // 1. Kiểm tra đã tồn tại pairId chưa
        List<MatchCard> existingCards = matchCardRepository
                .findByPairIdAndLesson(request.getPairId(), lesson);

        if (existingCards.size() >= 2) {
            throw new BadRequestException("This pairId already has 2 cards");
        }

        // 2. Không cho content trùng nhau
        if (request.getContent1().equalsIgnoreCase(request.getContent2())) {
            throw new BadRequestException("Two cards in a pair must be different");
        }

        // 3. Không cho trùng content trong lesson
        boolean content1Exists = matchCardRepository
                .existsByContentAndLesson(request.getContent1(), lesson);

        boolean content2Exists = matchCardRepository
                .existsByContentAndLesson(request.getContent2(), lesson);

        if (content1Exists || content2Exists) {
            throw new BadRequestException("Content already exists in this lesson");
        }

        // 4. Tạo 2 thẻ
        MatchCard card1 = MatchCard.builder()
                .pairId(request.getPairId())
                .content(request.getContent1())
                .xpReward(request.getXpReward())
                .lesson(lesson)
                .build();

        MatchCard card2 = MatchCard.builder()
                .pairId(request.getPairId())
                .content(request.getContent2())
                .xpReward(request.getXpReward())
                .lesson(lesson)
                .build();

        matchCardRepository.saveAll(List.of(card1, card2));
    }

    @Transactional
    public void updateMatchCardPair(MatchCardPairRequest request) {

        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new NotFoundException("Lesson not found"));

        List<MatchCard> cards = matchCardRepository
                .findByPairIdAndLesson(request.getPairId(), lesson);

        if (cards.size() != 2) {
            throw new BadRequestException("Pair must have exactly 2 cards to update");
        }

        String content1 = request.getContent1().trim();
        String content2 = request.getContent2().trim();

        if (content1.equalsIgnoreCase(content2)) {
            throw new BadRequestException("Two cards must have different content");
        }

        boolean content1Exists = matchCardRepository
                .existsByContentAndLessonAndPairIdNot(content1, lesson, request.getPairId());

        boolean content2Exists = matchCardRepository
                .existsByContentAndLessonAndPairIdNot(content2, lesson, request.getPairId());

        if (content1Exists || content2Exists) {
            throw new ConflictException("Content already exists in this lesson");
        }

        List<String> oldContents = cards.stream()
                .map(MatchCard::getContent)
                .map(String::trim)
                .toList();

        boolean isSame =
                oldContents.contains(content1) &&
                        oldContents.contains(content2);

        if (isSame) {
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

        List<MatchCard> cards = matchCardRepository
                .findByPairIdAndLesson(pairId, lesson);

        if (cards.size() != 2) {
            throw new BadRequestException("Pair must have exactly 2 cards");
        }

        matchCardRepository.deleteAll(cards);
    }

    public Page<MatchCardPairResponse> getAllCards(String keyword, Pageable pageable) {

        Page<MatchCard> page;

        if (keyword == null || keyword.isBlank()) {
            page = matchCardRepository.findAll(pageable);
        } else {
            page = matchCardRepository
                    .findByContentContainingIgnoreCase(keyword, pageable);
        }

        Map<Integer, List<MatchCard>> grouped = page.getContent().stream()
                .collect(Collectors.groupingBy(MatchCard::getPairId));

        List<MatchCardPairResponse> result = grouped.values().stream()
                .filter(list -> list.size() == 2)
                .map(list -> {
                    MatchCard c1 = list.get(0);
                    MatchCard c2 = list.get(1);

                    return new MatchCardPairResponse(
                            c1.getPairId(),
                            c1.getContent(),
                            c2.getContent(),
                            c1.getXpReward(),
                            c1.getLesson().getId(),
                            c1.getLesson().getLessonName()
                    );
                })
                .toList();

        return new PageImpl<>(result, pageable, result.size());
    }

    @Transactional
    public void deleteMultiple(List<Integer> pairIds, Integer lessonId) {

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Lesson not found"));

        for (Integer pairId : pairIds) {

            List<MatchCard> cards =
                    matchCardRepository.findByPairIdAndLesson(pairId, lesson);

            if (cards.size() == 2) {
                matchCardRepository.deleteAll(cards);
            }
        }
    }

}