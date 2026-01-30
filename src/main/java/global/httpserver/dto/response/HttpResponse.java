package main.java.global.httpserver.dto.response;

import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import main.java.global.httpserver.enums.HttpStatus;
import main.java.global.httpserver.parser.HttpRequestParser;

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
        this(HttpRequestParser.VERSION, httpStatus, new HashMap<>(), body);
    }

}
