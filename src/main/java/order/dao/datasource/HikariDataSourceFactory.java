package main.java.order.dao.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import main.java.global.exception.RestApiException;
import main.java.global.exception.errorcode.enums.CommonErrorCode;
import main.java.global.properties.AppProperties;

@AllArgsConstructor
public class HikariDataSourceFactory implements DataSourceFactory {

    private final AppProperties properties;
    private static final String propertyPath = "/main/resources/application.properties";

    @Override
    public DataSource createDataSource() {
        properties.init(propertyPath);
        validateProperties();
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(properties.getProperty("db.url"));
        hikariConfig.setUsername(properties.getProperty("db.username"));
        hikariConfig.setPassword(properties.getProperty("db.password"));
        hikariConfig.setDriverClassName(properties.getProperty("db.driver"));
        return new HikariDataSource(hikariConfig);
    }

    private void validateProperties() {
        Optional.ofNullable(properties.getProperty("db.url")).orElseThrow(() ->
                new RestApiException(CommonErrorCode.INTERNAL_SERVER_ERROR)
        );
        Optional.ofNullable(properties.getProperty("db.username")).orElseThrow(() ->
                new RestApiException(CommonErrorCode.INTERNAL_SERVER_ERROR)
        );
        Optional.ofNullable(properties.getProperty("db.password")).orElseThrow(() ->
                new RestApiException(CommonErrorCode.INTERNAL_SERVER_ERROR)
        );
        Optional.ofNullable(properties.getProperty("db.driver")).orElseThrow(() ->
                new RestApiException(CommonErrorCode.INTERNAL_SERVER_ERROR)
        );
    }
}
