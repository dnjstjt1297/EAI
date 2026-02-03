package main.java.global.httpserver.dto.response;

import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import main.java.global.httpserver.enums.HttpLine;
import main.java.global.httpserver.enums.HttpStatus;

public record HttpResponse(
        @NonNull
        String version,
        @NonNull
        HttpStatus status,
        @NonNull
        Map<String, String> headers,
        String body
) {

    public HttpResponse(HttpStatus httpStatus, String body) {
        this(HttpLine.DEFAULT_VERSION, httpStatus, new HashMap<>(), body);
    }

}
