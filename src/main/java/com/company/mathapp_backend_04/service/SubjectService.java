package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.Grade;
import com.company.mathapp_backend_04.entity.Subject;
import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.exception.BadRequestException;
import com.company.mathapp_backend_04.exception.NotFoundException;
import com.company.mathapp_backend_04.model.dto.SubjectOverviewDTO;
import com.company.mathapp_backend_04.model.dto.SubjectPerformanceProjection;
import com.company.mathapp_backend_04.model.dto.SubjectProgressDTO;
import com.company.mathapp_backend_04.model.dto.TypeXpSummaryProjection;
import com.company.mathapp_backend_04.model.enums.Source;
import com.company.mathapp_backend_04.model.request.SubjectRequest;
import com.company.mathapp_backend_04.model.response.SubjectPerformanceResponse;
import com.company.mathapp_backend_04.model.response.SubjectResponse;
import com.company.mathapp_backend_04.model.response.TypePerformanceResponse;
import com.company.mathapp_backend_04.repository.ChapterRepository;
import com.company.mathapp_backend_04.repository.GradeRepository;
import com.company.mathapp_backend_04.repository.LessonCompletionRepository;
import com.company.mathapp_backend_04.repository.SessionRepository;
import com.company.mathapp_backend_04.repository.SubjectRepository;
import com.company.mathapp_backend_04.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final GradeRepository gradeRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final LessonCompletionRepository lessonCompletionRepository;

    public List<SubjectOverviewDTO> getSubjectOverviews(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return subjectRepository.getSubjectOverviewsByGrade(user.getId(), user.getGrade().getId());
    }

    public List<SubjectResponse> getAllSubjects() {
        return subjectRepository.findAll().stream()
                .map(subject -> new SubjectResponse(
                        subject.getId(),
                        subject.getSubjectName(),
                        subject.getIcon(),
                        subject.getGrade() != null ? subject.getGrade().getId() : null,
                        subject.getGrade() != null ? subject.getGrade().getGradeName() : null
                ))
                .toList();
    }

    public List<Subject> getAll() {
        return subjectRepository.findAll();
    }

    public List<SubjectProgressDTO> getSubjectProgress(Integer userId) {
        return subjectRepository.getSubjectProgress(userId);
    }

    @Transactional
    public void addSubject(SubjectRequest subjectRequest) {
        Grade grade = gradeRepository.findById(subjectRequest.getGradeId())
                .orElseThrow(() -> new NotFoundException("Grade not found"));

        Optional<Subject> existingSubject = subjectRepository.findBySubjectNameIgnoreCaseAndGrade_Id(
                subjectRequest.getSubjectName(),
                subjectRequest.getGradeId()
        );

        if (existingSubject.isPresent()) {
            throw new BadRequestException("Subject already exists in this grade");
        }

        Subject subject = Subject.builder()
                .subjectName(subjectRequest.getSubjectName())
                .icon(subjectRequest.getIcon())
                .grade(grade)
                .build();

        subjectRepository.save(subject);
    }

    public List<SubjectPerformanceResponse> getSubjectPerformance(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<SubjectPerformanceProjection> data = sessionRepository.getSubjectPerformance(userId, user.getGrade().getId());
        List<SubjectPerformanceResponse> result = new ArrayList<>();

        for (SubjectPerformanceProjection row : data) {
            int earnedXp = toInt(row.getEarnedXp());
            int maxXp = toInt(row.getMaxXp());
            int accuracy = maxXp > 0 ? earnedXp * 100 / maxXp : 0;

            result.add(new SubjectPerformanceResponse(
                    row.getSubjectName(),
                    accuracy,
                    0,
                    getLevel(accuracy)
            ));
        }

        return result;
    }

    public List<TypePerformanceResponse> getTypePerformance(Integer userId) {
        TypeXpSummaryProjection summary = lessonCompletionRepository.getTypeXpSummary(userId);

        List<TypePerformanceResponse> result = new ArrayList<>();
        result.add(new TypePerformanceResponse(
                Source.FLASHCARD_GAME.name(),
                calculateAccuracy(toInt(summary.getEarnedFlashcardXp()), toInt(summary.getMaxFlashcardXp()))
        ));
        result.add(new TypePerformanceResponse(
                Source.MATCH_CARD_GAME.name(),
                calculateAccuracy(toInt(summary.getEarnedMatchXp()), toInt(summary.getMaxMatchXp()))
        ));
        result.add(new TypePerformanceResponse(
                Source.QUIZ_GAME.name(),
                calculateAccuracy(toInt(summary.getEarnedQuizXp()), toInt(summary.getMaxQuizXp()))
        ));

        return result;
    }

    @Transactional
    public void updateSubject(Integer id, SubjectRequest subjectRequest) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subject not found"));

        Grade grade = gradeRepository.findById(subjectRequest.getGradeId())
                .orElseThrow(() -> new NotFoundException("Grade not found"));

        Optional<Subject> existingSubject = subjectRepository.findBySubjectNameIgnoreCaseAndGrade_Id(
                subjectRequest.getSubjectName(),
                subjectRequest.getGradeId()
        );

        if (existingSubject.isPresent() && !existingSubject.get().getId().equals(id)) {
            throw new BadRequestException("Subject already exists");
        }

        subject.setSubjectName(subjectRequest.getSubjectName());
        subject.setIcon(subjectRequest.getIcon());
        subject.setGrade(grade);
        subjectRepository.save(subject);
    }

    @Transactional
    public void deleteSubject(Integer id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subject not found"));

        if (chapterRepository.existsBySubjectId(id)) {
            throw new BadRequestException("Cannot delete subject because it contains chapter");
        }

        subjectRepository.delete(subject);
    }

    private String getLevel(int accuracy) {
        if (accuracy >= 75) {
            return "Strong";
        }
        if (accuracy >= 50) {
            return "Average";
        }
        return "Needs Improvement";
    }

    private int calculateAccuracy(int earnedXp, int maxXp) {
        return maxXp > 0 ? earnedXp * 100 / maxXp : 0;
    }

    private int toInt(Long value) {
        return value != null ? value.intValue() : 0;
    }
}
