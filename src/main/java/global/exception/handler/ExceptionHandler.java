package main.java.global.exception.handler;

import lombok.NoArgsConstructor;
import main.java.global.exception.RestApiException;
import main.java.global.exception.dto.ErrorCodeDto;
import main.java.global.exception.errorcode.enums.CommonErrorCode;

@NoArgsConstructor
public class ExceptionHandler {

    private static final String ERROR_KEY = "error";
    private static final String MESSAGE_MESSAGE = "message";

    public ErrorCodeDto handle(Exception e) {
        ErrorCodeDto errorCodeDto;
        String message;

        if (e instanceof RestApiException rae) {
            message = createErrorMessage(rae.getErrorCode().name(), rae.getMessage());
            errorCodeDto = new ErrorCodeDto(message, rae.getErrorCode().getStatus());

        } else if (e instanceof IllegalArgumentException) {
            message = createErrorMessage(CommonErrorCode.INVALID_PARAMETER.name(), e.getMessage());
            errorCodeDto = new ErrorCodeDto(message, CommonErrorCode.INVALID_PARAMETER.getStatus());

        } else {
            message = createErrorMessage(CommonErrorCode.INTERNAL_SERVER_ERROR.name(),
                    e.getMessage());
            errorCodeDto = new ErrorCodeDto(message,
                    CommonErrorCode.INTERNAL_SERVER_ERROR.getStatus());
        }
        return errorCodeDto;

    }

    public String createErrorMessage(String name, String message) {
        return "{\"" + ERROR_KEY + "\":\"" + name + "\", \"" + MESSAGE_MESSAGE + "\":\"" + message
                + "\"}";
    }
}
