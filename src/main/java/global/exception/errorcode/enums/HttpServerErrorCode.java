package main.java.global.exception.errorcode.enums;

import main.java.global.exception.errorcode.ErrorCode;
import main.java.global.httpserver.enums.HttpStatus;

public enum HttpServerErrorCode implements ErrorCode {
    NOTFOUND_REQUEST(HttpStatus.NOT_FOUND, "요청을 찾을 수 없습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "올바르지 않은 요청입니다."),
    INVALID_METHOD(HttpStatus.METHOD_NOT_ALLOWED, "메서드가 존재하지 않습니다."),
    NOTFOUND_HANDLER(HttpStatus.NOT_FOUND, "요청을 처리할 핸들러를 찾을 수 없습니다."),
    INVALID_INTERCEPTOR(HttpStatus.BAD_REQUEST, "인터셉터에 의해 종료되었습니다.");

    private final HttpStatus status;
    private final String message;

    HttpServerErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
