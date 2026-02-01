package main.java.global.transaction.interceptor;

import java.lang.reflect.Method;
import lombok.AllArgsConstructor;
import main.java.global.transaction.annotation.Transactional;
import main.java.global.transaction.manager.TransactionManager;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

@AllArgsConstructor
public class TransactionInterceptor implements MethodInterceptor {

    private Object target;
    private final TransactionManager transactionManager;

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
            throws Throwable {
        if (method.isAnnotationPresent(Transactional.class)) {
            try {
                transactionManager.doBegin();
                Object result = proxy.invoke(target, args); // CGLIB의 빠른 호출 방식
                transactionManager.doCommit();
                return result;
            } catch (Exception e) {
                transactionManager.doRollback();
                throw e.getCause() != null ? e.getCause() : e;
            } finally {
                transactionManager.doCleanUpAfterCompletion();
            }
        }
        return proxy.invoke(target, args);
    }
}
