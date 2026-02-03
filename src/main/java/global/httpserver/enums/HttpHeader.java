package main.java.global.httpserver.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum HttpHeader {
    HOST("Host"),
    USER_AGENT("User-Agent"),
    ACCEPT("Accept"),
    AUTHORIZATION("Authorization"),

    // Response Headers
    SERVER("Server"),
    DATE("Date"),

    // Common Headers
    CONTENT_TYPE("Content-Type"),
    CONTENT_LENGTH("Content-Length"),
    CONNECTION("Connection"),
    TRANSFER_ENCODING("Transfer-Encoding"),
    CACHE_CONTROL("Cache-Control");

    private final String value;

}
