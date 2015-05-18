package net.dhleong.ctrlf.util;

import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import net.dhleong.ctrlf.ui.ToggleSwitch;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.Subscription;
import rx.android.AndroidSubscriptions;
import rx.android.internal.Assertions;
import rx.android.view.OnClickEvent;
import rx.functions.Action0;

/**
 * @author dhleong
 */
public class SwitchToggleObservable
        implements OnSubscribe<OnClickEvent> {

    private final ToggleSwitch view;

    public SwitchToggleObservable(final ToggleSwitch view) {
        this.view = view;
    }

    @Override
    public void call(final Subscriber<? super OnClickEvent> observer) {
        Assertions.assertUiThread();

        final CompoundButton.OnCheckedChangeListener listener =
                new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(final CompoundButton buttonView,
                            final boolean isChecked) {
                        observer.onNext(OnClickEvent.create(view));
                    }
                };

        final Subscription subscription = AndroidSubscriptions.unsubscribeInUiThread(new Action0() {
            @Override
            public void call() {
                view.setOnCheckedChangeListener(null);
            }
        });

        // we "could" add the composite stuff that RxAndroid does for clicks,
        //  but there's no need right now
        view.setOnCheckedChangeListener(listener);
        observer.add(subscription);
    }

    public static Observable<OnClickEvent> on(final ToggleSwitch view) {
        return Observable.create(new SwitchToggleObservable(view));
    }
}
