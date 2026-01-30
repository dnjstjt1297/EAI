package main.java.global.httpserver.handler;

import java.lang.reflect.Method;
import main.java.global.httpserver.dto.request.HttpRequest;
import main.java.global.httpserver.dto.response.HttpResponse;

public class HandlerAdaptor {

    public HttpResponse handle(HttpRequest request, Object handler) throws Exception {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        Object bean = handlerMethod.getBean();

        return (HttpResponse) method.invoke(bean, request);
    }
}
