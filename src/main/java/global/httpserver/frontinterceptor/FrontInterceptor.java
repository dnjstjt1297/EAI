package main.java.global.httpserver.frontinterceptor;

import main.java.global.httpserver.dto.request.HttpRequest;
import main.java.global.httpserver.dto.response.HttpResponse;

public interface FrontInterceptor {

    boolean preHandle(HttpRequest request, HttpResponse response, Object object) throws Exception;

    void postHandle(HttpRequest request, HttpResponse response, Object object) throws Exception;

    void afterCompletion(HttpRequest request, HttpResponse response, Object object, Exception ex)
            throws Exception;


}
