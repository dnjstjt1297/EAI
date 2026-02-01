package main.java.global.httpserver.interceptor;

import main.java.global.exception.RestApiException;
import main.java.global.exception.errorcode.enums.CommonErrorCode;
import main.java.global.httpserver.dto.request.HttpRequest;
import main.java.global.httpserver.dto.response.HttpResponse;
import main.java.order.controller.OrderController;

public class ValidationInterceptor implements HandlerInterceptor {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_XML = "application/xml";
    private static final String APPLICATION_JSON = "application/json";
    private static final String CONTENT_LENGTH = "Content-Length";

    @Override
    public boolean preHandle(HttpRequest request, HttpResponse response, Object handler)
            throws Exception {
        if (handler instanceof OrderController) {
            if (!request.headers().containsKey(CONTENT_TYPE.toLowerCase()) ||
                    !request.headers().get(CONTENT_TYPE.toLowerCase())
                            .equals(APPLICATION_XML.toLowerCase())) {

                throw new RestApiException(CommonErrorCode.INVALID_HEADER);
            }
            if (!request.headers().containsKey(CONTENT_LENGTH.toLowerCase())) {
                throw new RestApiException(CommonErrorCode.INVALID_HEADER);
            }
        }

        return true;
    }

    @Override
    public boolean postHandle(HttpRequest request, HttpResponse response, Object handler)
            throws Exception {
        if (handler instanceof OrderController) {
            if (!response.headers().containsKey(CONTENT_TYPE.toLowerCase()) ||
                    !request.headers().get(CONTENT_TYPE.toLowerCase())
                            .contains(APPLICATION_JSON.toLowerCase())) {
                throw new RestApiException(CommonErrorCode.INTERNAL_SERVER_ERROR);
            }
            if (!request.headers().containsKey(CONTENT_LENGTH.toLowerCase())) {
                throw new RestApiException(CommonErrorCode.INTERNAL_SERVER_ERROR);
            }
        }

        return true;
    }

}
