package com.company.mathapp_backend_04.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypePerformanceResponse {
    private String type;
    private int accuracy;
}