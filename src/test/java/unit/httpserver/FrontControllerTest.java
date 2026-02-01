package test.java.unit.httpserver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.never;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import main.java.global.exception.RestApiException;
import main.java.global.exception.dto.ErrorCodeDto;
import main.java.global.exception.errorcode.enums.CommonErrorCode;
import main.java.global.exception.errorcode.enums.HttpServerErrorCode;
import main.java.global.exception.handler.RestApiExceptionHandler;
import main.java.global.httpserver.FrontController;
import main.java.global.httpserver.dto.request.HttpRequest;
import main.java.global.httpserver.dto.response.HttpResponse;
import main.java.global.httpserver.enums.HttpMethod;
import main.java.global.httpserver.enums.HttpStatus;
import main.java.global.httpserver.handler.HandlerAdaptor;
import main.java.global.httpserver.handler.HandlerMapping;
import main.java.global.httpserver.handler.HandlerMethod;
import main.java.global.httpserver.interceptor.HandlerInterceptor;
import main.java.global.httpserver.sender.HttpResponseSender;
import main.java.order.controller.OrderController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FrontControllerTest {

    @Spy
    List<HandlerInterceptor> interceptors = new ArrayList<>();

    @Mock
    HandlerInterceptor handlerInterceptor1;
    @Mock
    HandlerInterceptor handlerInterceptor2;
    @Mock
    PrintWriter writer;
    @Mock
    HttpResponseSender httpResponseSender;
    @Mock
    OrderController orderController;
    @Mock
    RestApiExceptionHandler restApiExceptionHandler;
    @Mock
    HandlerMapping handlerMapping; // 추가 필요
    @Mock
    HandlerAdaptor handlerAdaptor; // 추가 필요 (기존 Adaptor 이름에 맞춰 수정)
    @Mock
    HandlerMethod handlerMethod;   // 추가 필요 (매핑의 결과물)

    @InjectMocks
    FrontController frontController;

    String errorKey;

    @BeforeEach
    void setUp() {
        errorKey = "{\"error\":\"";
        interceptors.clear();
        interceptors.add(handlerInterceptor1);
        interceptors.add(handlerInterceptor2);
    }

    @Test
    @DisplayName("모든 인터셉터 통과 시 응답을 전송할 수 있다.")
    void DoDispatchTest() throws Exception {
        // given
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http//localhost/8080/order",
                "/order", "1.1", null, null, null);
        HttpResponse httpResponse = new HttpResponse(HttpStatus.OK, "OK");

        given(handlerMapping.getHandler(argThat(info ->
                info.path().equals("/order") && info.type() == HttpMethod.GET)))
                .willReturn(handlerMethod);

        given(handlerInterceptor1.preHandle(any(), any(), any())).willReturn(true);
        given(handlerInterceptor2.preHandle(any(), any(), any())).willReturn(true);

        given(handlerAdaptor.handle(eq(httpRequest), eq(handlerMethod))).willReturn(httpResponse);

        given(handlerInterceptor1.postHandle(any(), any(), any())).willReturn(true);
        given(handlerInterceptor2.postHandle(any(), any(), any())).willReturn(true);

        // when
        frontController.doDispatch(httpRequest, writer);

        // then
        verify(httpResponseSender).send(eq(writer), eq(httpRequest), eq(httpResponse));

    }

    @Test
    @DisplayName("핸들러어댑터에서 예외 발생 시 에러 응답을 생성할 수 있다.")
    void doDispatchExceptionTest() throws Exception {
        // given
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http//localhost/8080/order",
                "/order", "1.1", null, null, null);

        // preHandle만 통과 (postHandle은 예외 시 실행 안 되므로 생략 - UnnecessaryStubbing 방지)
        given(handlerInterceptor1.preHandle(any(), any(), any())).willReturn(true);
        given(handlerInterceptor2.preHandle(any(), any(), any())).willReturn(true);

        given(handlerMapping.getHandler(argThat(info ->
                info.path().equals("/order") && info.type() == HttpMethod.GET)))
                .willReturn(handlerMethod);

        given(handlerAdaptor.handle(any(), any())).willThrow(
                new RestApiException(CommonErrorCode.UNKNOWN_ERROR));

        ErrorCodeDto errorDto = new ErrorCodeDto(
                errorKey + "\"" + CommonErrorCode.UNKNOWN_ERROR.getMessage() + "\"" + "}",
                HttpStatus.INTERNAL_SERVER_ERROR);
        given(restApiExceptionHandler.handle(any(RestApiException.class))).willReturn(errorDto);

        // when
        frontController.doDispatch(httpRequest, writer);

        // then
        verify(restApiExceptionHandler).handle(any(RestApiException.class));
        verify(httpResponseSender).send(eq(writer), eq(httpRequest), argThat(response ->
                response.status() == HttpStatus.INTERNAL_SERVER_ERROR
        ));
    }

    @Test
    @DisplayName("pre인터셉터가 false를 반환하면 핸들러 어댑터가 실행되지 않고 에러 응답을 생성할 수 있다.")
    void preHandleFalseTest() throws Exception {
        // given
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http//localhost/8080/order",
                "/order", "1.1", null, null, null);

        given(handlerMapping.getHandler(any())).willReturn(new Object());
        given(handlerInterceptor1.preHandle(any(), any(), any())).willReturn(false); // 두 번째에서 거절

        ErrorCodeDto errorDto = new ErrorCodeDto(
                errorKey + "\"" + CommonErrorCode.INTERNAL_SERVER_ERROR.getMessage() + "\"" + "}",
                HttpStatus.INTERNAL_SERVER_ERROR);
        given(restApiExceptionHandler.handle(any(RestApiException.class))).willReturn(errorDto);

        // when
        frontController.doDispatch(httpRequest, writer);

        // then
        verify(handlerInterceptor2, never()).preHandle(any(), any(), any());
        verify(httpResponseSender).send(eq(writer), eq(httpRequest), argThat(response ->
                response != null && response.body()
                        .contains(CommonErrorCode.INTERNAL_SERVER_ERROR.getMessage())
        ));
    }

    @Test
    @DisplayName("컨트롤러는 성공했으나, post 인터셉터 중 하나라도 false를 반환하면 에러 응답을 생성할 수 있다..")
    void postHandleFailTest() throws Exception {
        // given
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http//localhost/8080/order",
                "/order", "1.1", null, null, null);

        HttpResponse successResponse = new HttpResponse(HttpStatus.OK, "Success");

        given(handlerMapping.getHandler(any())).willReturn(new Object());
        given(handlerInterceptor1.preHandle(any(), any(), any())).willReturn(true);
        given(handlerInterceptor2.preHandle(any(), any(), any())).willReturn(true);
        given(handlerAdaptor.handle(any(), any())).willReturn(successResponse);
        given(handlerInterceptor2.postHandle(any(), any(), any())).willReturn(false);

        ErrorCodeDto errorDto = new ErrorCodeDto("POST_HANDLE_FAIL",
                HttpStatus.INTERNAL_SERVER_ERROR);
        given(restApiExceptionHandler.handle(any())).willReturn(errorDto);

        // when
        frontController.doDispatch(httpRequest, writer);

        // then
        verify(httpResponseSender).send(eq(writer), eq(httpRequest),
                argThat(res -> res.status() == HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    @DisplayName("존재하지 않은 핸들러 조회 시 에러 응답을 생성할 수 있다.")
    void handlerMappingNotFoundTest() throws Exception {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http//localhost/8080/order",
                "/order", "1.1", null, null, null);

        given(handlerMapping.getHandler(any())).willThrow(
                new RestApiException(HttpServerErrorCode.NOTFOUND_HANDLER));

        ErrorCodeDto errorDto = new ErrorCodeDto(
                errorKey + "\"" + HttpServerErrorCode.NOTFOUND_HANDLER.getMessage() + "\"" + "}",
                HttpStatus.NOT_FOUND);
        given(restApiExceptionHandler.handle(any(RestApiException.class))).willReturn(errorDto);

        frontController.doDispatch(httpRequest, writer);

        verify(httpResponseSender).send(eq(writer), eq(httpRequest),
                argThat(response -> response != null && response.body()
                        .contains(HttpServerErrorCode.NOTFOUND_HANDLER.getMessage())));
    }

    @Test
    @DisplayName("OOM 발생 시에도 finally 블록을 통해 응답 전송을 시도할 수 있다.")
    void handleOOMTest() throws Exception {
        // given
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "/order", "/order", "1.1", null,
                null, null);
        given(handlerMapping.getHandler(any())).willThrow(new OutOfMemoryError("가짜 OOM"));

        // when
        frontController.doDispatch(httpRequest, writer);

        // then
        // OOM 시 response는 null이지만 httpResponseSender.send는 호출되어야 함
        verify(httpResponseSender).send(eq(writer), eq(httpRequest), any());
    }

}