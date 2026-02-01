package main.java.order.dao;

import java.util.List;
import main.java.order.dto.OrderDto;

public interface OrderDao {

    void saveOrders(List<OrderDto> orderDto, String lastOrderId, String applicantKey);

    String findLastOrderId(String applicantKey);
}
