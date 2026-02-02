package main.java.global.container;

import java.util.List;
import java.util.Map;
import main.java.global.httpserver.dto.MappingInfo;
import main.java.global.httpserver.frontinterceptor.FrontInterceptor;
import main.java.global.httpserver.handler.HandlerMethod;

public interface ContainerService {

    Object getBean(String beanName);

    Map<String, Object> getBeanMap();

    Map<MappingInfo, HandlerMethod> getHandlerMap();

    List<FrontInterceptor> getFrontInterceptorList();
}
