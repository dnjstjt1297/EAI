package main.java.global.exception.errorcode;

import main.java.global.httpserver.enums.HttpStatus;

public interface ErrorCode {

    String name();

    HttpStatus getStatus();

    String getMessage();
}
