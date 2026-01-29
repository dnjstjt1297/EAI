package test.unit.exception.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import main.java.global.exception.RestApiException;
import main.java.global.exception.dto.ErrorCodeDto;
import main.java.global.exception.errorcode.enums.CommonErrorCode;
import main.java.global.exception.errorcode.enums.TcpErrorCode;
import main.java.global.exception.handler.RestApiExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ExceptionHandlerUnitTest {

    RestApiExceptionHandler restApiExceptionHandler = new RestApiExceptionHandler();

    @Test
    @DisplayName("RestApiException 발생 시 설정된 에러 코드와 상태 코드를 반환해야 한다")
    void handleRestApiException() throws Exception {
        // given
        Exception e = new RestApiException(TcpErrorCode.INVALID_REQUEST);

        // when
        ErrorCodeDto result = restApiExceptionHandler.handle(e);

        // then
        assertEquals(TcpErrorCode.INVALID_REQUEST.getStatus(), result.status());
        assertTrue(result.message().contains(TcpErrorCode.INVALID_REQUEST.getMessage()));
    }

    @Test
    @DisplayName("IllegalArgumentException 발생 시: INVALID_PARAMETER 코드를 반환해야 한다")
    void handleIllegalArgumentException() throws Exception {
        // given
        Exception e = new IllegalArgumentException("잘못된 인자");

        // when
        ErrorCodeDto result = restApiExceptionHandler.handle(e);

        // then
        assertEquals(CommonErrorCode.INVALID_PARAMETER.getStatus(), result.status());
    }

    @Test
    @DisplayName("3. 기타 일반 Exception 발생 시: INTERNAL_SERVER_ERROR를 반환해야 한다")
    void handleGeneralException() throws Exception {
        // given
        Exception e = new RuntimeException("DB 서버 다운");

        // when
        ErrorCodeDto result = restApiExceptionHandler.handle(e);

        // then
        assertEquals(CommonErrorCode.INTERNAL_SERVER_ERROR.getStatus(), result.status());
    }
}
