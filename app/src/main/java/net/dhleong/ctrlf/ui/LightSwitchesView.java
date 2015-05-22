package net.dhleong.ctrlf.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import net.dhleong.ctrlf.App;
import net.dhleong.ctrlf.R;
import net.dhleong.ctrlf.model.LightsStatus;
import net.dhleong.ctrlf.model.SimEvent;
import net.dhleong.ctrlf.util.RxUtil;
import net.dhleong.ctrlf.util.SwitchToggleObservable;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.OnClickEvent;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;

/**
 * Simple panel for controlling lights. On the Cessna,
 *  this is in the same panel with the ignition key
 *  and such, but for now we'll be lazy and just do this
 *
 * TODO Style, style, style
 *
 * @author dhleong
 */
public class LightSwitchesView extends LinearLayout {

    static final SimEvent[] SWITCH_EVENTS = {
            SimEvent.BEACON_LIGHTS_TOGGLE,
            SimEvent.LANDING_LIGHTS_TOGGLE,
            SimEvent.TAXI_LIGHTS_TOGGLE,
            SimEvent.PANEL_LIGHTS_TOGGLE,
            SimEvent.NAV_LIGHTS_TOGGLE,
            SimEvent.STROBES_TOGGLE
    };

    static final int[] LABELS = {
        R.string.beacon_lights,
        R.string.landing_lights,
        R.string.taxi_lights,
        R.string.panel_lights,
        R.string.nav_lights,
        R.string.strobes,
    };

    @Inject Observer<SimEvent> lightSwitcher;
    @Inject Observable<LightsStatus> lightsStatus;

    private final ToggleSwitch[] switches;
    private final String[] labels;
    private final Paint labelPaint;

    private boolean receivingStatus;

    private CompositeSubscription subscriptions = new CompositeSubscription();

    public LightSwitchesView(final Context context) {
        this(context, null);
    }

    public LightSwitchesView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        setWillNotDraw(false);

        App.provideComponent(this)
           .newLightsComponent()
           .inject(this);

        labelPaint = new Paint();
        labelPaint.setColor(0xff000000);
        labelPaint.setTextAlign(Align.CENTER);

        final int len = LABELS.length;
        labels = new String[len];
        for (int i = 0; i < len; i++) {
            labels[i] = context.getString(LABELS[i]);
        }

        switches = new ToggleSwitch[len];
        for (int i = 0; i < len; i++) {
            final SimEvent ev = SWITCH_EVENTS[i];
            final ToggleSwitch toggle = new ToggleSwitch(context);
            SwitchToggleObservable.on(toggle)
                                  .filter(new Func1<OnClickEvent, Boolean>() {
                                      @Override
                                      public Boolean call(
                                              final OnClickEvent onClickEvent) {
                                          // drop events caused by receiving
                                          //  a status from the server
                                          return !receivingStatus;
                                      }
                                  })
                                  .doOnNext(RxUtil.PERFORM_HAPTIC)
                                  .map(toEvent(ev))
                                  .subscribe(lightSwitcher);
            addView(toggle);
            switches[i] = toggle;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        subscriptions.add(
                lightsStatus.observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<LightsStatus>() {
                                @Override
                                public void call(final LightsStatus lightsStatus) {
                                    receivingStatus = true;
                                    final int len = SWITCH_EVENTS.length;
                                    for (int i = 0; i < len; i++) {
                                        final SimEvent ev = SWITCH_EVENTS[i];
                                        final boolean status = lightsStatus.getStatus(ev);
                                        switches[i].setChecked(status);
                                    }
                                    receivingStatus = false;
                                }
                            })
        );
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        subscriptions.unsubscribe();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        final Paint paint = labelPaint;
        final int len = labels.length;
        for (int i=0; i < len; i++) {
            View toggle = getChildAt(i);
            final int l = toggle.getLeft();
            final int r = toggle.getRight();
            final int t = toggle.getTop();
            canvas.drawText(labels[i], l + (r - l) / 2, t, paint);
        }
    }

    static Func1<OnClickEvent, SimEvent> toEvent(final SimEvent ev) {
        return new Func1<OnClickEvent, SimEvent>() {
            @Override
            public SimEvent call(final OnClickEvent onClickEvent) {
                return ev;
            }
        };
    }

}
