package ru.practicum.shareit.exception;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ErrorResponse {
    private String error;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ErrorResponse(String error) {
        this.error = error;
    }
}
