package com.company.mathapp_backend_04.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImportResult {
    private int total;
    private int success;
    private int error;
    private String errorFilePath;

}