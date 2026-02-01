package main.java.order.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import main.java.global.exception.RestApiException;
import main.java.global.exception.errorcode.enums.OrderErrorCode;
import main.java.global.transaction.holder.ConnectionHolder;
import main.java.order.dto.OrderDto;

@AllArgsConstructor
public class JdbcOrderDao implements OrderDao {

    private static final String ORDER_ID = "ORDER_ID";
    private static final String DEFAULT_ORDER_ID = "A000";

    private ConnectionHolder connectionHolder;


    @Override
    public void saveOrders(List<OrderDto> orderDtos, String lastOrderId, String applicantKey) {

        String sql = """
                INSERT INTO ORDER_TB (ORDER_ID, USER_ID, ITEM_ID, APPLICANT_KEY, NAME, ADDRESS, ITEM_NAME, PRICE, STATUS)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""";

        Connection connection = connectionHolder.getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (OrderDto orderDto : orderDtos) {
                lastOrderId = generateNextOrderId(lastOrderId);
                preparedStatement.setString(1, lastOrderId);
                preparedStatement.setString(2, orderDto.userId());
                preparedStatement.setString(3, orderDto.itemId());
                preparedStatement.setString(4, applicantKey);
                preparedStatement.setString(5, orderDto.name());
                preparedStatement.setString(6, orderDto.address());
                preparedStatement.setString(7, orderDto.itemName());
                preparedStatement.setString(8, orderDto.price());
                preparedStatement.setString(9, orderDto.status());
                preparedStatement.addBatch();
            }

            int[] results = preparedStatement.executeBatch();

            if (Arrays.stream(results).anyMatch(result -> result < 0)) {
                throw new RestApiException(OrderErrorCode.INSERT_DATABASE_ERROR);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public String findLastOrderId(String applicantKey) {

        String sql = "SELECT ORDER_ID FROM ORDER_TB WHERE APPLICANT_KEY = ? ORDER BY ORDER_ID DESC LIMIT 1";

        Connection connection = connectionHolder.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, applicantKey);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(ORDER_ID);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RestApiException(OrderErrorCode.NOTFOUND_LAST_ORDER_ID);
        }
    }

    public String generateNextOrderId(String lastOrderId) {
        if (lastOrderId == null) {
            return DEFAULT_ORDER_ID;
        }
        char prefix = lastOrderId.charAt(0);
        int number = Integer.parseInt(lastOrderId.substring(1)); // 001 -> 1

        if (number < 999) {
            number++;
        } else {
            if (prefix == 'Z') {
                throw new RestApiException(OrderErrorCode.NOTFOUND_LAST_ORDER_ID);
            }
            prefix++;
            number = 0;
        }

        return String.format("%c%03d", prefix, number);
    }
}
