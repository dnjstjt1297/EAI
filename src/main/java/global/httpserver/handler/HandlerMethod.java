package main.java.global.httpserver.handler;

import java.lang.reflect.Method;
import lombok.AllArgsConstructor;
import lombok.Getter;
import main.java.global.httpserver.dto.response.HttpResponse;

@Getter
@AllArgsConstructor
public class HandlerMethod {

    private final Object bean; // 호출할 컨트롤러
    private final Method method;  // 실행할 메서드 정보 (리플렉션)

    public HttpResponse invoke(Object... args) throws Exception {
        return (HttpResponse) method.invoke(bean, args);
    }
}
