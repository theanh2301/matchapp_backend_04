package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.Subject;
import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.exception.BadRequestException;
import com.company.mathapp_backend_04.model.dto.SubjectOverviewDTO;
import com.company.mathapp_backend_04.model.dto.SubjectProgressDTO;
import com.company.mathapp_backend_04.model.enums.Source;
import com.company.mathapp_backend_04.model.request.SubjectRequest;
import com.company.mathapp_backend_04.model.response.SubjectPerformanceResponse;
import com.company.mathapp_backend_04.model.response.SubjectResponse;
import com.company.mathapp_backend_04.model.response.TypePerformanceResponse;
import com.company.mathapp_backend_04.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final LessonCompletionRepository lessonCompletionRepository;

    public List<SubjectOverviewDTO> getSubjectOverviews(Integer userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        Integer gradeId = user.getGrade().getId();

        return subjectRepository.getSubjectOverviewsByGrade(user.getId(), gradeId);
    }

    public List<SubjectResponse> getAllSubjects() {

        List<Subject> subjects = subjectRepository.findAll();

        return subjects.stream()
                .map(s -> new SubjectResponse(
                        s.getId(),
                        s.getSubjectName(),
                        s.getSubjectClass(),
                        s.getIcon()
                ))
                .toList();
    }

    public List<Subject> getAll() {
        return subjectRepository.findAll();
    }

    public List<SubjectProgressDTO> getSubjectProgress(Integer userId) {
        return subjectRepository.getSubjectProgress(userId);
    }

    public void addSubject(SubjectRequest subjectRequest) {
        Optional<Subject> existingSubject = subjectRepository.
                findBySubjectNameIgnoreCaseAndSubjectClass(
                        subjectRequest.getSubjectName(),
                        subjectRequest.getSubjectClass()
                );

        if (existingSubject.isPresent()) {
            throw new BadRequestException("Subject in this class already exist");
        }

        Subject subject = Subject.builder()
                .subjectName(subjectRequest.getSubjectName())
                .subjectClass(subjectRequest.getSubjectClass())
                .icon(subjectRequest.getIcon())
                .build();

        subjectRepository.save(subject);
    }

    public List<SubjectPerformanceResponse> getSubjectPerformance(Integer userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new BadRequestException("User not found"));

        Integer gradeId = user.getGrade().getId();

        List<Object[]> data = sessionRepository.getSubjectPerformance(userId, gradeId);

        List<SubjectPerformanceResponse> result = new ArrayList<>();

        for (Object[] row : data) {

            String subject = (String) row[0];
            int earnedXp = ((Number) row[1]).intValue();
            int maxXp = ((Number) row[2]).intValue();

            int accuracy = maxXp > 0 ? (earnedXp * 100 / maxXp) : 0;

            String level = getLevel(accuracy);

            result.add(new SubjectPerformanceResponse(
                    subject,
                    accuracy,
                    0, // TODO: tính tuần sau
                    level
            ));
        }

        return result;
    }

    private String getLevel(int acc) {
        if (acc >= 75) return "Khá";
        if (acc >= 50) return "Trung bình";
        return "Yếu";
    }

    public List<TypePerformanceResponse> getTypePerformance(Integer userId) {

        // ===== 1. Lấy best XP =====
        Object[] bestXpData = lessonCompletionRepository.getTotalBestXp(userId);

        int bestFlash = bestXpData[0] != null ? ((Number) bestXpData[0]).intValue() : 0;
        int bestMatch = bestXpData[1] != null ? ((Number) bestXpData[1]).intValue() : 0;
        int bestQuiz = bestXpData[2] != null ? ((Number) bestXpData[2]).intValue() : 0;

        // ===== 2. Lấy max XP =====
        Object[] maxXpData = lessonCompletionRepository.getTotalMaxXp();

        int maxFlash = ((Number) maxXpData[0]).intValue();
        int maxMatch = ((Number) maxXpData[1]).intValue();
        int maxQuiz = ((Number) maxXpData[2]).intValue();

        // ===== 3. Tính accuracy =====
        List<TypePerformanceResponse> result = new ArrayList<>();

        result.add(new TypePerformanceResponse(
                Source.FLASHCARD_GAME.name(),
                maxFlash > 0 ? bestFlash * 100 / maxFlash : 0
        ));

        result.add(new TypePerformanceResponse(
                Source.MATCH_CARD_GAME.name(),
                maxMatch > 0 ? bestMatch * 100 / maxMatch : 0
        ));

        result.add(new TypePerformanceResponse(
                Source.QUIZ_GAME.name(),
                maxQuiz > 0 ? bestQuiz * 100 / maxQuiz : 0
        ));

        return result;
    }

    public void updateSubject(Integer id, SubjectRequest subjectRequest) {

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Subject not found"));

        Optional<Subject> existingSubject =
                subjectRepository.findBySubjectNameIgnoreCaseAndSubjectClass(
                        subjectRequest.getSubjectName(),
                        subjectRequest.getSubjectClass()
                );

        if (existingSubject.isPresent() && !existingSubject.get().getId().equals(id)) {
            throw new BadRequestException("Subject already exists");
        }

        subject.setSubjectName(subjectRequest.getSubjectName());
        subject.setSubjectClass(subjectRequest.getSubjectClass());
        subject.setIcon(subjectRequest.getIcon());

        subjectRepository.save(subject);
    }

    public void deleteSubject(Integer id) {

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Subject not found"));

        if (chapterRepository.existsBySubjectId(id)) {
            throw new BadRequestException("Cannot delete subject because it contains chapter");
        }

        subjectRepository.delete(subject);
    }
}
