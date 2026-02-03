package main.java.order.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import lombok.AllArgsConstructor;
import main.java.global.transaction.holder.ConnectionHolder;
import main.java.order.dto.OrderDto;

@AllArgsConstructor
public class JdbcShipmentDao implements ShipmentDao {

    private static final String SHIPMENT_ID = "SHIPMENT_ID";
    private static final String FULL_ID_ERROR = "SHIPMENT ID FULL";

    private static final String DEFAULT_SHIPMENT_ID = "A000";
    private ConnectionHolder connectionHolder;

    @Override
    public int[] saveShipment(List<OrderDto> orderDtos, String lastShipmentId,
            String applicantKey) {

        String sql = """
                INSERT INTO SHIPMENT_TB (SHIPMENT_ID, ORDER_ID, ITEM_ID, APPLICANT_KEY, ADDRESS)
                VALUES (?, ?, ?, ?, ?)""";

        Connection connection = connectionHolder.getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (OrderDto orderDto : orderDtos) {
                lastShipmentId = generateNextShipmentId(lastShipmentId);
                preparedStatement.setString(1, lastShipmentId);
                preparedStatement.setString(2, orderDto.orderId());
                preparedStatement.setString(3, orderDto.itemId());
                preparedStatement.setString(4, applicantKey);
                preparedStatement.setString(5, orderDto.address());
                preparedStatement.addBatch();
            }
            return preparedStatement.executeBatch();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String findLastShipmentId(String applicantKey) {
        String sql = "SELECT SHIPMENT_ID FROM SHIPMENT_TB WHERE APPLICANT_KEY = ? ORDER BY SHIPMENT_ID DESC LIMIT 1";

        Connection connection = connectionHolder.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, applicantKey);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(SHIPMENT_ID);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateNextShipmentId(String lastShipmentId) {

        if (lastShipmentId == null) {
            return DEFAULT_SHIPMENT_ID;
        }
        char prefix = lastShipmentId.charAt(0);
        int number = Integer.parseInt(lastShipmentId.substring(1)); // 001 -> 1

        if (number < 999) {
            number++;
        } else {
            if (prefix == 'Z') {
                throw new RuntimeException(FULL_ID_ERROR);
            }
            prefix++;
            number = 0;
        }

        return String.format("%c%03d", prefix, number);
    }
}
