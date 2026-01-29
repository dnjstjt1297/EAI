package main.java.global.exception.dto;

import lombok.NonNull;
import main.java.global.httpserver.enums.HttpStatus;

@NonNull
public record ErrorCodeDto(
        @NonNull
        String message,
        @NonNull
        HttpStatus status
) {

}
