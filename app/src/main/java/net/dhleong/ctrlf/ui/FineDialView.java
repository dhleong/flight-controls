package net.dhleong.ctrlf.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Build.VERSION_CODES;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import net.dhleong.ctrlf.R;
import net.dhleong.ctrlf.util.RxUtil;
import rx.Observable;
import rx.functions.Func0;
import rx.subjects.PublishSubject;

/**
 * The FineDialView represents a touchable knob with
 *  both an "outer" and "inner" section for large and
 *  small incremental changes, respectively
 *
 * @author dhleong
 */
public class FineDialView extends View {

    static final int DIAMETER_OUTER_DIPS = 96;
    static final int DIAMETER_INNER_DIPS = 60;
    static final int WIDTH_OUTER_DIPS = 20;

    /** radians of motion between "clicks" */
    static final double DETENT_ANGLE = Math.toRadians(35);
    /** duration in ms of the vibration when hitting a detent */
    static final long VIBRATION_MS = 15;

    static final int STATE_EMPTY = 0;
    public static final int STATE_INNER = 1;
    public static final int STATE_OUTER = 2;

    final int width, center;

    final float radiusOuter;
    final float radiusInner;
    final float radiusGap;

    final Paint outerPaint;
    final Paint innerPaint;
    final Paint gapPaint;

    final Vibrator vibrator;

    private int state = STATE_EMPTY;
    private float downX, downY;
    private float lastX, lastY;
    private double downAngle, lastAngle;
    private int lastDetents, totalDetents;

    private float[] rotations = {0, 0, 0};
    private float downRotation;
    private Paint linePaint;

    @SuppressWarnings("unchecked")
    private PublishSubject<Integer>[] detentSubjects = new PublishSubject[] {
            null,
            PublishSubject.<Integer>create(),
            PublishSubject.<Integer>create()
    };

    public FineDialView(final Context context) {
        this(context, null);
    }

    public FineDialView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        final Resources res = context.getResources();
        final float density = res.getDisplayMetrics().density;
        width = (int) (DIAMETER_OUTER_DIPS * density);
        center = width / 2;

        final float widthOuter = (WIDTH_OUTER_DIPS / 2) * density;
        radiusOuter = (DIAMETER_OUTER_DIPS / 2) * density - widthOuter;
        radiusInner = (DIAMETER_INNER_DIPS / 2) * density;

        final float widthGap = radiusOuter - radiusInner - (widthOuter / 2f);
        radiusGap = radiusInner + widthGap / 2f;

        outerPaint = new Paint();
        outerPaint.setStrokeWidth(widthOuter);
        outerPaint.setAntiAlias(true);
        outerPaint.setStyle(Style.STROKE);
        outerPaint.setColor(res.getColor(R.color.dial_outer));

        innerPaint = new Paint(outerPaint);
        innerPaint.setStyle(Style.FILL);
        innerPaint.setColor(res.getColor(R.color.dial_inner));

        gapPaint = new Paint(outerPaint);
        gapPaint.setColor(res.getColor(R.color.dial_gap));
        gapPaint.setStrokeWidth(widthGap);

        linePaint = new Paint();
        linePaint.setStrokeWidth(3 * density);
        linePaint.setColor(0xffCCCCCC);

        if (isInEditMode()) {
            vibrator = null;
        } else {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    public Observable<Integer> innerDetents() {
        return detentSubjects[STATE_INNER];
    }

    public Observable<Integer> outerDetents() {
        return detentSubjects[STATE_OUTER];
    }

    /**
     * The most convenient way to get events from this view; The inner
     *  and outer detents are merged into a single Observable, mutliplied
     *  by the appropriate multiplicand
     */
    public Observable<Integer> detents(int innerMultiplicand, int outerMultiplicand) {
        return Observable.merge(
                innerDetents().map(RxUtil.times(innerMultiplicand)),
                outerDetents().map(RxUtil.times(outerMultiplicand)));
    }

    /**
     * It is common for the inner detents to change based on a "pulled" state.
     *  This version lets you provide a Func0 that resolves to the multiplicand
     *  for the inner detent for just such an occasion
     *
     * @see #detents(int, int)
     */
    public Observable<Integer> detents(Func0<Integer> innerMultiplicand, int outerMultiplicand) {
        return Observable.merge(
                innerDetents().map(RxUtil.times(innerMultiplicand)),
                outerDetents().map(RxUtil.times(outerMultiplicand)));
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(center, center, radiusGap, gapPaint);

        canvas.save();
        canvas.rotate((float) Math.toDegrees(rotations[STATE_OUTER]), center, center);
        canvas.drawCircle(center, center, radiusOuter, outerPaint);
        final float lineCenter = center - radiusOuter;
        final float half = outerPaint.getStrokeWidth() / 2f;
        canvas.drawLine(center, lineCenter - half, center,
                lineCenter + half, linePaint);
        canvas.restore();

        canvas.save();
        canvas.rotate((float) Math.toDegrees(rotations[STATE_INNER]), center, center);
        canvas.drawCircle(center, center, radiusInner, innerPaint);
        canvas.drawLine(center, radiusInner, center,
                radiusInner - outerPaint.getStrokeWidth(), linePaint);
        canvas.restore();
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        final float x = event.getX();
        final float y = event.getY();
        final float dcX = x - center; // delta-to-center X
        final float dcY = y - center; // delta-to-center Y

        switch (event.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
            if (Math.hypot(dcX, dcY) < radiusInner) {
                state = STATE_INNER;
            } else {
                state = STATE_OUTER;
            }

            downX = lastX = x;
            downY = lastY = y;
            downAngle = lastAngle = angle(dcY, dcX);
            downRotation = rotations[state];
            lastDetents = 0; // reset!
            totalDetents = 0;

            // we will ALWAYS steal touch events
            getParent().requestDisallowInterceptTouchEvent(true);
            break;

        case MotionEvent.ACTION_MOVE:
            final double angle = angle(dcY, dcX);
            rotations[state] += angleDelta(lastAngle, angle);
            lastAngle = angle;
            invalidate();

            final double totalDelta = rotations[state] - downRotation;
            final int totalDetents = (int) (totalDelta / DETENT_ANGLE);
            final int newDetents = totalDetents - lastDetents;
            if (newDetents != 0) {
                this.totalDetents += Math.abs(newDetents);
                lastDetents = totalDetents;
                performDetentsMoved(state, newDetents);
            }
            break;

        case MotionEvent.ACTION_UP:
            state = STATE_EMPTY;

            if (this.totalDetents == 0) {
                performClick();
            }
            break;
        }

        return true;
    }

    public void performDetentsMoved(final int state, final int newDetents) {
        // always vibrate if enabled, but don't send events if disabled
        if (isEnabled()
                && getParent() instanceof ViewGroup
                && ((ViewGroup) getParent()).isEnabled()) {
            detentSubjects[state].onNext(newDetents);
        }

        if (isHapticFeedbackEnabled()) {
            vibrator.vibrate(VIBRATION_MS);
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        // TODO respect the spec?
        setMeasuredDimension(width, width);
    }

    /** @return the atan of y/x in the range [0, 2pi] */
    private static double angle(final float y, final float x) {
        return Math.PI + Math.atan2(y, x);
    }

    /**
     * Calculate the difference between the angles, taking into
     *  account crossovers (ie: down at 350, angle at 10 should be 20)
     */
    private static double angleDelta(final double downAngle, final double angle) {
        final double base = angle - downAngle;
        if (base > Math.PI) return Math.PI * 2 - base;
        if (base < -Math.PI) return Math.PI * 2 + base;
        return base;
    }

}
