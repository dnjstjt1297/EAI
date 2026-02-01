package test.java.unit.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import java.util.Properties;
import main.java.global.properties.AppProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AppPropertiesTest {

    @Mock
    Properties properties;

    @InjectMocks
    AppProperties appProperties;


    @Test
    @DisplayName("프로퍼티 Key값에 맞는 Value를 가져올 수 있다.")
    void getPropertyTest() {
        // given
        String key = "db.url";
        String url = "jdbc:mysql://localhost:3306/test";
        given(appProperties.getProperty(key)).willReturn(url);

        // When
        appProperties.getProperty(key);

        // Then
        assertEquals(url, properties.getProperty(key));
    }
}
