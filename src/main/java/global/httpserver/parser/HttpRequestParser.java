package main.java.global.httpserver.parser;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import main.java.global.exception.RestApiException;
import main.java.global.exception.errorcode.enums.HttpServerErrorCode;
import main.java.global.httpserver.dto.request.HttpRequest;
import main.java.global.httpserver.enums.HttpMethod;

public class HttpRequestParser {

    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String VERSION = "1.1";

    public HttpRequest parse(InputStream inputStream) {
        try {
            String line = readLine(inputStream);
            if (line == null) {
                return null;
            }

            while (line.isEmpty()) { // 빈 라인 스킵
                line = readLine(inputStream);
                if (line == null) {
                    return null;
                }
            }

            String[] request = line.split("\\s+");
            if (request.length < 2) {
                throw new RestApiException(HttpServerErrorCode.INVALID_REQUEST);
            }

            HttpMethod method = HttpMethod.valueOf(request[0].toUpperCase());
            String url = request[1];
            String path = parsePath(url);
            Map<String, String> params = parseQueryParams(url);
            Map<String, String> headers = parseHeaders(inputStream);
            String body = parseBody(inputStream, headers);

            String version = (request.length >= 3) ? request[2].split("/")[1] : VERSION;

            return new HttpRequest(method, url, path, version, params, headers, body);

        } catch (Exception e) {
            if (e instanceof RestApiException) {
                throw (RestApiException) e;
            }
            return null;
        }
    }

    private String readLine(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\r') {
                continue;
            }
            if (b == '\n') {
                break;
            }
            sb.append((char) b);
        }
        if (b == -1 && sb.isEmpty()) {
            return null;
        }
        return sb.toString();
    }

    private String parsePath(String url) {
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

    private Map<String, String> parseHeaders(InputStream in) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = readLine(in)) != null && !line.isEmpty()) {
            String[] headerTokens = line.split(": ", 2);
            if (headerTokens.length == 2) {
                String key = headerTokens[0].trim().toLowerCase();
                String value = headerTokens[1].trim();
                headers.put(key, value);
            }
        }
        return headers;
    }

    private String parseBody(InputStream in, Map<String, String> headers) throws IOException {
        String lengthStr = headers.get(HEADER_CONTENT_LENGTH.toLowerCase());
        if (lengthStr == null || lengthStr.isEmpty()) {
            return "";
        }

        int length = Integer.parseInt(lengthStr);
        byte[] byteBuffer = new byte[length];
        int readSum = 0;

        while (readSum < length) {
            int read = in.read(byteBuffer, readSum, length - readSum);
            if (read == -1) {
                break;
            }
            readSum += read;
        }

        return new String(byteBuffer, 0, readSum, UTF_8);
    }
}