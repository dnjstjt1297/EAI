package main.java.global.transaction.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.AllArgsConstructor;
import main.java.global.proxy.ProxyWrapper;
import main.java.global.transaction.annotation.Transactional;
import main.java.global.transaction.manager.TransactionManager;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

@AllArgsConstructor
public class TransactionInterceptor implements MethodInterceptor, ProxyWrapper {

    private Object target;
    private final TransactionManager transactionManager;

    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy proxy)
            throws Throwable {
        if (method.isAnnotationPresent(Transactional.class)) {
            try {
                transactionManager.doBegin();
                Object result = proxy.invoke(target, args);
                transactionManager.doCommit();
                return result;
            } catch (Exception e) {

                Exception exception = e;
                while (exception instanceof InvocationTargetException ite) {
                    exception = (Exception) ite.getTargetException();
                }

                transactionManager.doRollback();
                throw exception;
            } finally {
                transactionManager.doCleanUpAfterCompletion();
            }
        }
        return proxy.invoke(target, args);
    }

    @Override
    public Object getTarget() {
        return target;
    }
}
