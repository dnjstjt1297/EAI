package main.java.order.sftp.sender;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import main.java.global.exception.RestApiException;
import main.java.global.exception.errorcode.enums.CommonErrorCode;
import main.java.global.exception.errorcode.enums.OrderErrorCode;
import main.java.global.logging.annotation.LogExecution;
import main.java.global.properties.AppProperties;
import main.java.order.dto.OrderDto;
import main.java.order.dto.OrderMapper;
import main.java.order.sftp.client.SftpClient;

@AllArgsConstructor
public class JSchSftpSender implements SftpSender {

    private static final String FILE_NAME = "INSPIEN_김원석_%s.txt";
    private static final String DATE_FORMAT = "yyyyMMddHHmmss";

    private final SftpClient sftpClient;
    private final OrderMapper orderMapper;
    private final AppProperties properties;

    @Override
    @LogExecution
    public void upload(List<OrderDto> orderDtos, String applicationKey, String filepath) {

        validateProperties();
        sftpClient.connect(properties.getProperty("sftp.host"),
                Integer.parseInt(properties.getProperty("sftp.port")),
                properties.getProperty("sftp.username"),
                properties.getProperty("sftp.password"));

        String flatFormat = orderMapper.listToFlatFormat(orderDtos, applicationKey);
        String fileName = String.format(FILE_NAME,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)));

        byte[] contentBytes = flatFormat.getBytes(StandardCharsets.UTF_8);

        try (InputStream inputStream = new ByteArrayInputStream(contentBytes)) {
            sftpClient.load(filepath, inputStream, fileName);

        } catch (IOException e) {
            throw new RestApiException(OrderErrorCode.FAILED_SFTP_BYTE);
        } finally {
            sftpClient.close();
        }

    }

    private void validateProperties() {

        Optional.ofNullable(properties.getProperty("sftp.host")).orElseThrow(
                () -> new RestApiException(CommonErrorCode.INTERNAL_SERVER_ERROR));

        String port = Optional.ofNullable(properties.getProperty("sftp.port"))
                .map(String::trim)
                .orElseThrow(() -> new RestApiException(CommonErrorCode.INTERNAL_SERVER_ERROR));

        if (!port.matches("\\d+")) {
            throw new RestApiException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }

        Optional.ofNullable(properties.getProperty("sftp.username")).orElseThrow(
                () -> new RestApiException(CommonErrorCode.INTERNAL_SERVER_ERROR));
        Optional.ofNullable(properties.getProperty("sftp.password")).orElseThrow(
                () -> new RestApiException(CommonErrorCode.INTERNAL_SERVER_ERROR));
    }

}
