package main.java.global.container;


import java.util.List;
import java.util.Map;
import main.java.global.httpserver.dto.MappingInfo;
import main.java.global.httpserver.frontinterceptor.FrontInterceptor;
import main.java.global.httpserver.handler.HandlerMethod;

public class ContainerServiceImpl implements ContainerService {

    @Override
    public Object getBean(String beanName) {
        Map<String, Object> beanMap = Container.getBeanMap();

        return beanMap.get(beanName);
    }

    @Override
    public Map<String, Object> getBeanMap() {
        return Container.getBeanMap();
    }

    @Override
    public Map<MappingInfo, HandlerMethod> getHandlerMap() {
        return Container.getHandlerMap();
    }

    @Override
    public List<FrontInterceptor> getFrontInterceptorList() {
        return getBeanMap().values().stream()
                .filter(bean -> bean instanceof FrontInterceptor)
                .map(bean -> (FrontInterceptor) bean)
                .toList();
    }


}
