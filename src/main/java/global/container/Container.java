package main.java.global.container;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import main.java.global.exception.handler.RestApiExceptionHandler;
import main.java.global.httpserver.FrontController;
import main.java.global.httpserver.handler.HandlerAdaptor;
import main.java.global.httpserver.handler.HandlerMapping;
import main.java.global.httpserver.interceptor.HandlerInterceptor;
import main.java.global.httpserver.interceptor.ValidationInterceptor;
import main.java.global.httpserver.parser.HttpRequestParser;
import main.java.global.httpserver.sender.HttpResponseSender;
import main.java.global.properties.AppProperties;
import main.java.global.transaction.TransactionProxyGenerator;
import main.java.global.transaction.holder.ConnectionHolder;
import main.java.global.transaction.interceptor.TransactionInterceptor;
import main.java.global.transaction.manager.DataSourceTransactionManager;
import main.java.global.transaction.manager.TransactionManager;
import main.java.order.controller.OrderController;
import main.java.order.dao.JdbcOrderDao;
import main.java.order.dao.OrderDao;
import main.java.order.dao.datasource.DataSourceFactory;
import main.java.order.dao.datasource.HikariDataSourceFactory;
import main.java.order.dto.OrderMapper;
import main.java.order.parse.OrderXmlParse;
import main.java.order.service.OrderService;
import main.java.order.sftp.client.JSchSftpClient;
import main.java.order.sftp.client.SftpClient;
import main.java.order.sftp.sender.JSchSftpSender;
import main.java.order.sftp.sender.SftpSender;

/**
 * 의존성 관리 컨테이너.
 */

public class Container {

    // 빈처럼 객체 관리
    private static final Map<String, Object> beanMap = new HashMap<>();

    static {
        // properties
        AppProperties appProperties = new AppProperties(new Properties());
        registerBean("appProperties", appProperties);

        // datasource
        DataSourceFactory dataSourceFactory = new HikariDataSourceFactory(appProperties);
        DataSource dataSource = dataSourceFactory.createDataSource();

        // transaction
        ConnectionHolder connectionHolder = new ConnectionHolder(new ThreadLocal<>());
        registerBean("connectionHolder", connectionHolder);
        TransactionManager transactionManager = new DataSourceTransactionManager(dataSource,
                connectionHolder);
        registerBean("transactionManager", transactionManager);

        // dao
        OrderDao orderDao = new JdbcOrderDao(connectionHolder);
        registerBean("orderDao", orderDao);
        OrderMapper orderMapper = new OrderMapper();
        registerBean("orderMapper", orderMapper);

        // sftp
        SftpClient sftpClient = new JSchSftpClient();
        SftpSender sftpSender = new JSchSftpSender(sftpClient, orderMapper, appProperties);

        // order service
        OrderService orderService = new OrderService(orderDao, orderMapper, appProperties,
                sftpSender);
        OrderService orderServiceProxy = TransactionProxyGenerator.getProxy(OrderService.class,
                new TransactionInterceptor(orderService, transactionManager));
        registerBean("orderService", orderServiceProxy);

        // order controller
        OrderXmlParse orderXmlParse = new OrderXmlParse();
        registerBean("orderXmlParse", orderXmlParse);
        OrderController orderController = new OrderController(orderServiceProxy, orderXmlParse);
        registerBean("orderController", orderController);

        // objectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        registerBean("objectMapper", objectMapper);

        // exception
        RestApiExceptionHandler restApiExceptionHandler = new RestApiExceptionHandler();
        registerBean("restApiExceptionHandler", restApiExceptionHandler);

        // interceptor 관련
        List<HandlerInterceptor> interceptorList = new ArrayList<>();
        interceptorList.add(new ValidationInterceptor());
        registerBean("interceptorList", interceptorList);

        // httpServer 관련
        HttpRequestParser httpRequestParser = new HttpRequestParser();
        registerBean("httpRequestParser", httpRequestParser);

        HttpResponseSender httpResponseSender = new HttpResponseSender();
        registerBean("httpResponseSender", httpResponseSender);

        HandlerMapping handlerMapping = new HandlerMapping(new HashMap<>(), beanMap);
        registerBean("handlerMapping", handlerMapping);

        HandlerAdaptor handlerAdaptor = new HandlerAdaptor();
        registerBean("handlerAdaptor", handlerAdaptor);

        FrontController frontController = new FrontController(restApiExceptionHandler,
                interceptorList, httpResponseSender, handlerMapping, handlerAdaptor);
        registerBean("frontController", frontController);

    }


    public static <T> T getBean(String beanId, Class<T> clazz) {
        return clazz.cast(beanMap.get(beanId));
    }

    public static void registerBean(String beanId, Object bean) {
        beanMap.put(beanId, bean);
    }

    public static Map<String, Object> getAllBean() {
        return beanMap;
    }
}
