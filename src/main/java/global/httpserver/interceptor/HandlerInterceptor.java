package main.java.global.httpserver.interceptor;

import main.java.global.httpserver.dto.request.HttpRequest;
import main.java.global.httpserver.dto.response.HttpResponse;

public interface HandlerInterceptor {

    boolean preHandle(HttpRequest request, HttpResponse response, Object object) throws Exception;

    boolean postHandle(HttpRequest request, HttpResponse response, Object object) throws Exception;

}
