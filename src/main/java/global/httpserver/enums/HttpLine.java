package main.java.global.httpserver.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class HttpLine {

    public static final String SP = " ";
    public static final String CRLF = "\r\n";
    public static final String VERSION_PREFIX = "HTTP/";
    public static final String DEFAULT_VERSION = "1.1";

    @Getter
    @RequiredArgsConstructor
    public enum Property {
        METHOD("Method"),
        PATH("Path"),
        VERSION("Version"),
        STATUS_CODE("Status-Code"),
        REASON_PHRASE("Reason-Phrase");

        private final String description;
    }
}