package main.java.order.dto.request;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ORDER_DATA")
public class OrderRequest {

    @XmlElement(name = "HEADER")
    private List<Header> headers;

    @XmlElement(name = "ITEM")
    private List<Item> items;

    @Getter
    @Setter
    @ToString
    @XmlAccessorType(XmlAccessType.FIELD)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Header {

        @XmlElement(name = "USER_ID")
        private String userId;
        @XmlElement(name = "NAME")
        private String name;
        @XmlElement(name = "ADDRESS")
        private String address;
        @XmlElement(name = "STATUS")
        private String status;

    }

    @Getter
    @Setter
    @ToString
    @XmlAccessorType(XmlAccessType.FIELD)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {

        @XmlElement(name = "USER_ID")
        private String userId;
        @XmlElement(name = "ITEM_ID")
        private String itemId;
        @XmlElement(name = "ITEM_NAME")
        private String itemName;
        @XmlElement(name = "PRICE")
        private String price;
    }
}