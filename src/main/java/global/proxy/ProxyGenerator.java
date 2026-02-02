package main.java.global.proxy;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

public class ProxyGenerator {

    private static final Objenesis objenesis = new ObjenesisStd();

    @SuppressWarnings("unchecked") //
    public static <T> T getProxy(Class<T> clazz, MethodInterceptor interceptor) {

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);

        enhancer.setInterfaces(new Class[]{Factory.class});

        enhancer.setCallbackType(interceptor.getClass());

        Class<?> proxyClass = enhancer.createClass();

        T proxy = (T) objenesis.newInstance(proxyClass);
        ((Factory) proxy).setCallbacks(new Callback[]{interceptor});

        return proxy;
    }
}
