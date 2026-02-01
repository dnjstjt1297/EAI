package main.java.global.httpserver.sender;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.PrintWriter;
import java.time.ZonedDateTime;
import java.util.Map;
import main.java.global.httpserver.dto.request.HttpRequest;
import main.java.global.httpserver.dto.response.HttpResponse;


/**
 * Response를 HTTP 1.1 프로토콜 규격에 맞게 클라이언트에게 보내는 클래스입니다. (하드코딩이 심합니다...)
 */
public class HttpResponseSender {

    private static final String HEADER_DATE = "Date";
    private static final String HEADER_SERVER = "Server";
    private static final String HEADER_CONNECTION = "Connection";
    private static final String CLOSE = "close";
    private static final String HEADER_CHARSET = "charset";
    private static final String SERVER_NAME = "EAI_SERVER";
    public static final String APPLICATION_JSON = "application/json";

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";

    public static final String HTTP = "HTTP/";


    public void send(PrintWriter writer, HttpRequest request,
            HttpResponse response) {
        try {
            writer.print(HTTP +
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
                System.err.println("Error in HTTP response writer");
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            writer.flush();
        }
    }

    public void sendError(PrintWriter writer, HttpResponse response) {
        try {
            writer.print(HTTP +
                    response.version() + " " +
                    response.status().valueToString() + " " +
                    response.status().getReasonPhrase() + "\r\n");

            response.headers().put(HEADER_DATE, ZonedDateTime.now().toString());
            response.headers().put(HEADER_SERVER, SERVER_NAME);
            response.headers().put(HEADER_CONNECTION, "close");
            response.headers()
                    .put(HEADER_CONTENT_TYPE,
                            APPLICATION_JSON + "; " + HEADER_CHARSET + "=" + UTF_8.name());
            response.headers()
                    .put(HEADER_CONTENT_LENGTH,
                            String.valueOf(response.body().getBytes(UTF_8).length));

            for (Map.Entry<String, String> header : response.headers().entrySet()) {
                writer.print(header.getKey() + ": " + header.getValue() + "\r\n");
            }

            writer.print("\r\n");
            writer.print(response.body());

            if (writer.checkError()) {
                System.err.println("Error in HTTP response writer");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            writer.flush();
        }
    }

    public void makeResponseHeaders(HttpRequest request,
            HttpResponse response) {

        response.headers().put(HEADER_DATE, ZonedDateTime.now().toString());
        response.headers().put(HEADER_SERVER, SERVER_NAME);
        response.headers()
                .put(HEADER_CONTENT_LENGTH, String.valueOf(response.body().getBytes(UTF_8).length));

        if (request != null && request.headers().containsKey(HEADER_CONNECTION.toLowerCase())) {
            String clientConn = request.headers().get(HEADER_CONNECTION.toLowerCase());
            response.headers().put(HEADER_CONNECTION, clientConn);

        } else {
            response.headers().put(HEADER_CONNECTION, CLOSE);
        }

        response.headers().put(HEADER_CONTENT_TYPE, APPLICATION_JSON + "; "
                + HEADER_CHARSET + "=" + UTF_8.name());

    }

}
