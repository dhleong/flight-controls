package net.dhleong.ctrlf.util;

import rx.Observable;
import rx.Subscriber;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Action1;
import rx.functions.Func1;


/**
 * Prevent receipt of an event from overriding your output.
 *
 * Usage:
 *  1. Hook your Preventer into the output using:
 *  <code>
 *      observable.doOnNext(preventer);
 *  </code>
 *
 *  2. Filter your input using:
 *  <code>
 *      observable.lift(preventer.prevent())
 *  </code>
 *
 * @author dhleong
 */
public class OverridePreventer<T> implements Action1<T> {

    boolean awaitingSent;
    T lastSent;

    @Override
    public void call(final T sentValue) {
        awaitingSent = true;
        lastSent = sentValue;
    }

    /**
     * Pass to Observable#lift() to filter out
     *  events as appropriate. If your input
     *  doesn't use the same type as your output,
     *  you can use {@see #prevent(Func1)} to handle
     *  the conversion
     */
    public Observable.Operator<T, T> prevent() {
        return prevent(new Func1<T, T>() {
            @Override
            public T call(final T input) {
                return input;
            }
        });
    }

    /**
     * If the value you send and the value you receive differ,
     *  you can pass a mapper Func to handle the conversion
     * @see #prevent()
     */
    public <R> Observable.Operator<R, R> prevent(final Func1<R, T> mapper) {
        return new Observable.Operator<R, R>() {
            @Override
            public Subscriber<? super R> call(final Subscriber<? super R> child) {
                return new Subscriber<R>(child) {
                    @Override
                    public void onCompleted() {
                        child.onCompleted();
                    }

                    @Override
                    public void onError(final Throwable e) {
                        child.onError(e);
                    }

                    @Override
                    public void onNext(final R input) {
                        try {
                            final T value = mapper.call(input);
                            if (shouldAllow(value)) {
                                child.onNext(input);
                            } else {
                                request(1);
                            }
                        } catch (Throwable e) {
                            child.onError(OnErrorThrowable.addValueAsLastCause(e, input));
                        }
                    }
                };
            }
        };
    }

    boolean shouldAllow(final T input) {
        if (awaitingSent && input != lastSent) {
            return false;
        } else if (awaitingSent) {
            // equals! allow it
            awaitingSent = false;
            return true;
        }

        // not awaiting; allow it!
        return true;
    }

    public static <T> OverridePreventer<T> create() {
        return new OverridePreventer<>();
    }

}
