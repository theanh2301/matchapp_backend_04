package com.company.mathapp_backend_04.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubjectPerformanceResponse {
    private String subjectName;
    private int accuracy;        // %
    private int changePercent;   // % tuần này (+/-)
    private String level;        // Khá / Trung bình / Yếu
}