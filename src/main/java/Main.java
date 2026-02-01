package main.java;

import main.java.global.container.Container;
import main.java.global.exception.handler.RestApiExceptionHandler;
import main.java.global.httpserver.FrontController;
import main.java.global.httpserver.connection.Http11Connection;
import main.java.global.httpserver.connection.HttpConnection;
import main.java.global.httpserver.handler.HandlerMapping;
import main.java.global.httpserver.parser.HttpRequestParser;
import main.java.global.httpserver.sender.HttpResponseSender;

public class Main {

    public static void main(String[] args) {

        HandlerMapping handlerMapping
                = Container.getBean("handlerMapping", HandlerMapping.class);
        handlerMapping.init();

        FrontController frontController
                = Container.getBean("frontController", FrontController.class);
        HttpRequestParser httpRequestParser
                = Container.getBean("httpRequestParser", HttpRequestParser.class);
        HttpResponseSender httpResponseSender
                = Container.getBean("httpResponseSender", HttpResponseSender.class);
        RestApiExceptionHandler restApiExceptionHandler
                = Container.getBean("restApiExceptionHandler", RestApiExceptionHandler.class);

        HttpConnection connection = new Http11Connection(frontController, httpRequestParser,
                httpResponseSender, restApiExceptionHandler);

        connection.start();

    }
}