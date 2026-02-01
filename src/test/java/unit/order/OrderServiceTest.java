package test.java.unit.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.util.List;
import main.java.global.exception.RestApiException;
import main.java.global.exception.errorcode.enums.CommonErrorCode;
import main.java.global.properties.AppProperties;
import main.java.order.dao.OrderDao;
import main.java.order.dto.OrderDto;
import main.java.order.dto.OrderMapper;
import main.java.order.dto.request.OrderRequest;
import main.java.order.dto.response.OrderResponse;
import main.java.order.service.OrderService;
import main.java.order.sftp.sender.SftpSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderDao orderDao;
    @Mock
    OrderMapper orderMapper;
    @Mock
    AppProperties properties;
    @Mock
    SftpSender sftpSender;
    @InjectMocks
    OrderService orderService;

    @Test
    @DisplayName("유효한 요청과 키가 있을 때 주문을 저장할 수 있다.")
    void orderSuccess() {
        // given
        OrderRequest request = new OrderRequest();
        request.setHeaders(List.of(new OrderRequest.Header("USER1", "TEST_NAME", "TEST_ADD", "N")));

        request.setItems(List.of(new OrderRequest.Item("USER1", "ITEM1", "TEST_NAME", "10000")));
        List<OrderDto> dtos = List.of(mock(OrderDto.class));
        String validKey = "TEST_APP_KEY";
        String lastOrderId = "A000";

        given(orderDao.findLastOrderId(validKey)).willReturn(lastOrderId);
        given(orderMapper.toOrderDtos(request)).willReturn(dtos);
        given(properties.getProperty("applicant.key")).willReturn(validKey);
        given(properties.getProperty("sftp.filepath")).willReturn("test/path");

        // when
        OrderResponse response = orderService.order(request);

        // then
        assertEquals("주문 API 성공", response.message());
        then(orderDao).should(times(1)).saveOrders(dtos, lastOrderId, validKey);
    }

    @Test
    @DisplayName("APPLICANT_KEY가 없으면 예외가 발생할 수 있다.")
    void orderFailNoAppKey() {
        // given
        OrderRequest request = new OrderRequest();
        request.setHeaders(List.of(new OrderRequest.Header("USER1", "TEST_NAME", "TEST_ADD", "N")));
        request.setItems(List.of(new OrderRequest.Item("USER1", "ITEM1", "TEST_NAME", "10000")));

        given(orderMapper.toOrderDtos(request)).willReturn(List.of());
        given(properties.getProperty("applicant.key")).willReturn(null);

        // when & then
        RestApiException exception = assertThrows(RestApiException.class, () ->
                orderService.order(request)
        );
        assertEquals(CommonErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
        then(orderDao).shouldHaveNoInteractions();
    }
}
