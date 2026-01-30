package main.java.controller;

import main.java.global.httpserver.dto.request.HttpRequest;
import main.java.global.httpserver.dto.response.HttpResponse;
import main.java.global.httpserver.enums.HttpMethod;
import main.java.global.httpserver.enums.HttpStatus;
import main.java.global.httpserver.handler.Mapping;
import main.java.global.httpserver.handler.RestController;

@RestController
public class TestController {

    @Mapping(path = "/test1", method = HttpMethod.GET)
    public HttpResponse test(HttpRequest request) {
        return new HttpResponse(HttpStatus.OK, "test");
    }

    @Mapping(path = "/test2", method = HttpMethod.POST)
    public HttpResponse test2(HttpRequest request) {
        return new HttpResponse(HttpStatus.OK, "test2");
    }

    @Mapping(path = "/test2", method = HttpMethod.PUT)
    public HttpResponse test3(HttpRequest request) {
        return new HttpResponse(HttpStatus.OK, "test3");
    }
}
