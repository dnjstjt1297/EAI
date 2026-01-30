package test.unit.httpserver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
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
    HttpResponseSender sender;
    @Mock
    RestApiExceptionHandler exceptionHandler;

    @InjectMocks
    Http11Connection connection;

    @Test
    @DisplayName("클라이언트의 HTTP 요청을 파싱하여 FrontController로 전달할 수 있다.")
    void handleRequestTest() throws Exception {
        // given
        String rawRequest = "GET /test HTTP/1.1\r\nConnection: close\r\n\r\n";
        InputStream in = new ByteArrayInputStream(rawRequest.getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        given(socket.getInputStream()).willReturn(in);
        given(socket.getOutputStream()).willReturn(out);

        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "/test",
                "/test", "1.1", null, new HashMap<>(), null);
        httpRequest.headers().put("connection", "close");

        given(parser.parse(any())).willReturn(httpRequest);

        // when
        connection.handleRequest(socket, frontController);

        // then
        verify(frontController).doDispatch(eq(httpRequest), any(PrintWriter.class));
    }
}
