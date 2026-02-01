package main.java.order.dao.datasource;

import javax.sql.DataSource;

public interface DataSourceFactory {

    DataSource createDataSource();
}
