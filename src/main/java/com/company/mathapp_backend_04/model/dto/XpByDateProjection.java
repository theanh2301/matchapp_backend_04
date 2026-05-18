package com.company.mathapp_backend_04.model.dto;

import java.time.LocalDate;

public interface XpByDateProjection {
    LocalDate getDate();

    Long getTotalXp();
}
