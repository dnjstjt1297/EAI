package main.java.order.dto;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import main.java.global.exception.RestApiException;
import main.java.global.exception.errorcode.enums.OrderErrorCode;
import main.java.global.logging.LogContext;
import main.java.global.logging.annotation.LogExecution;
import main.java.order.dto.request.OrderRequest;
import main.java.order.dto.request.OrderRequest.Header;
import main.java.order.dto.request.OrderRequest.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
public class OrderMapper {

    private final LogContext logContext;
    private static final Logger log = LoggerFactory.getLogger(OrderMapper.class);

    public String listToFlatFormat(List<OrderDto> orderDtos, String applicationKey) {
        StringBuilder sb = new StringBuilder();
        for (OrderDto dto : orderDtos) {
            sb.append(dto.userId()).append("^")
                    .append(dto.itemId()).append("^")
                    .append(applicationKey).append("^")
                    .append(dto.name()).append("^")
                    .append(dto.address()).append("^")
                    .append(dto.itemName()).append("^")
                    .append(dto.price()).append("\n");
        }
        return sb.toString();
    }

    @LogExecution
    public List<OrderDto> toOrderDtos(OrderRequest orderRequest) {

        Map<String, Header> headerMap = orderRequest.getHeaders().stream()
                .collect(Collectors.toMap(
                        Header::getUserId,
                        header -> header,
                        (existing, replacement) -> {
                            throw new RestApiException(OrderErrorCode.DUPLICATED_HEADER_USER_ID);
                        }
                ));

        return orderRequest.getItems().stream()
                .filter(item -> {
                    boolean exists = headerMap.containsKey(item.getUserId());
                    if (!exists) {
                        log.warn("{} [WARM] 존재하지 않는 주문자, {}의 {} 스킵",
                                logContext.getIndent(), item.getUserId(), item.getItemId());
                    }
                    return exists;
                })
                .map(item -> {
                    Header header = headerMap.get(item.getUserId());
                    validateItem(header, item);

                    return new OrderDto(
                            null,
                            item.getUserId(),
                            item.getItemId(),
                            header.getName(),
                            header.getAddress(),
                            item.getItemName(),
                            item.getPrice(),
                            header.getStatus()
                    );
                })
                .collect(Collectors.toList());
    }

    private void validateItem(Header header, Item item) {
        if (!header.getStatus().equals(OrderStatus.N.name())) {
            throw new RestApiException(OrderErrorCode.INVALID_STATUS);
        }
        try {
            int price = Integer.parseInt(item.getPrice());
            if (price <= 0) {
                throw new RestApiException(OrderErrorCode.INVALID_PRICE);
            }
        } catch (NumberFormatException e) {
            throw new RestApiException(OrderErrorCode.INVALID_PRICE);
        }
    }
}
