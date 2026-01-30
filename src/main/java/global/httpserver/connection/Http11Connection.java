package main.java.global.httpserver.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.AllArgsConstructor;
import main.java.global.exception.dto.ErrorCodeDto;
import main.java.global.exception.handler.RestApiExceptionHandler;
import main.java.global.httpserver.FrontController;
import main.java.global.httpserver.dto.request.HttpRequest;
import main.java.global.httpserver.dto.response.HttpResponse;
import main.java.global.httpserver.parser.HttpRequestParser;
import main.java.global.httpserver.sender.HttpResponseSender;

/**
 * HTTP 1.1 프로토콜을 기반으로 클라이언트의 연결을 유지하고 처리하는 연결 관리 클래스입니다. 멀티 스레딩을 지원하고 Keep-Alive를 지원 해 클라이언트가 연결
 * 종료를 요청하기 전까지 동일한 소켓에서 연속적인 HTTP 요청을 처리합니다.
 **/

@AllArgsConstructor
public class Http11Connection implements HttpConnection {

    private static final String HEADER_CONNECTION = "connection";
    private static final String CLOSE = "close";
    private static final int PORT = 8080;
    private static final int TIMEOUT = 5000;
    private static final int THREAD_POOL_SIZE = 10;

    private final FrontController frontController;
    private final HttpRequestParser httpRequestParser;
    private final HttpResponseSender httpResponseSender;
    private final RestApiExceptionHandler restApiExceptionHandler;

    @Override
    public void start() {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.execute(() -> handleRequest(clientSocket, frontController));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            threadPool.shutdown();
        }
    }

    public void handleRequest(Socket clientSocket, FrontController frontController) {
        checkSocketTimeout(clientSocket);

        try (clientSocket; InputStream in = clientSocket.getInputStream();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            while (true) {
                try {
                    HttpRequest request = httpRequestParser.parse(in);
                    frontController.doDispatch(request, out);

                    if (request.headers().containsKey(HEADER_CONNECTION.toLowerCase()) &&
                            request.headers().get(HEADER_CONNECTION.toLowerCase())
                                    .equals(CLOSE.toLowerCase())) {
                        break;
                    }

                } catch (Exception e) {
                    sendErrorCodeResponse(out, e);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkSocketTimeout(Socket socket) {
        try {
            socket.setSoTimeout(TIMEOUT);
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendErrorCodeResponse(PrintWriter out, Exception e) {
        ErrorCodeDto errorCodeDto = restApiExceptionHandler.handle(e);
        HttpResponse httpResponse = new HttpResponse(errorCodeDto.status(),
                errorCodeDto.message());
        httpResponseSender.sendError(out, httpResponse);
    }

}
