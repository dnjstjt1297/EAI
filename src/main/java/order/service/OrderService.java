package main.java.order.service;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import main.java.global.exception.RestApiException;
import main.java.global.exception.errorcode.enums.CommonErrorCode;
import main.java.global.logging.annotation.LogExecution;
import main.java.global.properties.AppProperties;
import main.java.global.transaction.annotation.Transactional;
import main.java.order.dao.OrderDao;
import main.java.order.dto.OrderDto;
import main.java.order.dto.OrderMapper;
import main.java.order.dto.request.OrderRequest;
import main.java.order.dto.response.OrderResponse;
import main.java.order.sftp.sender.SftpSender;

@AllArgsConstructor
public class OrderService {

    private final String APPLICANT_KEY = "applicant.key";

    private final OrderDao orderDao;
    private final OrderMapper orderMapper;
    private final AppProperties properties;
    private final SftpSender sftpSender;

    @LogExecution
    @Transactional
    public OrderResponse order(OrderRequest orderRequest) {

        List<OrderDto> orderDtos = orderMapper.toOrderDtos(orderRequest);
        // DB 적재
        String applicantKey = Optional.ofNullable(properties.getProperty(APPLICANT_KEY))
                .filter(key -> !key.trim().isEmpty())
                .orElseThrow(
                        () -> new RestApiException(CommonErrorCode.INTERNAL_SERVER_ERROR));

        String lastOrderId = orderDao.findLastOrderId(applicantKey);
        orderDao.saveOrders(orderDtos, lastOrderId, applicantKey);

        sftpSender.upload(orderDtos, applicantKey, properties.getProperty("sftp.filepath"));

        return new OrderResponse("주문 API 성공");
    }


}
