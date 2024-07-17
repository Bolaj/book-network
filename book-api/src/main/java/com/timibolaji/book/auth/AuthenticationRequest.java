package com.timibolaji.book.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder

public class AuthenticationRequest {
    @Email(message = "Kindly provide a valid email")
    @NotEmpty(message = "Email is required")
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @NotEmpty(message = "Password is required")
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password should be 6 characters long minimum ")
    private String password;
}
