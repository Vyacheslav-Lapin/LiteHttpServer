package common;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class PoolTest {

    @FunctionalInterface
    public interface TestFuncInt {
        String hello(String name);
    }

    @Test
    public void test() throws Exception {
        TestFuncInt realObj = name -> String.format("Hello %s!", name);
        TestFuncInt proxyObj = Pool.getProxyMakerFor(TestFuncInt.class).apply(
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "toString":
                            return proxy.getClass().getName() + "@" +
                                    Integer.toHexString(System.identityHashCode(proxy)) +
                                    ", with InvocationHandler";
                    }
                    return method.invoke(realObj, args);
                });

        assertThat(realObj.hello("Duke"), is(proxyObj.hello("Duke")));
        assertThat(proxyObj.toString(), not(realObj.toString()));
        System.out.printf("proxyObj.hashCode(): %H%n", proxyObj);
        System.out.printf("proxyObj.equals(proxyObj): %B%n", proxyObj.equals(proxyObj));
        System.out.printf("proxyObj.equals(new Object()): %B%n", proxyObj.equals(new Object()));
        System.out.printf("proxyObj.equals(null): %B%n", proxyObj.equals(null));
    }
}