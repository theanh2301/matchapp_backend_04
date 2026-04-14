package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.Grade;
import com.company.mathapp_backend_04.model.dto.PracticeOverviewDTO;
import com.company.mathapp_backend_04.model.enums.PracticeType;
import com.company.mathapp_backend_04.model.response.PracticeStatsGroupResponse;
import com.company.mathapp_backend_04.model.response.PracticeStatsResponse;
import com.company.mathapp_backend_04.repository.PracticeRepository;
import com.company.mathapp_backend_04.repository.UserPracticeRepository;
import com.company.mathapp_backend_04.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class PracticeService {

    private final PracticeRepository practiceRepository;
    private final UserPracticeRepository userPracticeRepository;
    private final UserRepository userRepository;


    public PracticeStatsResponse getPracticeStats(PracticeType practiceType, Integer userId) {

        Integer gradeId = userRepository.findById(userId)
                .orElseThrow()
                .getGrade()
                .getId();

        Integer total = practiceRepository.countByPracticeTypeAndGrade_Id(practiceType, gradeId);

        Integer completed = userPracticeRepository.countCompletedByPracticeTypeAndUserId(practiceType, userId, gradeId);

        return new PracticeStatsResponse(practiceType, total, completed);
    }

    public List<PracticeOverviewDTO> getPracticeOverview(PracticeType practiceType, Integer userId, Integer gradeId) {
        return practiceRepository.getPracticeOverviewWithProgress(
                practiceType.name(), userId, gradeId
        );
    }

    public List<PracticeOverviewDTO> getPracticeOverviewWeak(Integer userId) {
        Integer gradeId = userRepository.findById(userId)
                .orElseThrow()
                .getGrade()
                .getId();

        return practiceRepository.getPracticeOverviewWeak(userId, gradeId);
    }

    public PracticeStatsGroupResponse getAllPracticeStats(Integer userId) {

        Integer gradeId = userRepository.findById(userId)
                .orElseThrow()
                .getGrade()
                .getId();

        Map<PracticeType, Integer> totalMap = new HashMap<>();
        Map<PracticeType, Integer> completedMap = new HashMap<>();

        for (Object[] row : practiceRepository.countAllByPracticeType(gradeId)) {
            totalMap.put((PracticeType) row[0], ((Long) row[1]).intValue());
        }

        for (Object[] row : practiceRepository.countCompletedGroupByType(userId)) {
            completedMap.put((PracticeType) row[0], ((Long) row[1]).intValue());
        }

        List<PracticeStatsResponse> result = new ArrayList<>();

        for (PracticeType type : PracticeType.values()) {
            result.add(new PracticeStatsResponse(
                    type,
                    totalMap.getOrDefault(type, 0),
                    completedMap.getOrDefault(type, 0)
            ));
        }

        return new PracticeStatsGroupResponse(result);
    }

    /*public PracticeStatsResponse getPracticeStats(PracticeType practiceType, Integer userId, Integer gradeId) {
        Integer total = practiceRepository
                .countByPracticeTypeAndGrade_Id(practiceType, gradeId);

        Integer completed = userPracticeRepository
                .countCompletedByPracticeTypeAndUserId(practiceType, userId, gradeId);

        return new PracticeStatsResponse(practiceType, total, completed);
    }

    public List<PracticeOverviewDTO> getPracticeOverview(PracticeType practiceType, Integer userId, Integer gradeId) {
        return practiceRepository.getPracticeOverviewWithProgress(
                practiceType.name(), userId, gradeId
        );
    }

    public List<PracticeOverviewDTO> getPracticeOverviewWeak(Integer userId, Integer gradeId) {
        return practiceRepository.getPracticeOverviewWeak(userId, gradeId);
    }*/
}