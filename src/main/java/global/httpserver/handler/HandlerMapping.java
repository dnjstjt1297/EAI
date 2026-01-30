package main.java.global.httpserver.handler;

import java.lang.reflect.Method;
import java.util.Map;
import lombok.AllArgsConstructor;
import main.java.global.httpserver.dto.MappingInfo;
import main.java.global.httpserver.enums.HttpMethod;

@AllArgsConstructor
public class HandlerMapping {

    private final Map<MappingInfo, HandlerMethod> handlerMap;
    private final Map<String, Object> beanMap;

    public void init() {

        for (Object bean : beanMap.values()) {
            Class<?> clazz = bean.getClass();

            if (clazz.isAnnotationPresent(RestController.class)) {
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(Mapping.class)) {
                        Mapping mapping = method.getAnnotation(Mapping.class);
                        String path = mapping.path();
                        HttpMethod type = mapping.method();
                        MappingInfo mappingInfo = new MappingInfo(path, type);

                        if (handlerMap.containsKey(mappingInfo)) {
                            throw new IllegalStateException("중복된 경로 발견");
                        }

                        handlerMap.put(mappingInfo, new HandlerMethod(bean, method));
                    }
                }
            }
        }
    }

    public Object getHandler(MappingInfo mappingInfo) {
        return handlerMap.get(mappingInfo);
    }

}
