package main.java.global.exception.dto;

import main.java.global.httpserver.enums.HttpStatus;

public record ErrorCodeDto(
        String message,
        HttpStatus status
) {

}
