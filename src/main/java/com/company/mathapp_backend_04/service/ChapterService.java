package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.Chapter;
import com.company.mathapp_backend_04.entity.Subject;
import com.company.mathapp_backend_04.exception.BadRequestException;
import com.company.mathapp_backend_04.exception.ConflictException;
import com.company.mathapp_backend_04.exception.NotFoundException;
import com.company.mathapp_backend_04.model.dto.ChapterOverviewDTO;
import com.company.mathapp_backend_04.model.request.ChapterRequest;
import com.company.mathapp_backend_04.model.response.ChapterResponse;
import com.company.mathapp_backend_04.repository.ChapterRepository;
import com.company.mathapp_backend_04.repository.LessonRepository;
import com.company.mathapp_backend_04.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChapterService {
    private final ChapterRepository chapterRepository;
    private final SubjectRepository subjectRepository;
    private final LessonRepository lessonRepository;

    public List<ChapterResponse> getChaptersBySubjectId(Integer id) {
        List<Chapter> chapters = chapterRepository.findChapterBySubjectId(id);

        return chapters.stream().map(c-> new ChapterResponse(
            c.getId(),
            c.getChapterName(),
            c.getDescription()
        )).toList();
    }

    public void addChapter(ChapterRequest chapterRequest) {

        Subject subject = subjectRepository.findById(chapterRequest.getSubjectId())
                .orElseThrow(() -> new BadRequestException("Subject not found"));

        Optional<Chapter> existingChapter = chapterRepository.findByChapterNameAndSubject(
                        chapterRequest.getChapterName(),
                        subject
                );

        if (existingChapter.isPresent()) {
            throw new BadRequestException("Chapter already exists in this subject");
        }

        Chapter chapter = Chapter.builder()
                .chapterName(chapterRequest.getChapterName())
                .description(chapterRequest.getDescription())
                .subject(subject)
                .build();

        chapterRepository.save(chapter);
    }

    public void updateChapter(Integer id, ChapterRequest chapterRequest) {

        Subject subject = subjectRepository.findById(chapterRequest.getSubjectId())
                .orElseThrow(() -> new BadRequestException("Subject not found"));

        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Chapter not found"));

        if (chapterRepository.existsByChapterNameAndSubjectAndIdNot(
                chapterRequest.getChapterName(), subject, id)) {
            throw new BadRequestException("Chapter already exists in this subject");
        }

        if (chapter.getChapterName().equals(chapterRequest.getChapterName())
                && Objects.equals(chapter.getDescription(), chapterRequest.getDescription())
                && chapter.getSubject().getId().equals(subject.getId())) {
            throw new BadRequestException("No changes detected");
        }

        chapter.setChapterName(chapterRequest.getChapterName());
        chapter.setDescription(chapterRequest.getDescription());
        chapter.setSubject(subject);

        chapterRepository.save(chapter);
    }

    public void deleteChapter(Integer id) {

        if (!chapterRepository.existsById(id)) {
            throw new NotFoundException("Chapter not found");
        }

        if (lessonRepository.existsByChapterId(id)) {
            throw new ConflictException("Cannot delete chapter because it contains lessons");
        }

        try {
            chapterRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Cannot delete chapter due to data constraints");
        }
    }

    public List<ChapterOverviewDTO> getChaptersBySubject(Integer subjectId, Integer userId) {
        return chapterRepository.getChapterOverviewsBySubject(subjectId, userId);
    }
}
