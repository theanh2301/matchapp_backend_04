package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.Grade;
import com.company.mathapp_backend_04.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;

    public List<Grade> getAll() {
        return gradeRepository.findAll();
    }

    public Grade getById(Integer id) {
        return gradeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grade not found"));
    }
}