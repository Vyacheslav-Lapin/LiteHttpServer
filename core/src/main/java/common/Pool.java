package common;

import common.functions.ExceptionalConsumer;
import common.functions.ExceptionalSupplier;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class Pool<T extends AutoCloseable> implements Supplier<T>, AutoCloseable {

    private BlockingQueue<T> freeObjectsQueue;
    private volatile boolean isClosing;
    private Function<InvocationHandler, T> proxyMaker;

    public Pool(Class<T> anInterface, int size, Supplier<T> generator) {
        freeObjectsQueue = new ArrayBlockingQueue<>(size);
        proxyMaker = getProxyMakerFor(anInterface);

        //noinspection JavacQuirks
        IntStream.range(0, size)
                .mapToObj(i -> generator.get())
                .map(this::proxy)
                .forEach(freeObjectsQueue::add);
    }

    static <T> Function<InvocationHandler, T> getProxyMakerFor(Class<T> anInterface) {
        //noinspection unchecked
        return invocationHandler -> (T) Proxy.newProxyInstance(
                anInterface.getClassLoader(), new Class[]{anInterface}, invocationHandler);
    }

    private T proxy(T t) {
        return proxyMaker.apply((proxy, method, args) -> {
            //noinspection unchecked
            return method.getName().equals("close") && !isClosing ?
                    freeObjectsQueue.offer((T) proxy) :
                    method.invoke(proxy, args);
        });
    }

    @Override
    public T get() {
        if (isClosing) throw new RuntimeException("Trying to get object from closed pool!");
        return ExceptionalSupplier.getOrThrowUnchecked(freeObjectsQueue::take);
    }

    @Override
    public void close() throws Exception {
        isClosing = true;
        freeObjectsQueue.forEach(ExceptionalConsumer.toUncheckedConsumer(T::close));
    }
}