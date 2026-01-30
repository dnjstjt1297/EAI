package main.java;

import main.java.global.container.IocContainer;
import main.java.global.exception.handler.RestApiExceptionHandler;
import main.java.global.httpserver.FrontController;
import main.java.global.httpserver.connection.Http11Connection;
import main.java.global.httpserver.connection.HttpConnection;
import main.java.global.httpserver.handler.HandlerMapping;
import main.java.global.httpserver.parser.HttpRequestParser;
import main.java.global.httpserver.sender.HttpResponseSender;

public class Main {

    public static void main(String[] args) {

        FrontController frontController =
                IocContainer.getBean("frontController", FrontController.class);
        HttpRequestParser httpRequestParser =
                IocContainer.getBean("httpRequestParser", HttpRequestParser.class);
        HttpResponseSender httpResponseSender
                = IocContainer.getBean("httpResponseSender", HttpResponseSender.class);
        RestApiExceptionHandler restApiExceptionHandler
                = IocContainer.getBean("restApiExceptionHandler", RestApiExceptionHandler.class);
        HandlerMapping handlerMapping
                = IocContainer.getBean("handlerMapping", HandlerMapping.class);

        handlerMapping.init();
        HttpConnection connection = new Http11Connection(frontController, httpRequestParser,
                httpResponseSender, restApiExceptionHandler);

        connection.start();

    }
}