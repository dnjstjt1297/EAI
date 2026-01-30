package main.java.global.httpserver.dto;

import main.java.global.httpserver.enums.HttpMethod;

public record MappingInfo(
        String path,
        HttpMethod type
) {

}
