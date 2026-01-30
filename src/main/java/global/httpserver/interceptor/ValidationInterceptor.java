package main.java.global.httpserver.interceptor;

import main.java.global.httpserver.dto.request.HttpRequest;
import main.java.global.httpserver.dto.response.HttpResponse;

public class ValidationInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpRequest request, HttpResponse response, Object object)
            throws Exception {
        return true;
    }

    @Override
    public boolean postHandle(HttpRequest request, HttpResponse response, Object object)
            throws Exception {
        return true;
    }

}
