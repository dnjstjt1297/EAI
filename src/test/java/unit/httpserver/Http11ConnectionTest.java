package test.java.unit.httpserver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import main.java.global.exception.handler.RestApiExceptionHandler;
import main.java.global.httpserver.FrontController;
import main.java.global.httpserver.connection.Http11Connection;
import main.java.global.httpserver.dto.request.HttpRequest;
import main.java.global.httpserver.enums.HttpMethod;
import main.java.global.httpserver.parser.HttpRequestParser;
import main.java.global.httpserver.sender.HttpResponseSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class Http11ConnectionTest {

    @Mock
    Socket socket;
    @Mock
    FrontController frontController;
    @Mock
    HttpRequestParser parser;
    @Mock
    HttpResponseSender responseSender;
    @Mock
    RestApiExceptionHandler exceptionHandler;
    @InjectMocks
    Http11Connection connection;

    @Test
    @DisplayName("클라이언트의 HTTP 요청을 파싱하여 FrontController로 전달할 수 있다.")
    void handleRequestTest() throws Exception {
        // given
        String rawRequest = "POST /order HTTP/1.1\r\nConnection: close\r\n\r\n";
        
        InetAddress mockAddress = mock(java.net.InetAddress.class);
        given(mockAddress.getHostAddress()).willReturn("127.0.0.1");
        given(socket.getInetAddress()).willReturn(mockAddress);

        given(socket.getInputStream()).willReturn(new ByteArrayInputStream(rawRequest.getBytes()));
        given(socket.getOutputStream()).willReturn(new ByteArrayOutputStream());

        HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, "/order",
                "/order", "1.1", null, new HashMap<>(), null);
        httpRequest.headers().put("connection", "close");

        given(parser.parse(any())).willReturn(httpRequest).willReturn(null);

        // when
        connection.handleRequest(socket);

        // then
        verify(frontController).doDispatch(any(), any());
    }
}
