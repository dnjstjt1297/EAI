package main.java.order.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import main.java.global.logging.annotation.LogExecution;
import main.java.global.transaction.holder.ConnectionHolder;
import main.java.order.dto.OrderDto;

@AllArgsConstructor
public class JdbcOrderDao implements OrderDao {

    private static final String ORDER_ID = "ORDER_ID";
    private static final String USER_ID = "USER_ID";
    private static final String ITEM_ID = "ITEM_ID";
    private static final String NAME = "NAME";
    private static final String ADDRESS = "ADDRESS";
    private static final String ITEM_NAME = "ITEM_NAME";
    private static final String PRICE = "PRICE";
    private static final String STATUS = "STATUS";
    private static final String FULL_ID_ERROR = "ORDER ID FULL";


    private static final String DEFAULT_ORDER_ID = "A000";

    private ConnectionHolder connectionHolder;


    @Override
    @LogExecution
    public int[] saveOrders(List<OrderDto> orderDtos, String lastOrderId, String applicantKey) {

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

            return preparedStatement.executeBatch();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @LogExecution
    public String findLastOrderId(String applicantKey) {

        String sql = "SELECT ORDER_ID FROM ORDER_TB WHERE APPLICANT_KEY = ? ORDER BY ORDER_ID DESC FETCH FIRST 1 ROWS ONLY";

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
            throw new RuntimeException(e);
        }
    }

    @Override
    @LogExecution
    public List<OrderDto> findOrdersWithStatus(String applicantKey, String status) {

        String sql = "SELECT * FROM ORDER_TB WHERE APPLICANT_KEY = ? AND STATUS = ?";
        Connection connection = connectionHolder.getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, applicantKey);
            preparedStatement.setString(2, status);
            List<OrderDto> orders = new ArrayList<>();

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    orders.add(new OrderDto(
                            rs.getString(ORDER_ID),
                            rs.getString(USER_ID),
                            rs.getString(ITEM_ID),
                            rs.getString(NAME),
                            rs.getString(ADDRESS),
                            rs.getString(ITEM_NAME),
                            rs.getString(PRICE),
                            rs.getString(STATUS)
                    ));
                }
            }
            return orders;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int[] updateOrderStatus(List<OrderDto> orderDtos, String applicantKey, String status) {
        String sql = "UPDATE ORDER_TB SET STATUS = ? WHERE ORDER_ID = ? AND APPLICANT_KEY = ?";

        Connection connection = connectionHolder.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            for (OrderDto dto : orderDtos) {
                preparedStatement.setString(1, status);
                preparedStatement.setString(2, dto.orderId());
                preparedStatement.setString(3, applicantKey);
                preparedStatement.addBatch();
            }
            return preparedStatement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
                throw new RuntimeException(FULL_ID_ERROR);
            }
            prefix++;
            number = 0;
        }

        return String.format("%c%03d", prefix, number);
    }


}
