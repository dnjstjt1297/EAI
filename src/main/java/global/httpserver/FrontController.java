package main.java.global.httpserver;

import java.io.PrintWriter;
import java.util.List;
import lombok.AllArgsConstructor;
import main.java.controller.OrderController;
import main.java.global.container.IocContainer;
import main.java.global.exception.dto.ErrorCodeDto;
import main.java.global.exception.handler.RestApiExceptionHandler;
import main.java.global.httpserver.dto.request.HttpRequest;
import main.java.global.httpserver.dto.response.HttpResponse;
import main.java.global.httpserver.enums.HttpStatus;
import main.java.global.httpserver.interceptor.HandlerInterceptor;
import main.java.global.httpserver.sender.HttpResponseSender;

/**
 * 애플리케이션의 단일 진입점 역할을 하는 Front Controller. 모든 외부 HTTP 요청을 가장 먼저 수신하여 인터셉터를 수행하고, 요청된 경로에 적합한 핸들러에게
 * 작업을 위임하는 역할을 수행함.
 */
@AllArgsConstructor
public class FrontController {

    private final RestApiExceptionHandler restApiExceptionHandler;
    private final List<HandlerInterceptor> interceptors;
    private final HttpResponseSender httpResponseSender;

    public void doDispatch(HttpRequest request, PrintWriter writer) throws Exception {
        HttpResponse response = null;
        HttpStatus status;

        try {
            // 핸들러 매핑 (자세한 구현은 생략, 바로 Order Api 찾음)
            OrderController handler = IocContainer.getBean("orderController",
                    OrderController.class);

            // 인터셉터 전 처리
            for (HandlerInterceptor interceptor : interceptors) {
                if (!interceptor.preHandle(request, response, handler)) {
                    return;
                }
            }

            response = handler.order(request);

            // 인터셉터 후 처리
            for (int i = interceptors.size() - 1; i >= 0; i--) {
                if (!interceptors.get(i).postHandle(request, response, handler)) {
                    return;
                }
            }

        } catch (Exception e) {
            ErrorCodeDto errorCodeDto = restApiExceptionHandler.handle(e);
            status = errorCodeDto.status();

            response = new HttpResponse(request.version(), status, null, errorCodeDto.message());
        } finally {
            httpResponseSender.send(writer, request, response);
        }
    }
}
