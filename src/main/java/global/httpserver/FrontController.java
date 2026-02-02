package main.java.global.httpserver;

import java.io.PrintWriter;
import java.util.List;
import lombok.AllArgsConstructor;
import main.java.global.container.ContainerService;
import main.java.global.exception.RestApiException;
import main.java.global.exception.dto.ErrorCodeDto;
import main.java.global.exception.errorcode.enums.HttpServerErrorCode;
import main.java.global.exception.handler.RestApiExceptionHandler;
import main.java.global.httpserver.dto.MappingInfo;
import main.java.global.httpserver.dto.request.HttpRequest;
import main.java.global.httpserver.dto.response.HttpResponse;
import main.java.global.httpserver.enums.HttpStatus;
import main.java.global.httpserver.frontinterceptor.FrontInterceptor;
import main.java.global.httpserver.handler.HandlerAdaptor;
import main.java.global.httpserver.handler.HandlerMapping;
import main.java.global.httpserver.sender.HttpResponseSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 애플리케이션의 단일 진입점 역할을 하는 Front Controller입니다. 핸들러를 매핑해 요청을 처리할 컨트롤러를 찾아 인터셉터를 수행하고, 핸들러 어댑터를 통해 컨트롤러
 * 로직을 수행합니다. REST API 전용이라 뷰 리졸버는 구현하지 않았습니다.
 */

@AllArgsConstructor
public class FrontController {

    private final RestApiExceptionHandler restApiExceptionHandler;
    private final HttpResponseSender httpResponseSender;
    private final HandlerMapping handlerMapping;
    private final HandlerAdaptor handlerAdaptor;
    private final ContainerService containerService;
    private static final Logger log = LoggerFactory.getLogger(FrontController.class);

    public void doDispatch(HttpRequest request, PrintWriter writer) throws Exception {
        HttpResponse response = null;
        HttpStatus status;
        Object handler = null;
        Exception exception = null;

        List<FrontInterceptor> interceptors = containerService.getFrontInterceptorList();

        try {
            // 핸들러 매핑
            MappingInfo mappingInfo = new MappingInfo(request.path(), request.method());
            handler = handlerMapping.getHandler(mappingInfo);

            if (handler == null) {
                throw new RestApiException(HttpServerErrorCode.NOTFOUND_HANDLER);
            }

            // 인터셉터 전 처리
            {
                for (FrontInterceptor interceptor : interceptors) {
                    if (!interceptor.preHandle(request, response, handler)) {
                        throw new RestApiException(HttpServerErrorCode.INVALID_INTERCEPTOR);
                    }
                }
            }

            // 핸들러 어댑터를 통해 컨트롤러 실행
            response = handlerAdaptor.handle(request, handler);

            // 인터셉터 후 처리
            for (int i = interceptors.size() - 1; i >= 0; i--) {
                interceptors.get(i).postHandle(request, response, handler);
            }

        } catch (Exception e) {
            exception = e;
            ErrorCodeDto errorCodeDto = restApiExceptionHandler.handle(e);
            status = errorCodeDto.status();
            response = new HttpResponse(status, errorCodeDto.message());
        } finally {
            httpResponseSender.send(writer, request, response);
            triggerAfterCompletion(interceptors, request, response, handler, exception);
        }
    }

    private void triggerAfterCompletion(List<FrontInterceptor> interceptors,
            HttpRequest request, HttpResponse response, Object handler, Exception exception) {
        // 인터셉터 끝 처리
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            try {
                interceptors.get(i).afterCompletion(request, response, handler, exception);
            } catch (Exception e) {
                log.error("[ERROR] path: {}, message: {}", request.path(), e.getMessage());
            }
        }
    }
}
