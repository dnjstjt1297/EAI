package main.java.global.container;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import main.java.global.exception.handler.RestApiExceptionHandler;
import main.java.global.exception.handler.SchedulerExceptionHandler;
import main.java.global.httpserver.FrontController;
import main.java.global.httpserver.connection.Http11Connection;
import main.java.global.httpserver.connection.HttpConnection;
import main.java.global.httpserver.dto.MappingInfo;
import main.java.global.httpserver.frontinterceptor.FrontInterceptor;
import main.java.global.httpserver.frontinterceptor.LoggingFrontInterceptor;
import main.java.global.httpserver.frontinterceptor.ValidationFrontInterceptor;
import main.java.global.httpserver.handler.HandlerAdaptor;
import main.java.global.httpserver.handler.HandlerMapping;
import main.java.global.httpserver.handler.HandlerMethod;
import main.java.global.httpserver.parser.HttpRequestParser;
import main.java.global.httpserver.sender.HttpResponseSender;
import main.java.global.logging.annotation.LogExecution;
import main.java.global.logging.interceptor.LoggingInterceptor;
import main.java.global.properties.AppProperties;
import main.java.global.proxy.ProxyGenerator;
import main.java.global.transaction.annotation.Transactional;
import main.java.global.transaction.holder.ConnectionHolder;
import main.java.global.transaction.interceptor.TransactionInterceptor;
import main.java.global.transaction.manager.DataSourceTransactionManager;
import main.java.global.transaction.manager.TransactionManager;
import main.java.order.controller.OrderController;
import main.java.order.dao.JdbcOrderDao;
import main.java.order.dao.JdbcShipmentDao;
import main.java.order.dao.OrderDao;
import main.java.order.dao.ShipmentDao;
import main.java.order.dao.datasource.DataSourceFactory;
import main.java.order.dao.datasource.HikariDataSourceFactory;
import main.java.order.dto.OrderMapper;
import main.java.order.parse.OrderXmlParse;
import main.java.order.scheduler.OrderJob;
import main.java.order.scheduler.OrderJobFactory;
import main.java.order.scheduler.service.OrderSchedulerService;
import main.java.order.scheduler.service.QuartzOrderSchedulerService;
import main.java.order.service.OrderService;
import main.java.order.sftp.client.JSchSftpClient;
import main.java.order.sftp.client.SftpClient;
import main.java.order.sftp.sender.JSchSftpSender;
import main.java.order.sftp.sender.SftpSender;

/**
 * 의존성 관리 컨테이너.
 */

@Slf4j
public class Container {

    @Getter
    private static final Map<String, Object> beanMap = new HashMap<>();

    @Getter
    private static final Map<MappingInfo, HandlerMethod> handlerMap = new HashMap<>();

    static {
        // Container
        ContainerService containerService =
                registerBean(ContainerServiceImpl.class.getName(),
                        new ContainerServiceImpl(), ContainerService.class);

        // App properties
        AppProperties appProperties = registerBean(AppProperties.class.getName(),
                new AppProperties(new Properties()), AppProperties.class);

        // datasource
        DataSourceFactory dataSourceFactory = new HikariDataSourceFactory(appProperties);
        DataSource dataSource = registerBean(HikariDataSourceFactory.class.getName(),
                dataSourceFactory.createDataSource(), DataSource.class);

        // Transaction
        ConnectionHolder connectionHolder =
                registerBean(ConnectionHolder.class.getName(),
                        new ConnectionHolder(new ThreadLocal<>()), ConnectionHolder.class);

        TransactionManager transactionManager =
                registerBean(TransactionManager.class.getName(),
                        new DataSourceTransactionManager(dataSource, connectionHolder),
                        TransactionManager.class);

        // Exception
        RestApiExceptionHandler restApiExceptionHandler =
                registerBean(RestApiExceptionHandler.class.getName(),
                        new RestApiExceptionHandler(), RestApiExceptionHandler.class);

        SchedulerExceptionHandler schedulerExceptionHandler =
                registerBean(SchedulerExceptionHandler.class.getName(),
                        new SchedulerExceptionHandler(), SchedulerExceptionHandler.class);

        // HTTP REQ & RES
        HttpResponseSender httpResponseSender =
                registerBean(HttpResponseSender.class.getName(),
                        new HttpResponseSender(), HttpResponseSender.class);

        HttpRequestParser httpRequestParser =
                registerBean(HttpRequestParser.class.getName(),
                        new HttpRequestParser(), HttpRequestParser.class);

        // Handler mapping
        HandlerMapping handlerMapping =
                registerBean(HandlerMapping.class.getName(),
                        new HandlerMapping(containerService),
                        HandlerMapping.class);

        // Front interceptor
        FrontInterceptor loggingInterceptor = registerBean(LoggingFrontInterceptor.class.getName(),
                new LoggingFrontInterceptor(), FrontInterceptor.class);

        FrontInterceptor validationInterceptor = registerBean(
                ValidationFrontInterceptor.class.getName(), new ValidationFrontInterceptor(),
                FrontInterceptor.class);

        // Handler adaptor
        HandlerAdaptor handlerAdaptor =
                registerBean(HandlerAdaptor.class.getName(),
                        new HandlerAdaptor(), HandlerAdaptor.class);

        // Front Controller
        FrontController frontController =
                registerBean(FrontController.class.getName(),
                        new FrontController(restApiExceptionHandler, httpResponseSender,
                                handlerMapping, handlerAdaptor, containerService),
                        FrontController.class);
        // server
        HttpConnection connection = registerBean(Http11Connection.class.getName(),
                new Http11Connection(frontController, httpRequestParser,
                        httpResponseSender, restApiExceptionHandler), HttpConnection.class);

        // DAO
        OrderDao orderDao
                = registerBean(JdbcOrderDao.class.getName(), new JdbcOrderDao(connectionHolder),
                OrderDao.class);
        ShipmentDao shipmentDao
                = registerBean(JdbcShipmentDao.class.getName(),
                new JdbcShipmentDao(connectionHolder),
                ShipmentDao.class);

        // OrderMapper
        OrderMapper orderMapper =
                registerBean(OrderMapper.class.getName(), new OrderMapper(),
                        OrderMapper.class);

        // SFTP
        SftpClient sftpClient =
                registerBean(JSchSftpClient.class.getName(), new JSchSftpClient(),
                        SftpClient.class);

        SftpSender sftpSender =
                registerBean(JSchSftpSender.class.getName(),
                        new JSchSftpSender(sftpClient, orderMapper, appProperties),
                        SftpSender.class);

        // Service
        OrderService orderService =
                registerBean(OrderService.class.getName(),
                        new OrderService(orderDao, shipmentDao,
                                orderMapper, appProperties, sftpSender),
                        OrderService.class);

        // Controller
        OrderXmlParse orderXmlParse =
                registerBean(OrderXmlParse.class.getName(), new OrderXmlParse(),
                        OrderXmlParse.class);

        OrderController orderController =
                registerBean(OrderController.class.getName(),
                        new OrderController(orderService, orderXmlParse), OrderController.class);

        // Scheduler
        OrderJob orderJob =
                registerBean(OrderJob.class.getName(),
                        new OrderJob(orderService), OrderJob.class);

        OrderJobFactory orderJobFactory =
                registerBean(OrderJobFactory.class.getName(), new OrderJobFactory(orderJob),
                        OrderJobFactory.class);

        OrderSchedulerService orderSchedulerService =
                registerBean(QuartzOrderSchedulerService.class.getName(),
                        new QuartzOrderSchedulerService(orderJobFactory),
                        OrderSchedulerService.class);

    }


    private static <T> T registerBean(String beanId, Object bean, Class<T> clazz) {
        Object proxiedBean = createProxyBean(bean);
        beanMap.put(beanId, proxiedBean);
        return clazz.cast(proxiedBean);
    }

    private static Object createProxyBean(Object bean) {
        Object proxy = bean;
        Class<?> clazz = bean.getClass();
        if (hasMethodAnnotation(clazz, Transactional.class)) {
            TransactionManager transactionManager =
                    (TransactionManager) beanMap.get(TransactionManager.class.getName());
            proxy = ProxyGenerator.getProxy(clazz,
                    new TransactionInterceptor(proxy, transactionManager));

        }
        if (hasMethodAnnotation(clazz, LogExecution.class)) {
            proxy = ProxyGenerator.getProxy(clazz, new LoggingInterceptor(proxy));

        }
        return proxy;
    }

    private static boolean hasMethodAnnotation(Class<?> clazz,
            Class<? extends Annotation> annotationClass) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotationClass)) {
                return true;
            }
        }
        return false;
    }


}
