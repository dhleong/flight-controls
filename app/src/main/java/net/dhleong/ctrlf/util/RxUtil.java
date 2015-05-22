package net.dhleong.ctrlf.util;

import android.view.HapticFeedbackConstants;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.SimEvent;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.view.OnClickEvent;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * @author dhleong
 */
public class RxUtil {
    public static final Func1<? super OnClickEvent, Void> CLICK_TO_VOID =
            new Func1<OnClickEvent, Void>() {
                @Override
                public Void call(final OnClickEvent onClickEvent) {
                    return null;
                }
            };

    public static final Action1<OnClickEvent> PERFORM_HAPTIC = new Action1<OnClickEvent>() {
        @Override
        public void call(final OnClickEvent onClickEvent) {
            onClickEvent.view().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
    };

    /**
     * Returns an Observer which sends the provided Event
     *  over the given Connection. The Type observed will
     *  be included as the parameter if an Integer; otherwise,
     *  the param sent will just be 0
     */
    public static <T> Observer<T> doSend(final Connection connection, final SimEvent event) {
        return new Observer<T>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(final Throwable e) {
                // ...?
            }

            @Override
            public void onNext(final T param) {
                if (param instanceof Integer) {
                    connection.sendEvent(event, (Integer) param);
                } else {
                    connection.sendEvent(event, 0);
                }
            }
        };
    }

    public static Func1<Integer, Integer> times(final int multiplicand) {
        return new Func1<Integer, Integer>() {
            @Override
            public Integer call(final Integer input) {
                return input * multiplicand;
            }
        };
    }

    public static Func1<Integer, Integer> times(final Func0<Integer> multiplicand) {
        return new Func1<Integer, Integer>() {
            @Override
            public Integer call(final Integer input) {
                return input * multiplicand.call();
            }
        };
    }

    /** @return a Func that returns True for instances of the provided class */
    public static Func1<? super Object,Boolean> isInstanceOf(final Class<?> klass) {
        return new Func1<Object, Boolean>() {
            @Override
            public Boolean call(final Object simData) {
                return klass.isInstance(simData);
            }
        };
    }

    /**
     * Modifies the Observable to filter out objects which aren't an instance
     *  of the provided class; the resulting Observable's items will all be
     *  cast the the provided class.
     */
    public static <To, From> Observable.Operator<To, From> pickInstancesOf(final Class<To> klass) {
        return new Observable.Operator<To, From>() {
            @Override
            public Subscriber<? super From> call(final Subscriber<? super To> o) {
                return new Subscriber<From>() {
                    @Override
                    public void onCompleted() {
                        o.onCompleted();
                    }

                    @Override
                    public void onError(final Throwable e) {
                        o.onError(e);
                    }

                    @Override
                    public void onNext(final From from) {
                        if (klass.isInstance(from)) {
                            o.onNext(klass.cast(from));
                        }
                    }
                };
            }
        };
    }

    /**
     * Create a mapping function that pins the input to be within the
     *  provided bounds, inclusive
     */
    public static Func1<Integer, Integer> limitRange(final int lowerBound, final int upperBound) {
        return new Func1<Integer, Integer>() {
            @Override
            public Integer call(final Integer input) {
                return Math.min(upperBound, Math.max(lowerBound, input));
            }
        };
    }

    /**
     * Create a mapping function that pins the input to be within the
     *  provided bounds, inclusive; if over upperBound, it will become
     *  <code>input % upperBound</code>
     */
    public static Func1<Integer, Integer> modulo(final int upperBound) {
        return new Func1<Integer, Integer>() {
            @Override
            public Integer call(final Integer input) {
                if (input < 0) {
                    return (upperBound + input) % upperBound;
                }

                return input % upperBound;
            }
        };
    }
}
