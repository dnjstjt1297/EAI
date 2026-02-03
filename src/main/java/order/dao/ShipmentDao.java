package main.java.order.dao;

import java.util.List;
import main.java.order.dto.OrderDto;

public interface ShipmentDao {

    int[] saveShipment(List<OrderDto> orderDtos, String lastShipmentId, String applicantKey);

    String findLastShipmentId(String applicantKey);

}
