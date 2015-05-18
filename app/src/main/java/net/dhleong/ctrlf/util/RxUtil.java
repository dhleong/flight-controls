package net.dhleong.ctrlf.util;

import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.SimEvent;
import rx.Observer;
import rx.android.view.OnClickEvent;
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
}
