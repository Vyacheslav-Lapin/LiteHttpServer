package common.functions;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface ExceptionalBiFunction<T, U, R, E extends Throwable> extends BiFunction<T, U, Exceptional<R, E>> {

    R get(T t, U u) throws E;

    @Override
    default Exceptional<R, E> apply(T t, U u) {
        try {
            return Exceptional.withValue(get(t, u));
        } catch (Throwable e) {
            //noinspection unchecked
            return Exceptional.withException((E) e);
        }
    }

    static <T, U, R, E extends Throwable> R getOrThrowUnchecked(ExceptionalBiFunction<T, U, R, E> exceptionalBiFunction,
                                                                T param1, U param2) {
        return exceptionalBiFunction.apply(param1, param2).getOrThrowUnchecked();
    }

    static <T, U, R, E extends Throwable> BiFunction<T, U, R> toUnchecked(ExceptionalBiFunction<T, U, R, E> exceptionalBiFunction) {
        return (t, u) -> getOrThrowUnchecked(exceptionalBiFunction, t, u);
    }

    static <T, U, R, E extends Throwable> ExceptionalSupplier<R, E> carry(ExceptionalBiFunction<T, U, R, E> exceptionalBiFunction,
                                                                          T param1, U param2) {
        return () -> exceptionalBiFunction.get(param1, param2);
    }

    static <T, U, R, E extends Throwable> Supplier<R> carryUnchecked(ExceptionalBiFunction<T, U, R, E> exceptionalBiFunction,
                                                                     T param1, U param2) {
        return carry(exceptionalBiFunction, param1, param2)::getOrThrowUnchecked;
    }

    static <T, U, R, E extends Throwable> ExceptionalFunction<U, R, E> carryFirst(ExceptionalBiFunction<T, U, R, E> exceptionalBiFunction,
                                                                                  T param) {
        return u -> exceptionalBiFunction.get(param, u);
    }

    static <T, U, R, E extends Throwable> Function<U, R> carryFirstUnchecked(ExceptionalBiFunction<T, U, R, E> exceptionalBiFunction,
                                                                             T param) {
        return u -> carryFirst(exceptionalBiFunction, param)
                .apply(u)
                .getOrThrowUnchecked();
    }

    static <T, U, R, E extends Throwable> ExceptionalFunction<T, R, E> carrySecond(ExceptionalBiFunction<T, U, R, E> exceptionalBiFunction,
                                                                                   U param) {
        return t -> exceptionalBiFunction.get(t, param);
    }

    static <T, U, R, E extends Throwable> Function<T, R> carrySecondUnchecked(ExceptionalBiFunction<T, U, R, E> exceptionalBiFunction,
                                                                              U param) {
        return t -> carrySecond(exceptionalBiFunction, param)
                .apply(t)
                .getOrThrowUnchecked();
    }
}