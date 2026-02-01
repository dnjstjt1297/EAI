package main.java.order.dto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import main.java.order.dto.request.OrderRequest;
import main.java.order.dto.request.OrderRequest.Header;

public class OrderMapper {

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

    public List<OrderDto> toOrderDtos(OrderRequest orderRequest) {

        Map<String, Header> headerMap = orderRequest.getHeaders().stream()
                .collect(Collectors.toMap(
                        Header::getUserId,
                        header -> header,
                        (existing, replacement) -> {
                            System.err.println("중복 헤더 발견: " + existing.getUserId());
                            return existing;
                        }
                ));

        return orderRequest.getItems().stream()
                .filter(item -> {
                    boolean exists = headerMap.containsKey(item.getUserId());
                    if (!exists) {
                        //todo 로그
                        System.err.println("주문자를 찾을 수 없습니다.: " + item.getUserId() + "는 스킵 되었습니다.");
                    }
                    return exists;
                })
                .map(item -> {
                    Header header = headerMap.get(item.getUserId());
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
}
