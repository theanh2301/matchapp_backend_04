package com.company.mathapp_backend_04.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {
    @Email(message = "Invalid email format")
    String email;
    @NotBlank(message = "fullname cannot be empty")
    String fullName;
    @NotNull(message = "gradeId cannot null")
    Integer gradeId;
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    String phone;
    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dob;
    @NotBlank(message = "password cannot be empty")
    String password;
    @NotBlank(message = "confirm cannot be empty")
    String confirmPassword;
}