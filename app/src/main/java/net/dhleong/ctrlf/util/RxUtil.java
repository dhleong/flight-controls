package net.dhleong.ctrlf.util;

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
}
