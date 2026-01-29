package main.java.global.exception;

import lombok.Getter;
import main.java.global.exception.errorcode.ErrorCode;

@Getter
public class RestApiException extends RuntimeException {

    private final ErrorCode errorCode;

    public RestApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
