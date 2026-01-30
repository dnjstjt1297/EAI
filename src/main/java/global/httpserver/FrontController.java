package main.java.global.httpserver;

import java.io.PrintWriter;
import java.util.List;
import lombok.AllArgsConstructor;
import main.java.global.exception.RestApiException;
import main.java.global.exception.dto.ErrorCodeDto;
import main.java.global.exception.errorcode.enums.HttpServerErrorCode;
import main.java.global.exception.handler.RestApiExceptionHandler;
import main.java.global.httpserver.dto.MappingInfo;
import main.java.global.httpserver.dto.request.HttpRequest;
import main.java.global.httpserver.dto.response.HttpResponse;
import main.java.global.httpserver.enums.HttpStatus;
import main.java.global.httpserver.handler.HandlerAdaptor;
import main.java.global.httpserver.handler.HandlerMapping;
import main.java.global.httpserver.interceptor.HandlerInterceptor;
import main.java.global.httpserver.sender.HttpResponseSender;

/**
 * 애플리케이션의 단일 진입점 역할을 하는 Front Controller입니다. 핸들러를 매핑해 요청을 처리할 컨트롤러를 찾아 인터셉터를 수행하고, 핸들러 어댑터를 통해 컨트롤러
 * 로직을 수행합니다. REST API 전용이라 뷰 리졸버는 구현하지 않았습니다.
 */

@AllArgsConstructor
public class FrontController {

    private final RestApiExceptionHandler restApiExceptionHandler;
    private final List<HandlerInterceptor> interceptors;
    private final HttpResponseSender httpResponseSender;
    private final HandlerMapping handlerMapping;
    private final HandlerAdaptor handlerAdaptor;

    public void doDispatch(HttpRequest request, PrintWriter writer) throws Exception {
        HttpResponse response = null;
        HttpStatus status;

        try {
            // 핸들러 매핑
            MappingInfo mappingInfo = new MappingInfo(request.path(), request.method());
            Object handler = handlerMapping.getHandler(mappingInfo);

            if (handler == null) {
                throw new RestApiException(HttpServerErrorCode.NOTFOUND_HANDLER);
            }

            // 인터셉터 전 처리
            {
                for (HandlerInterceptor interceptor : interceptors) {
                    if (!interceptor.preHandle(request, response, handler)) {
                        throw new RestApiException(HttpServerErrorCode.INVALID_INTERCEPTOR);
                    }
                }
            }

            // 핸들러 어댑터를 통해 컨트롤러 실행
            response = handlerAdaptor.handle(request, handler);

            // 인터셉터 후 처리
            for (int i = interceptors.size() - 1; i >= 0; i--) {
                if (!interceptors.get(i).postHandle(request, response, handler)) {
                    throw new RestApiException(HttpServerErrorCode.INVALID_INTERCEPTOR);
                }
            }

        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            ErrorCodeDto errorCodeDto = restApiExceptionHandler.handle(e);
            status = errorCodeDto.status();
            response = new HttpResponse(status, errorCodeDto.message());
        } finally {
            httpResponseSender.send(writer, request, response);
        }
    }
}
