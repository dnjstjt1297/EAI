package main.java.global.transaction.manager;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import main.java.global.exception.RestApiException;
import main.java.global.exception.errorcode.enums.TransactionErrorCode;
import main.java.global.transaction.holder.ConnectionHolder;

@AllArgsConstructor
@Getter
public class DataSourceTransactionManager implements TransactionManager {

    private final DataSource dataSource;
    private final ConnectionHolder connectionHolder;

    @Override
    public void doBegin() {
        try {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            connectionHolder.setConnection(connection);
        } catch (SQLException e) {
            throw new RestApiException(TransactionErrorCode.FAILED_BEGIN);
        }
    }

    @Override
    public void doCommit() {
        try {
            Connection connection = connectionHolder.getConnection();
            connection.commit();
        } catch (SQLException e) {
            throw new RestApiException(TransactionErrorCode.FAILED_COMMIT);
        }
    }

    @Override
    public void doRollback() {
        try {
            Connection connection = connectionHolder.getConnection();
            connection.rollback();
        } catch (SQLException e) {
            throw new RestApiException(TransactionErrorCode.FAILED_ROLLBACK);
        }
    }

    @Override
    public void doCleanUpAfterCompletion() {
        try {
            Connection connection = connectionHolder.getConnection();
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        } catch (SQLException e) {
            throw new RestApiException(TransactionErrorCode.FAILED_CLOSE);
        } finally {
            connectionHolder.removeConnection();
        }
    }
}
