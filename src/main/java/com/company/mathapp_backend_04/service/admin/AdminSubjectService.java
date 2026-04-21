package com.company.mathapp_backend_04.service.admin;

import com.company.mathapp_backend_04.entity.Grade;
import com.company.mathapp_backend_04.entity.Subject;
import com.company.mathapp_backend_04.repository.GradeRepository;
import com.company.mathapp_backend_04.repository.SubjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminSubjectService {
    private final SubjectRepository subjectRepository;
    private final GradeRepository gradeRepository;

    public Page<Subject> getAll(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return subjectRepository.findAll(pageable);
        }
        return subjectRepository.findBySubjectNameContainingIgnoreCase(keyword.trim(), pageable);
    }

    public Subject getById(int id) {
        return subjectRepository.findById(id).orElseThrow(() -> new RuntimeException("Subject not found"));
    }

    @Transactional
    public void save(Subject subject, Integer gradeId) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new RuntimeException("Grade not found"));
        subject.setGrade(grade);
        subjectRepository.save(subject);
    }

    @Transactional
    public void delete(int id) {
        subjectRepository.deleteById(id);
    }
}