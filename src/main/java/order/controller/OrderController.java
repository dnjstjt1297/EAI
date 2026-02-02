package main.java.order.controller;

import lombok.AllArgsConstructor;
import main.java.global.httpserver.dto.request.HttpRequest;
import main.java.global.httpserver.dto.response.HttpResponse;
import main.java.global.httpserver.enums.HttpMethod;
import main.java.global.httpserver.enums.HttpStatus;
import main.java.global.httpserver.handler.Mapping;
import main.java.global.httpserver.handler.RestController;
import main.java.global.logging.annotation.LogExecution;
import main.java.order.dto.request.OrderRequest;
import main.java.order.dto.response.OrderResponse;
import main.java.order.parse.OrderXmlParse;
import main.java.order.service.OrderService;

@RestController
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderXmlParse orderXmlParse;

    @LogExecution
    @Mapping(path = "/order", method = HttpMethod.POST)
    public HttpResponse order(HttpRequest request) {

        OrderRequest orderRequest = orderXmlParse.parseOrderXml(request.body());
        OrderResponse response = orderService.order(orderRequest);
        String body = response.message();
        return new HttpResponse(HttpStatus.CREATED, body);
    }
}
