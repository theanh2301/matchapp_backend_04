package com.company.mathapp_backend_04.service.admin;

import com.company.mathapp_backend_04.entity.Grade;
import com.company.mathapp_backend_04.entity.Subject;
import com.company.mathapp_backend_04.repository.ChapterRepository;
import com.company.mathapp_backend_04.repository.GradeRepository;
import com.company.mathapp_backend_04.repository.SubjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSubjectService {

    private final SubjectRepository subjectRepository;
    private final GradeRepository gradeRepository;
    private final ChapterRepository chapterRepository;

    public Page<Subject> getAll(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return subjectRepository.findAll(pageable);
        }
        return subjectRepository.findBySubjectNameContainingIgnoreCase(keyword.trim(), pageable);
    }

    public Subject getById(int id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subject not found with id = " + id));
    }

    @Transactional
    public void save(Subject subject, Integer gradeId) {
        if (subject.getSubjectName() == null || subject.getSubjectName().trim().isEmpty()) {
            throw new RuntimeException("Subject name cannot be blank");
        }

        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new RuntimeException("Grade not found"));

        boolean duplicate = subjectRepository
                .findBySubjectNameIgnoreCaseAndGrade_Id(subject.getSubjectName().trim(), gradeId)
                .filter(item -> subject.getId() == null || !item.getId().equals(subject.getId()))
                .isPresent();

        if (duplicate) {
            throw new RuntimeException("Subject already exists in this grade");
        }

        Subject target = subject.getId() == null ? new Subject() : getById(subject.getId());
        target.setSubjectName(subject.getSubjectName().trim());
        target.setIcon(subject.getIcon() == null ? null : subject.getIcon().trim());
        target.setGrade(grade);

        subjectRepository.save(target);
    }

    @Transactional
    public void delete(int id) {
        if (!subjectRepository.existsById(id)) {
            throw new RuntimeException("Subject not found");
        }
        if (chapterRepository.existsBySubjectId(id)) {
            throw new RuntimeException("Cannot delete subject because it contains chapters");
        }
        subjectRepository.deleteById(id);
    }

    @Transactional
    public void deleteBulk(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        subjectRepository.deleteAllById(ids);
    }
}
