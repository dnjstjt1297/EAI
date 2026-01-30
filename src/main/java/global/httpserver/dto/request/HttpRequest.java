package main.java.global.httpserver.dto.request;

import java.util.Map;
import main.java.global.httpserver.enums.HttpMethod;

public record HttpRequest(
        HttpMethod method,
        String url,
        String path,
        String version,
        Map<String, String> params,
        Map<String, String> headers,
        String body
) {

}