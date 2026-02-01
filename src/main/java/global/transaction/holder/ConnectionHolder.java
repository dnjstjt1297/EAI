package main.java.global.transaction.holder;

import java.sql.Connection;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ConnectionHolder {

    private final ThreadLocal<Connection> connectionHolder;

    public Connection getConnection() {
        return connectionHolder.get();
    }

    public void setConnection(Connection connection) {
        connectionHolder.set(connection);
    }

    public void removeConnection() {
        connectionHolder.remove();
    }

}
