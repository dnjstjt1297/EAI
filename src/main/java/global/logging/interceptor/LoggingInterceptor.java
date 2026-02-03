package main.java.global.logging.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.AllArgsConstructor;
import main.java.global.logging.LogContext;
import main.java.global.logging.annotation.LogExecution;
import main.java.global.proxy.ProxyWrapper;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
public class LoggingInterceptor implements MethodInterceptor, ProxyWrapper {

    private final Object target;
    private final LogContext logContext;
    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy proxy)
            throws Throwable {
        if (method.isAnnotationPresent(LogExecution.class)) {
            LogExecution logExecution = method.getAnnotation(LogExecution.class);

            String indent = logContext.getIndent();
            String className = target.getClass().getSimpleName();

            log.info("{}[INFO] START: {}.{}()", indent, className, method.getName());

            logContext.increment();

            long startTime = System.currentTimeMillis();
            try {

                Object result = method.invoke(target, args);
                logContext.decrement();

                log.info("{}[INFO] END: {}.{}() , Duration: {}ms", indent, className,
                        method.getName(), System.currentTimeMillis() - startTime);
                return result;
            } catch (Exception e) {

                Exception exception = e;

                while (exception instanceof InvocationTargetException ite) {
                    exception = (Exception) ite.getTargetException();
                }

                logContext.decrement();
                log.error("{}[ERROR] END: {}.{}() , Exception: {} , Duration: {}ms",
                        indent, className, method.getName(), exception.getMessage(),
                        System.currentTimeMillis() - startTime);
                throw exception;
            }
        }
        return proxy.invoke(target, args);
    }

    @Override
    public Object getTarget() {
        return target;
    }
}
