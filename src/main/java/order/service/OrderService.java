package main.java.order.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import main.java.global.exception.RestApiException;
import main.java.global.exception.errorcode.enums.CommonErrorCode;
import main.java.global.exception.errorcode.enums.OrderErrorCode;
import main.java.global.logging.annotation.LogExecution;
import main.java.global.properties.AppProperties;
import main.java.global.transaction.annotation.Transactional;
import main.java.order.dao.OrderDao;
import main.java.order.dao.ShipmentDao;
import main.java.order.dto.OrderDto;
import main.java.order.dto.OrderMapper;
import main.java.order.dto.OrderStatus;
import main.java.order.dto.request.OrderRequest;
import main.java.order.dto.response.OrderResponse;
import main.java.order.sftp.sender.SftpSender;

@AllArgsConstructor
public class OrderService {

    private final String APPLICANT_KEY = "applicant.key";

    private final OrderDao orderDao;
    private final ShipmentDao shipmentDao;
    private final OrderMapper orderMapper;
    private final AppProperties properties;
    private final SftpSender sftpSender;

    @LogExecution
    @Transactional
    public OrderResponse order(OrderRequest orderRequest) {

        List<OrderDto> orderDtos = orderMapper.toOrderDtos(orderRequest);
        // DB 적재
        String applicantKey = findApplicationKey();

        String lastOrderId = orderDao.findLastOrderId(applicantKey);

        int[] saveOrders = orderDao.saveOrders(orderDtos, lastOrderId, applicantKey);
        validateResult(saveOrders);

        sftpSender.upload(orderDtos, applicantKey, properties.getProperty("sftp.filepath"));

        return new OrderResponse("주문 API 성공");
    }

    @LogExecution
    @Transactional
    public void shipmentUpload() {
        String applicantKey = findApplicationKey();

        List<OrderDto> orderDtos = orderDao.findOrdersWithStatus(applicantKey,
                OrderStatus.N.name());

        String lastShipmentId = shipmentDao.findLastShipmentId(applicantKey);

        int[] saveShipments = shipmentDao.saveShipment(orderDtos, lastShipmentId, applicantKey);
        validateResult(saveShipments);

        int[] updateOrders = orderDao.updateOrderStatus(orderDtos, applicantKey,
                OrderStatus.Y.name());
        validateResult(updateOrders);
    }

    private String findApplicationKey() {
        return Optional.ofNullable(properties.getProperty(APPLICANT_KEY))
                .filter(key -> !key.trim().isEmpty())
                .orElseThrow(
                        () -> new RestApiException(CommonErrorCode.INTERNAL_SERVER_ERROR));
    }

    private void validateResult(int[] results) {
        if (Arrays.stream(results).anyMatch(result -> result < 0)) {
            throw new RestApiException(OrderErrorCode.INSERT_DATABASE_ERROR);
        }
    }


}
