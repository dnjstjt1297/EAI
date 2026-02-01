package main.java.order.dto;

public record OrderDto(
        String orderId,
        String userId,
        String itemId,
        String name,
        String address,
        String itemName,
        String price,
        String status
) {

}
