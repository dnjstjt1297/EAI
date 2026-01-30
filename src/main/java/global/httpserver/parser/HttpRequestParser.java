package main.java.global.httpserver.parser;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import main.java.global.exception.RestApiException;
import main.java.global.exception.errorcode.enums.HttpServerErrorCode;
import main.java.global.httpserver.dto.request.HttpRequest;
import main.java.global.httpserver.enums.HttpMethod;

/**
 * Request를 HTTP 1.1 프로토콜 규격에 맞게 파싱하는 클래스입니다. (단 URL의 path variable은 제외하였습니다.)
 */
public class HttpRequestParser {

    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String VERSION = "1.1";

    public HttpRequest parse(InputStream inputStream) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, UTF_8));

            String line = reader.readLine();
            while (line != null && line.isEmpty()) {
                line = reader.readLine();
            }
            if (line == null) {
                throw new RestApiException(HttpServerErrorCode.NOTFOUND_REQUEST);
            }

            String[] request = line.split("\\s+");

            if (request.length < 2) {
                throw new RestApiException(HttpServerErrorCode.INVALID_REQUEST);
            }

            HttpMethod method = HttpMethod.valueOf(request[0].toUpperCase());
            String url = request[1];
            String path = parsePath(url);
            Map<String, String> params = parseQueryParams(url);
            Map<String, String> headers = parseHeaders(reader);
            String body = parseBody(reader, headers);
            String version = (request.length >= 3) ? request[2].split("/")[1] : VERSION;

            return new HttpRequest(method, url, path, version, params, headers, body);

        } catch (Exception e) {
            throw new RestApiException(HttpServerErrorCode.INVALID_REQUEST);
        }
    }

    private String parsePath(String url) {
        if (url == null || url.isEmpty()) {
            return "/";
        }

        try {
            URI uri = new URI(url);
            String path = uri.getPath();

            if (path == null || path.isEmpty()) {
                return "/";
            }

            path = path.replaceAll("//+", "/");
            if (path.length() > 1 && path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            return path;
        } catch (Exception e) {
            int queryIndex = url.indexOf('?');
            String path = (queryIndex != -1) ? url.substring(0, queryIndex) : url;
            return path.startsWith("/") ? path : "/" + path;
        }
    }

    private Map<String, String> parseQueryParams(String url) {
        int queryIdx = url.indexOf('?');
        if (queryIdx == -1) {
            return Collections.emptyMap();
        }

        Map<String, String> params = new HashMap<>();
        String queryString = url.substring(queryIdx + 1);
        for (String pair : queryString.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                params.put(kv[0], URLDecoder.decode(kv[1], UTF_8));
            }
        }
        return params;
    }

    private Map<String, String> parseHeaders(BufferedReader reader) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            String[] headerTokens = line.split(": ", 2);
            if (headerTokens.length == 2) {
                String key = headerTokens[0].trim().toLowerCase();
                String value = headerTokens[1].trim();
                headers.put(key, value);
            }
        }
        return headers;
    }

    private String parseBody(BufferedReader reader, Map<String, String> headers)
            throws IOException {
        String lengthStr = headers.get(HEADER_CONTENT_LENGTH);
        if (lengthStr == null) {
            return "";
        }

        int length = Integer.parseInt(lengthStr);
        char[] buffer = new char[length];
        int readSum = 0;
        while (readSum < length) {
            int read = reader.read(buffer, readSum, length - readSum);
            if (read == -1) {
                break;
            }
            readSum += read;
        }
        return new String(buffer, 0, readSum);
    }

}
