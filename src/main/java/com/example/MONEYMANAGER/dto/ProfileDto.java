package com.example.MONEYMANAGER.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDto {
    private Long id;
    @NotBlank(message = "Fullname is required")
    private String fullname;
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String email;
    private String password;
    private String image;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
