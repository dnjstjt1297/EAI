package main.java;


import main.java.global.container.ContainerService;
import main.java.global.container.ContainerServiceImpl;
import main.java.global.httpserver.connection.Http11Connection;
import main.java.global.httpserver.connection.HttpConnection;
import main.java.global.httpserver.handler.HandlerMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) {

        ContainerService containerService = new ContainerServiceImpl();

        HttpConnection connection = (HttpConnection) containerService.getBean(
                Http11Connection.class.getName());
        HandlerMapping handlerMapping = (HandlerMapping) containerService.getBean(
                HandlerMapping.class.getName());

        handlerMapping.init();
        connection.start();

    }
}