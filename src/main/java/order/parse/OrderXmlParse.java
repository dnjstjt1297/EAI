package main.java.order.parse;


import java.io.StringReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import main.java.global.exception.RestApiException;
import main.java.global.exception.errorcode.enums.OrderErrorCode;
import main.java.global.logging.annotation.LogExecution;
import main.java.order.dto.request.OrderRequest;
import org.xml.sax.SAXParseException;

public class OrderXmlParse {

    @LogExecution
    public OrderRequest parseOrderXml(String xml) {
        try {
            if (xml == null || xml.isEmpty()) {
                throw new RestApiException(OrderErrorCode.NOTFOUND_XML);
            }
            JAXBContext context = JAXBContext.newInstance(OrderRequest.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (OrderRequest) unmarshaller.unmarshal(new StringReader(xml));
        } catch (UnmarshalException e) {
            // xml 형식 오류
            throw new RestApiException(OrderErrorCode.INVALID_XML);

        } catch (JAXBException e) {
            if (e.getCause() instanceof SAXParseException) {
                // xml 문법 오류
                throw new RestApiException(OrderErrorCode.INVALID_XML);

            }
            // 그 외 오류
            throw new RestApiException(OrderErrorCode.FAILED_PARSE_XML);
        }
    }
}