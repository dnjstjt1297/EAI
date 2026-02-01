package main.java.global.exception.errorcode.enums;


import lombok.AllArgsConstructor;
import main.java.global.exception.errorcode.ErrorCode;
import main.java.global.httpserver.enums.HttpStatus;

@AllArgsConstructor
public enum TransactionErrorCode implements ErrorCode {

    FAILED_BEGIN(HttpStatus.INTERNAL_SERVER_ERROR, "트랜잭션을 위한 커넥션을 열기에 실패하였습니다."),
    FAILED_COMMIT(HttpStatus.INTERNAL_SERVER_ERROR, "트랜잭션 커밋에 실패하였습니다."),
    FAILED_ROLLBACK(HttpStatus.INTERNAL_SERVER_ERROR, "트랜잭션 롤백에 실패하였습니다."),
    FAILED_CLOSE(HttpStatus.INTERNAL_SERVER_ERROR, "트랜잭션을 위한 커넥션 닫기에 실패했습니다.");

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
