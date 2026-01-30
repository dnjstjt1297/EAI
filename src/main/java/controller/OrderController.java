package main.java.controller;

import main.java.global.httpserver.dto.request.HttpRequest;
import main.java.global.httpserver.dto.response.HttpResponse;
import main.java.global.httpserver.enums.HttpMethod;
import main.java.global.httpserver.enums.HttpStatus;
import main.java.global.httpserver.handler.Mapping;
import main.java.global.httpserver.handler.RestController;

@RestController
public class OrderController {

    @Mapping(path = "/order", method = HttpMethod.GET)
    public HttpResponse order(HttpRequest request) {
        return new HttpResponse(HttpStatus.OK, "안녕하세요");
    }
}
