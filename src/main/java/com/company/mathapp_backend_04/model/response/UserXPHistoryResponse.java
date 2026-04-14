package com.company.mathapp_backend_04.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserXPHistoryResponse {
    private Integer id;
    private Integer userId;
    private Integer xp;
    private String source;
    private Integer sourceId;
    private LocalDateTime earnedAt;
}
