package com.fitness.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "email is required")
    @Email(message ="Invalid email format")
    private String email;

    @NotBlank(message = "password is must")
    @Size(message = "password must have atleast 6 characters")
    private String password;
    private String keycloakId;

    private String firstName;
    private String lastName;



}
