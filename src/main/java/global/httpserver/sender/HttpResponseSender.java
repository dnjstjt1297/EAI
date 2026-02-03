package main.java.global.httpserver.sender;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.PrintWriter;
import java.time.ZonedDateTime;
import java.util.Map;
import main.java.global.httpserver.dto.request.HttpRequest;
import main.java.global.httpserver.dto.response.HttpResponse;
import main.java.global.httpserver.enums.HttpHeader;
import main.java.global.httpserver.enums.HttpLine;
import main.java.global.logging.LogContext;
import main.java.global.logging.annotation.LogExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Response를 HTTP 1.1 프로토콜 규격에 맞게 클라이언트에게 보내는 클래스입니다. (하드코딩이 심합니다...)
 */
public class HttpResponseSender {

    private static final String SERVER_NAME = "EAI_SERVER";
    private static final Logger log = LoggerFactory.getLogger(HttpResponseSender.class);

    public static final String HTTP = "HTTP/";

    @LogExecution
    public void send(PrintWriter writer, HttpRequest request,
            HttpResponse response) {
        try {
            writer.print(HttpLine.VERSION_PREFIX +
                    response.version() + " " +
                    response.status().valueToString() + " " +
                    response.status().getReasonPhrase() + "\r\n");

            makeResponseHeaders(request, response);

            for (Map.Entry<String, String> header : response.headers().entrySet()) {
                writer.print(header.getKey() + ": " + header.getValue() + "\r\n");
            }

            writer.print("\r\n");
            writer.print(response.body());

            if (writer.checkError()) {
                log.warn(
                        "{}[WARN] Client disconnected prematurely or Network error occurred during response transmission.",
                        LogContext.getIndent());
            }

        } catch (Exception e) {
            log.error("{}[ERROR] Failed to send HTTP response to client | Path: {}"
                    , LogContext.getIndent(), (request != null ? request.path() : "UNKNOWN"), e);

        } finally {
            writer.flush();
        }
    }

    private void makeResponseHeaders(HttpRequest request,
            HttpResponse response) {

        response.headers()
                .put(HttpHeader.DATE.getValue().toLowerCase(), ZonedDateTime.now().toString());
        response.headers()
                .put(HttpHeader.SERVER.getValue().toLowerCase(), SERVER_NAME);
        response.headers()
                .put(HttpHeader.CONTENT_LENGTH.getValue().toLowerCase(),
                        String.valueOf(response.body().getBytes(UTF_8).length));

        if (request != null && request.headers()
                .containsKey(HttpHeader.CONNECTION.getValue().toLowerCase())) {

            String clientConn = request.headers().get(HttpHeader.CONNECTION.name().toLowerCase());
            response.headers().put(HttpHeader.CONNECTION.getValue().toLowerCase(), clientConn);

        } else {
            response.headers().put(HttpHeader.CONNECTION.getValue().toLowerCase(), "close");
        }

        // 하드 코딩 주의
        response.headers().put(HttpHeader.CONTENT_TYPE.getValue().toLowerCase(),
                "application/json; charset=utf-8");

    }

}
