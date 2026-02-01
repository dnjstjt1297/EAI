package main.java.global.exception.errorcode.enums;


import lombok.AllArgsConstructor;
import main.java.global.exception.errorcode.ErrorCode;
import main.java.global.httpserver.enums.HttpStatus;

@AllArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    INVALID_XML(HttpStatus.BAD_REQUEST, "주문 XML이 형식이 맞지 않습니다."),
    NOTFOUND_XML(HttpStatus.NOT_FOUND, "주문 XML이 존재하지 않습니다."),
    FAILED_PARSE_XML(HttpStatus.INTERNAL_SERVER_ERROR, "주문 XML 파싱을 실패했습니다."),
    INSERT_DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "주문 API DB 저장에 실패했습니다."),
    NOTFOUND_LAST_ORDER_ID(HttpStatus.NOT_FOUND, "주문 테이블의 마지막 기본 키 조회에 실패했습니다."),
    FAILED_GENERATED_ID(HttpStatus.INTERNAL_SERVER_ERROR, "주문 키 생성에 실패했습니다"),

    FAILED_SFTP_BYTE(HttpStatus.INTERNAL_SERVER_ERROR, "바이트 파일로 변환에 실패하였습니다."),
    FAILED_SFTP_LOAD(HttpStatus.INTERNAL_SERVER_ERROR, "파일 전송에 실패하였습니다."),
    FAILED_SFTP_CONNECT(HttpStatus.INTERNAL_SERVER_ERROR, "SFTP 서버 연결에 실패했습니다.");

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
