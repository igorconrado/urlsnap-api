package com.urlsnap.url.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUrlRequest {

    @NotBlank
    @URL
    private String originalUrl;

    @Size(min = 3, max = 10)
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "must be alphanumeric")
    private String customCode;

    private LocalDateTime expiresAt;
}
