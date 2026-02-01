package main.java.global.properties;

import java.io.InputStream;
import java.util.Properties;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AppProperties {

    private final Properties properties;

    public void init(String path) {
        try (InputStream inputStream = getClass().getResourceAsStream(path)) {
            properties.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

}
