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
import net.dhleong.ctrlf.ui.art.PathArtist;
import net.dhleong.ctrlf.ui.base.BaseInstrumentView;
import net.dhleong.ctrlf.util.OverridePreventer;
import net.dhleong.ctrlf.util.UiUtil;
import net.dhleong.ctrlf.util.scopes.Named;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

import javax.inject.Inject;

import static net.dhleong.ctrlf.util.RxUtil.modulo;

/**
 * @author dhleong
 */
public class HeadingIndicatorView extends BaseInstrumentView {

    static class AirplaneArtist extends PathArtist<HeadingIndicatorView> {

        public AirplaneArtist(final HeadingIndicatorView view) {
            super(view);
        }

        @Override
        protected Path onCreatePath() {
            return prepareAirplanePath(view.totalOffset, width());
        }
    }


    private static final float AIRPLANE_SCALE = 0.55f;
    private static final float TICK_MAJOR = 8;
    private static final float TICK_MINOR = 5;
    private static final float TICK_OFFSET = 10;
    private static final float DIAL_OFFSET = 10;

    /** in degrees */
    private static final float TICK_INTERVAL = 6;
    private static final float DELTA_DEADZONE = 0.01f;

    final SmallDialView bugDial;

    @Inject @Named("APHeadingBug") Observer<Integer> headingBugObserver;
    @Inject Observable<AutoPilotStatus> autoPilotStatus;
    @Inject Observable<HeadingStatus> headingStatus;

    final OverridePreventer<Integer> headingBugOverrides = OverridePreventer.create();

    final Paint airplanePaint, bugPaint, tickPaint;
    final float tickMajor, tickMinor;
    final float tickOffset, dialOffset, totalOffset;

    final AirplaneArtist airplaneArtist = new AirplaneArtist(this);

    long lastFrame = 0;

    float heading, headingDeltaRate, headingBug;

    private Action1<? super Integer> setBugDegrees = new Action1<Integer>() {
        @Override
        public void call(final Integer integer) {
            setBugDegrees(integer);
        }
    };

    public HeadingIndicatorView(final Context context) {
        this(context, null);
    }

    public HeadingIndicatorView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        App.provideComponent(this)
           .newInstrumentComponent()
           .inject(this);

        bugDial = new SmallDialView(context);
        bugDial.setHapticDuration(5); // this is going to spin a lot more; be more subtle
        addView(bugDial);

        final Resources res = getResources();
        final float density = res.getDisplayMetrics().density;

        tickMajor = TICK_MAJOR * density;
        tickMinor = TICK_MINOR * density;
        tickOffset = TICK_OFFSET * density;
        dialOffset = DIAL_OFFSET * density;
        totalOffset = tickOffset + dialOffset;

        airplanePaint = new Paint();
        airplanePaint.setColor(UiUtil.resolveResource(this, R.color.heading_airplane));
        airplanePaint.setAntiAlias(true);
        airplanePaint.setStyle(Paint.Style.STROKE);
        airplanePaint.setStrokeWidth(3 * density);
        airplanePaint.setStrokeJoin(Paint.Join.ROUND);

        bugPaint = new Paint(airplanePaint);
        bugPaint.setStrokeWidth(5 * density);

        tickPaint = new Paint();
        tickPaint.setColor(0xff111111);
        tickPaint.setStrokeCap(Paint.Cap.ROUND);
        tickPaint.setStrokeWidth(2 * density);
        tickPaint.setTextAlign(Paint.Align.CENTER);
        tickPaint.setTextSize(18 * density);
        tickPaint.setAntiAlias(true);
    }

    public void setBugDegrees(final int degrees) {
        headingBug = degrees;
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        subscriptions.add(
                autoPilotStatus.observeOn(AndroidSchedulers.mainThread())
                               .lift(headingBugOverrides.prevent(new Func1<AutoPilotStatus, Integer>() {
                                   @Override
                                   public Integer call(final AutoPilotStatus autoPilotStatus) {
                                       return (int) autoPilotStatus.headingBug;
                                   }
                               }))
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
                            final float oldHeading = heading;
                            heading = headingStatus.heading;
                            headingDeltaRate = headingStatus.headingDeltaRate;

                            if (headingDeltaRate > DELTA_DEADZONE
                                    || Math.abs(heading - oldHeading) > 0) {
                                postInvalidateOnAnimation();
                            }
                        }
                    })
        );

        subscriptions.add(
                bugDial.detents(1, 1)
                       .map(new Func1<Integer, Integer>() {
                           @Override
                           public Integer call(final Integer detents) {
                               return (int) headingBug + detents;
                           }
                       })
                       .map(modulo(360))
                       .doOnNext(setBugDegrees)
                       .doOnNext(headingBugOverrides)
                       .subscribe(headingBugObserver)
        );
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        final long now = SystemClock.uptimeMillis();
        if (lastFrame > 0) {
            long delta = now - lastFrame;
            onUpdate(delta);
        }
        lastFrame = now;

        canvas.save();
        canvas.translate(0, -dialOffset);

        final float center = (getRight() - getLeft()) / 2f;
        int start = canvas.save();
        canvas.rotate(-heading, center, center);
        onDrawMarkers(canvas, center);

        canvas.save();
        canvas.rotate(headingBug, center, center);
        onDrawBug(canvas, center);
        canvas.restoreToCount(start);

        airplaneArtist.draw(canvas, airplanePaint);
        canvas.restore();

        if (headingDeltaRate > DELTA_DEADZONE) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int dialWidth = getMeasuredWidth() / 4;
        int dialSpec = MeasureSpec.makeMeasureSpec(dialWidth, MeasureSpec.EXACTLY);

        bugDial.measure(dialSpec, dialSpec);
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
        int width = r - l;
        int dialSize = bugDial.getMeasuredWidth();
        bugDial.layout(width - dialSize, width - dialSize, width, width);
    }

    private void onDrawMarkers(final Canvas canvas, final float center) {
        canvas.save();
        for (int degree=0; degree < 360; degree += TICK_INTERVAL) {
            final float length = degree % 10 == 0
                ? tickMajor
                : tickMinor;

            final float end = totalOffset + length;
            canvas.drawLine(center, totalOffset, center, end, tickPaint);

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
        final float length = tickOffset + tickMinor + (tickMajor + tickMinor) / 2;
        final float separation = bugPaint.getStrokeWidth();

        final float start = dialOffset;
        final float end = start + length;
        canvas.drawLine(center - separation, start, center - separation, end, bugPaint);
        canvas.drawLine(center + separation, start, center + separation, end, bugPaint);
    }

    private void onUpdate(final long deltaMillis) {
        heading += headingDeltaRate * (deltaMillis / 1000f);
    }

    private static Path prepareAirplanePath(final float offset, final float width) {

        final float center = width / 2;
        final float unit = width / 10 * AIRPLANE_SCALE;
        final float half = unit / 2;
        final float tiny = unit / 4;

        final Path newPath = new Path();
        newPath.moveTo(center, offset + tiny);
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
