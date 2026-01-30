package main.java.global.container;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import main.java.controller.OrderController;
import main.java.controller.TestController;
import main.java.global.exception.handler.RestApiExceptionHandler;
import main.java.global.httpserver.FrontController;
import main.java.global.httpserver.handler.HandlerAdaptor;
import main.java.global.httpserver.handler.HandlerMapping;
import main.java.global.httpserver.interceptor.HandlerInterceptor;
import main.java.global.httpserver.interceptor.ValidationInterceptor;
import main.java.global.httpserver.parser.HttpRequestParser;
import main.java.global.httpserver.sender.HttpResponseSender;

/**
 * 스프링 프레임워크를 모방한 IoC 컨테이너. 객체 생성의 주도권을 개발자로부터 컨테이너로 이전하여 유연한 리팩토링 환경을 제공함.
 */

public class IocContainer {

    // 빈처럼 객체 관리
    private static final Map<String, Object> beanMap = new HashMap<>();

    static {

        // Controller 관련
        OrderController orderController = new OrderController();
        registerBean("orderController", new OrderController());

        TestController testController = new TestController();
        registerBean("testController", new TestController());

        // objectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        registerBean("objectMapper", objectMapper);

        // exception
        RestApiExceptionHandler restApiExceptionHandler = new RestApiExceptionHandler();
        registerBean("restApiExceptionHandler", restApiExceptionHandler);

        // interceptor 관련
        List<HandlerInterceptor> interceptorList = new ArrayList<>();
        interceptorList.add(new ValidationInterceptor());
        registerBean("interceptorList", interceptorList);

        // httpServer 관련
        HttpRequestParser httpRequestParser = new HttpRequestParser();
        registerBean("httpRequestParser", httpRequestParser);

        HttpResponseSender httpResponseSender = new HttpResponseSender();
        registerBean("httpResponseSender", httpResponseSender);

        HandlerMapping handlerMapping = new HandlerMapping(new HashMap<>(), beanMap);
        registerBean("handlerMapping", handlerMapping);

        HandlerAdaptor handlerAdaptor = new HandlerAdaptor();
        registerBean("handlerAdaptor", handlerAdaptor);

        FrontController frontController = new FrontController(restApiExceptionHandler,
                interceptorList, httpResponseSender, handlerMapping, handlerAdaptor);
        registerBean("frontController", frontController);

    }


    public static <T> T getBean(String beanId, Class<T> clazz) {
        return clazz.cast(beanMap.get(beanId));
    }

    public static void registerBean(String beanId, Object bean) {
        beanMap.put(beanId, bean);
    }

    public static Map<String, Object> getAllBean() {
        return beanMap;
    }
}
