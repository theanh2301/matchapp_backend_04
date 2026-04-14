package com.company.mathapp_backend_04.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserXPHistoryRequest {
    @NotNull(message = "userId cannot be null")
    private Integer userId;
    @NotNull(message = "xp cannot be null")
    private Integer xp;
    @NotNull(message = "source cannot be null")
    private String source;
    @NotNull(message = "sourceId cannot be null")
    private Integer sourceId;
}