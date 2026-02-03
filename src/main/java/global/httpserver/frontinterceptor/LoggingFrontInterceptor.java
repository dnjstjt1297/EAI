package main.java.global.httpserver.frontinterceptor;

import main.java.global.httpserver.dto.request.HttpRequest;
import main.java.global.httpserver.dto.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingFrontInterceptor implements FrontInterceptor {

    private final static Logger log = LoggerFactory.getLogger(LoggingFrontInterceptor.class);
    private final static String HEADER_CONNECTION = "Connection";


    private Long startTime;

    @Override
    public boolean preHandle(HttpRequest request, HttpResponse response, Object object)
            throws Exception {
        startTime = System.nanoTime();
        String contentType = request.headers().getOrDefault(HEADER_CONNECTION.toLowerCase(), "");
        log.info("[INFO] REQUEST: {} {} , H-Connection: {}", request.method(), request.path(),
                contentType);
        return true;
    }

    @Override
    public void postHandle(HttpRequest request, HttpResponse response, Object object)
            throws Exception {
    }

    @Override
    public void afterCompletion(HttpRequest request, HttpResponse response, Object object,
            Exception ex) throws Exception {
        Long duration = (startTime != null) ? System.nanoTime() - startTime : 0;
        log.info("[INFO] RESPONSE: {} | Time: {}ms", request.path(), duration);
    }
}
