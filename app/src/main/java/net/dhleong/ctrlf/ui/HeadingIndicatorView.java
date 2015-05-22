package net.dhleong.ctrlf.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.AttributeSet;
import net.dhleong.ctrlf.App;
import net.dhleong.ctrlf.model.AutoPilotStatus;
import net.dhleong.ctrlf.model.HeadingStatus;
import net.dhleong.ctrlf.ui.base.BaseInstrumentView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import javax.inject.Inject;

/**
 * @author dhleong
 */
public class HeadingIndicatorView extends BaseInstrumentView {

    @Inject Observable<AutoPilotStatus> autoPilotStatus;
    @Inject Observable<HeadingStatus> headingStatus;

    long lastFrame = 0;

    float heading, headingDeltaRate, headingBug;

    public HeadingIndicatorView(final Context context) {
        this(context, null);
    }

    public HeadingIndicatorView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        App.provideComponent(this)
           .newInstrumentComponent()
           .inject(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        subscriptions.add(
                autoPilotStatus.observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<AutoPilotStatus>() {
                        @Override
                        public void call(final AutoPilotStatus autoPilotStatus) {
                            headingBug = autoPilotStatus.headingBug;
                        }
                    })
        );
        subscriptions.add(
                headingStatus.observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<HeadingStatus>() {
                        @Override
                        public void call(final HeadingStatus headingStatus) {
                            heading = headingStatus.heading;
                            headingDeltaRate = headingStatus.headingDeltaRate;
                        }
                    })
        );

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        if (lastFrame > 0) {
            final long now = SystemClock.uptimeMillis();
            long delta = now - lastFrame;
            onUpdate(delta);
            lastFrame = now;
        }

        onDrawPlane(canvas);

        int start = canvas.save();
        canvas.rotate(heading);
        onDrawMarkers(canvas);

        canvas.save();
        canvas.rotate(headingBug);
        onDrawBug(canvas);
        canvas.restoreToCount(start);
    }

    private void onDrawPlane(final Canvas canvas) {
    }

    private void onDrawMarkers(final Canvas canvas) {
        
    }

    private void onDrawBug(final Canvas canvas) {
    }

    private void onUpdate(final long deltaMillis) {
        if (Math.abs(headingDeltaRate) > 0.01) {
            heading += headingDeltaRate * (deltaMillis / 1000f);
        }
    }
}
