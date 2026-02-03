package main.java.global.httpserver.handler;

import java.lang.reflect.Method;
import java.util.Map;
import lombok.AllArgsConstructor;
import main.java.global.container.ContainerService;
import main.java.global.httpserver.dto.MappingInfo;
import main.java.global.httpserver.enums.HttpMethod;
import main.java.global.proxy.ProxyWrapper;
import net.sf.cglib.proxy.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
public class HandlerMapping {

    private final ContainerService containerService;
    private static Logger log = LoggerFactory.getLogger(HandlerMapping.class);

    public void init() {
        Map<MappingInfo, HandlerMethod> handlerMap = containerService.getHandlerMap();
        Map<String, Object> beanMap = containerService.getBeanMap();

        for (Object bean : beanMap.values()) {
            Class<?> clazz = getTargetClass(bean);

            if (clazz.isAnnotationPresent(RestController.class)) {
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(Mapping.class)) {
                        Mapping mapping = method.getAnnotation(Mapping.class);
                        String path = mapping.path();
                        HttpMethod type = mapping.method();
                        MappingInfo mappingInfo = new MappingInfo(path, type);

                        if (handlerMap.containsKey(mappingInfo)) {
                            log.error("[ERROR] Duplicated path:{}", mappingInfo.path());
                            throw new IllegalStateException("Duplicated path");
                        }

                        handlerMap.put(mappingInfo, new HandlerMethod(bean, method));
                    }
                }
            }
        }
    }

    public Object getHandler(MappingInfo mappingInfo) {
        return containerService.getHandlerMap().get(mappingInfo);
    }

    public static Class<?> getTargetClass(Object bean) {

        while (bean instanceof Factory factory) {
            Object callback = factory.getCallback(0);
            if (callback instanceof ProxyWrapper wrapper) {
                bean = wrapper.getTarget();
            } else {
                break;
            }
        }
        return bean.getClass();
    }

}
