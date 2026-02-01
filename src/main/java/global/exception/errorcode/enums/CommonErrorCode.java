package main.java.global.exception.errorcode.enums;

import lombok.AllArgsConstructor;
import main.java.global.exception.errorcode.ErrorCode;
import main.java.global.httpserver.enums.HttpStatus;

@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {


    INVALID_HEADER(HttpStatus.BAD_REQUEST, "헤더 값이 올바르지 않습니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "잘못된 파라미터 입니다."),
    NOTFOUND_RESOURCE(HttpStatus.NOT_FOUND, "자원이 존재하지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 에러입니다."),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "에러 메시지 생성 실패했습니다.");

    private final HttpStatus status;
    private final String message;


    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;

    }
}
