package main.java.order.sftp.sender;

import java.util.List;
import main.java.order.dto.OrderDto;

public interface SftpSender {

    void upload(List<OrderDto> orderDtos, String applicantKey, String filepath);
}
