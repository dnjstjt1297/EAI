package main.java.global.httpserver.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

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
    private static final Logger log = LoggerFactory.getLogger(Http11Connection.class);

    @Override
    public void start() {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        log.info("[HTTPSERVER] HTTP 1.1 Server is running on port {} ...", PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.execute(() -> handleRequest(clientSocket));

                } catch (IOException e) {
                    log.error("[HTTPSERVER] Socket accept error", e);
                }
            }

        } catch (IOException e) {
            log.error("[HTTPSERVER] ServerSocket error", e);
            throw new RuntimeException(e);
        } finally {
            threadPool.shutdown();
        }
    }

    public void handleRequest(Socket clientSocket) {
        checkSocketTimeout(clientSocket);

        try (clientSocket; InputStream in = clientSocket.getInputStream();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            while (true) {
                String requestId = UUID.randomUUID().toString().substring(0, 8);
                MDC.put("request_id", requestId);
                MDC.put("client_ip", clientSocket.getInetAddress().getHostAddress());

                try {
                    HttpRequest request = httpRequestParser.parse(in);
                    if (request == null) {
                        log.info("[HTTPSERVER] Connection closed by client (EOF)");
                        break;
                    }

                    frontController.doDispatch(request, out);
                    String connHeader = request.headers().get(HEADER_CONNECTION.toLowerCase());
                    if (CLOSE.equalsIgnoreCase(connHeader)) {
                        log.info("[HTTPSERVER] Connection close header detected. Closing...");
                        break;
                    }

                } catch (Exception e) {
                    log.error("[HTTPSERVER] Error processing request", e);
                    sendErrorCodeResponse(out, e);
                    break;
                } finally {
                    log.info("[HTTPSERVER] Request finished.");
                    MDC.clear();
                }
            }

        } catch (Exception e) {
            log.error("[HTTPSERVER] Connection handler error", e);
        }
    }

    private void checkSocketTimeout(Socket socket) {
        try {
            socket.setSoTimeout(TIMEOUT);
            log.info("[HTTPSERVER] Socket timeout set to {}ms", TIMEOUT);
        } catch (IOException e) {
            try {
                log.error("[HTTPSERVER] Socket timeout error", e);
                socket.close();
            } catch (IOException ex) {
                log.error("[HTTPSERVER] Socket close error", e);
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
