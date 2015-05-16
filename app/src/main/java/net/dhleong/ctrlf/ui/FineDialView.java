package net.dhleong.ctrlf.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import net.dhleong.ctrlf.R;

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

    // radians of motion between "clicks"
    static final double DETENT_ANGLE = Math.toRadians(4);

    static final int STATE_EMPTY = 0;
    static final int STATE_INNER = 1;
    static final int STATE_OUTER = 2;

    final int width, center;

    final float radiusOuter;
    final float radiusInner;
    final float radiusGap;

    final Paint outerPaint;
    final Paint innerPaint;
    final Paint gapPaint;

    private int state = STATE_EMPTY;
    private float downX, downY;
    private float lastX, lastY;
    private double downAngle, lastAngle;

    private float[] rotations = {0, 0, 0};
    private float downRotation;
    private Paint linePaint;

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
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(center, center, radiusGap, gapPaint);

        canvas.save();
        canvas.rotate((float) Math.toDegrees(rotations[STATE_OUTER]), center, center);
        canvas.drawCircle(center, center, radiusOuter, outerPaint);
//        canvas.drawLine(center, radiusOuter - outerPaint.getStrokeWidth(), center,
//                radiusOuter + outerPaint.getStrokeWidth(), linePaint);
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

    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        final float x = event.getX();
        final float y = event.getY();
        final float dcX = x - center; // delta-to-center X
        final float dcY = y - center; // delta-to-center Y
        final double hypot = Math.hypot(dcX, dcY);

        switch (event.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
            if (hypot < radiusInner) {
                state = STATE_INNER;
            } else {
                state = STATE_OUTER;
            }

            downX = lastX = x;
            downY = lastY = y;
            downAngle = lastAngle = angle(dcY, dcX);
            downRotation = rotations[state];
            break;

        case MotionEvent.ACTION_MOVE:
            final double angle = angle(dcY, dcX);
            final double delta = angleDelta(lastAngle, angle);
            rotations[state] += delta;
            lastAngle = angle;
            invalidate();

            final int detents = (int) (delta / DETENT_ANGLE);
            break;

        case MotionEvent.ACTION_UP:
            state = STATE_EMPTY;
            break;
        }

        return true;
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
        if (base < -Math.PI) return Math.PI * -2 + base;
        return base;
    }


}
