package com.company.mathapp_backend_04.model.response;

import com.company.mathapp_backend_04.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private String token;
    private Integer userId;
    private Integer gradeId;
    private Role role;
}
