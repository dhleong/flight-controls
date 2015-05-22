package net.dhleong.ctrlf.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.SystemClock;
import android.util.AttributeSet;
import net.dhleong.ctrlf.App;
import net.dhleong.ctrlf.R;
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

    private static final float AIRPLANE_SCALE = 0.75f;
    private static final float TICK_MAJOR = 6;
    private static final float TICK_MINOR = 4;
    private static final float TICK_OFFSET = 5;

    /** in degrees */
    private static final float TICK_INTERVAL = 5;

    @Inject Observable<AutoPilotStatus> autoPilotStatus;
    @Inject Observable<HeadingStatus> headingStatus;

    final Paint airplanePaint, bugPaint, tickPaint;
    final float tickMajor, tickMinor, tickOffset;

    Path airplane;

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

        final Resources res = getResources();
        final float density = res.getDisplayMetrics().density;

        tickMajor = TICK_MAJOR * density;
        tickMinor = TICK_MINOR * density;
        tickOffset = TICK_OFFSET * density;

        airplanePaint = new Paint();
        airplanePaint.setColor(res.getColor(R.color.heading_airplane));
        airplanePaint.setAntiAlias(true);
        airplanePaint.setStyle(Paint.Style.STROKE);
        airplanePaint.setStrokeWidth(3 * density);
        airplanePaint.setStrokeJoin(Paint.Join.ROUND);

        bugPaint = new Paint(airplanePaint);
        bugPaint.setStrokeWidth(4 * density);

        tickPaint = new Paint();
        tickPaint.setColor(0xff111111);
        tickPaint.setStrokeCap(Paint.Cap.ROUND);
        tickPaint.setStrokeWidth(2 * density);
        tickPaint.setTextAlign(Paint.Align.CENTER);
        tickPaint.setTextSize(18 * density);
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

        final float center = (getRight() - getLeft()) / 2f;
        int start = canvas.save();
        canvas.rotate(heading);
        onDrawMarkers(canvas, center);

        canvas.save();
        canvas.rotate(headingBug);
        onDrawBug(canvas, center);
        canvas.restoreToCount(start);
    }

    private void onDrawPlane(final Canvas canvas) {

        final Path path;
        final Path existing = airplane;
        if (existing == null) {
            final float width = getRight() - getLeft();
            path = airplane = prepareAirplanePath(width);
        } else {
            path = existing;
        }

        canvas.drawPath(path, airplanePaint);
    }

    private void onDrawMarkers(final Canvas canvas, final float center) {
        canvas.save();
        for (int degree=0; degree < 360; degree += TICK_INTERVAL) {
            final float length = degree % 10 == 0
                ? tickMajor
                : tickMinor;

            final float end = tickOffset + length;
            canvas.drawLine(center, tickOffset, center, end, tickPaint);

            final String toDraw;
            switch (degree) {
            case 0: toDraw = "N"; break;
            case 90: toDraw = "E"; break;
            case 180: toDraw = "S"; break;
            case 270: toDraw = "W"; break;
            default:
                if (degree % 30 == 0) {
                    toDraw = String.valueOf(degree / 10);
                } else {
                    toDraw = null;
                }
            }

            if (toDraw != null) {
                canvas.drawText(toDraw, center, end + tickPaint.getTextSize(), tickPaint);
            }

            canvas.rotate(TICK_INTERVAL, center, center);
        }
        canvas.restore();
    }

    private void onDrawBug(final Canvas canvas, final float center) {
        final float length = tickMinor + (tickMajor + tickMinor) / 2;
        final float separation = bugPaint.getStrokeWidth();
        canvas.drawLine(center - separation, 0, center - separation, length, bugPaint);
        canvas.drawLine(center + separation, 0, center + separation, length, bugPaint);
    }

    private void onUpdate(final long deltaMillis) {
        if (Math.abs(headingDeltaRate) > 0.01) {
            heading += headingDeltaRate * (deltaMillis / 1000f);
        }
    }

    private static Path prepareAirplanePath(final float width) {

        final float center = width / 2;
        final float unit = width / 10 * AIRPLANE_SCALE;
        final float half = unit / 2;
        final float tiny = unit / 4;

        final Path newPath = new Path();
        newPath.moveTo(center, tiny);
        newPath.lineTo(center, center - 3 * unit);

        // right nose + wing
        newPath.lineTo(center + unit, center - unit);
        newPath.lineTo(center + 4 * unit, center);
        newPath.rLineTo(-half, unit);
        newPath.lineTo(center + half + tiny, center + half + tiny);
        newPath.rLineTo(-tiny, unit + half);

        // right tail
        newPath.rLineTo(unit, unit);
        newPath.rLineTo(-half, half);
        newPath.rLineTo(-unit, -half);

        // left tail
        newPath.rLineTo(-unit, half);
        newPath.rLineTo(-half, -half);
        newPath.rLineTo(unit, -unit);

        // left nose + wing
        newPath.rLineTo(-tiny, -(unit + half));
        newPath.lineTo(center - (4 * unit) + half, center + unit);
        newPath.rLineTo(-half, -unit);
        newPath.lineTo(center - unit, center - unit);
        newPath.lineTo(center, center - 3 * unit);

        return newPath;
    }
}
