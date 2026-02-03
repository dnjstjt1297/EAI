package main.java.order.dao;

import java.util.List;
import main.java.order.dto.OrderDto;

public interface OrderDao {

    int[] saveOrders(List<OrderDto> orderDto, String lastOrderId, String applicantKey);

    String findLastOrderId(String applicantKey);

    List<OrderDto> findOrdersWithStatus(String applicantKey, String status);

    int[] updateOrderStatus(List<OrderDto> orderDtos, String applicationKey, String status);

}
