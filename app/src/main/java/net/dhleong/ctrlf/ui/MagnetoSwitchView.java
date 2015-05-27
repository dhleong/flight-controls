package net.dhleong.ctrlf.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import net.dhleong.ctrlf.R;
import rx.Observable;
import rx.subjects.PublishSubject;

import static net.dhleong.ctrlf.util.RxUtil.pickInstancesOf;
import static net.dhleong.ctrlf.util.UiUtil.angle;
import static net.dhleong.ctrlf.util.UiUtil.angleDelta;

/**
 * Currently just for handling the key ignition/magneto
 *  management stuff, but could be abstracted to
 *  handle generic rotate-y switches
 *
 * @author dhleong
 */
public class MagnetoSwitchView extends View {

    public enum MagnetoMode {
        OFF(false, false),
        RIGHT(false, true),
        LEFT(true, false),
        BOTH(true, true),
        START(true, true);

        public final boolean hasLeft;
        public final boolean hasRight;

        MagnetoMode(boolean hasLeft, boolean hasRight) {
            this.hasLeft = hasLeft;
            this.hasRight = hasRight;
        }
    }

    static class Notch {
        final Object tag;
        final float rotationDegrees;
        final int shiftOnRelease;
        private final int labelResId;
        private String label;

        public Notch(final Object tag, final float rotationDegrees, final int labelResId) {
            this(tag, rotationDegrees, labelResId, 0);
        }

        public Notch(final Object tag,
                final float rotationDegrees, final int labelResId,
                final int shiftOnRelease) {
            this.tag = tag;
            this.rotationDegrees = rotationDegrees;
            this.labelResId = labelResId;
            this.shiftOnRelease = shiftOnRelease;
        }

        public String getLabel(final Context context) {
            final String existing = label;
            if (existing != null) return existing;

            return label = context.getString(labelResId);
        }
    }

    private static final int KEY_FILL = 0xFFd8d8d8;
    private static final int KEY_STROKE = 0xFFc7c7c7;

    static final float ANGLE_SCALE = 0.8f;

    static final int WIDTH = 120;
    static final int HEIGHT = 120;

    static final float COLUMN_SIZE = 0.5f;
    static final float KEY_SIZE = 0.8f;

    private final Notch[] notches = {
            new Notch(MagnetoMode.OFF,   270, R.string.off),
            new Notch(MagnetoMode.RIGHT, 315, R.string.key_right),
            new Notch(MagnetoMode.LEFT,  360, R.string.key_left),
            new Notch(MagnetoMode.BOTH,  400, R.string.key_both),
            new Notch(MagnetoMode.START, 440, R.string.key_start, -1),
    };

    private final Paint columnPaint;
    private final Paint keyPaint;
    private final Paint labelPaint;

    private final float density;
    private final RectF keyRect = new RectF();

    private int currentNotch = 0;
    private float currentRotation = notches[currentNotch].rotationDegrees;

    private double lastAngle;

    private PublishSubject<Object> eventTags = PublishSubject.create();

    public MagnetoSwitchView(final Context context) {
        this(context, null);
    }

    public MagnetoSwitchView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        density = context.getResources().getDisplayMetrics().density;

        columnPaint = new Paint();
        columnPaint.setColor(0xFF333333);

        keyPaint = new Paint();
        keyPaint.setColor(KEY_FILL);
        keyPaint.setStrokeWidth(2 * density);

        labelPaint = new Paint();
        labelPaint.setColor(0xFF111111);
        labelPaint.setTextAlign(Align.CENTER);
        labelPaint.setTextSize(12 * density);
        labelPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        final float width = getWidth();
        final float height = getHeight();
        final float small = Math.min(width, height);

        final float colRadius = .5f * small * COLUMN_SIZE;
        final float keyRadius = .5f * small * KEY_SIZE;
        final float colOffset = keyRadius - colRadius;

        canvas.save();
        canvas.translate(width / 2f, height - colRadius - colOffset);
        canvas.drawCircle(0, 0,
                colRadius,
                columnPaint);

        for (Notch notch : notches) {
            canvas.save();
            canvas.rotate(notch.rotationDegrees);
            canvas.drawText(notch.getLabel(getContext()),
                    0, -colRadius,
                    labelPaint);
            canvas.restore();
        }

        final float keySize = 8 * density;
        final float keyCorner = 4 * density;
        keyRect.set(-keySize, -keyRadius, keySize, keyRadius);
        canvas.rotate(currentRotation);

        keyPaint.setStyle(Style.FILL);
        keyPaint.setColor(KEY_FILL);
        canvas.drawRoundRect(keyRect, keyCorner, keyCorner, keyPaint);

        keyPaint.setStyle(Style.STROKE);
        keyPaint.setColor(KEY_STROKE);
        canvas.drawRoundRect(keyRect, keyCorner, keyCorner, keyPaint);

        canvas.restore();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final float density = getResources().getDisplayMetrics().density;

        final int width = (int) (WIDTH * density);
        final int height = (int) (HEIGHT * density);
        setMeasuredDimension(width, height);
    }

    @Override
    @TargetApi(VERSION_CODES.LOLLIPOP)
    public boolean onTouchEvent(final MotionEvent event) {

        final float width = getWidth();
        final float height = getHeight();
        final float small = Math.min(width, height);

        final float colRadius = .5f * small * COLUMN_SIZE;
        final float keyRadius = .5f * small * KEY_SIZE;
        final float colOffset = keyRadius - colRadius;

        final float centerX = width / 2f;
        final float centerY = height - colRadius - colOffset;
        final float x = event.getX();
        final float y = event.getY();
        final float dcX = x - centerX; // delta-to-center X
        final float dcY = y - centerY; // delta-to-center Y

        final int highestNotch = notches.length - 1;
        final int thisNotch = currentNotch;

        switch (event.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
            lastAngle = angle(dcY, dcX);

            // we will ALWAYS steal touch events
            getParent().requestDisallowInterceptTouchEvent(true);
            break;

        case MotionEvent.ACTION_MOVE:
            final double angle = angle(dcY, dcX);
            final double newRotation = currentRotation
                    + ANGLE_SCALE * Math.toDegrees(angleDelta(lastAngle, angle));
            lastAngle = angle;
            invalidate();

            if (thisNotch < highestNotch
                    && newRotation >= notches[thisNotch+1].rotationDegrees) {

                performNotchMoved(1);
            } else if (thisNotch > 0
                    && newRotation <= notches[thisNotch-1].rotationDegrees) {

                performNotchMoved(-1);
            }

            if (newRotation < notches[0].rotationDegrees) {
                currentRotation = notches[0].rotationDegrees;
            } else if (newRotation > notches[highestNotch].rotationDegrees) {
                currentRotation = notches[highestNotch].rotationDegrees;
            } else {
                currentRotation = (float) newRotation;
            }
            break;

        case MotionEvent.ACTION_UP:
            final int shift = notches[currentNotch].shiftOnRelease;
            performNotchMoved(shift);
            break;
        }

        return true;
    }

    public void performNotchMoved(final int notchesMoved) {
        if (notchesMoved != 0) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        } else {
            // no movement; light feedback on release
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        }

        setCurrentNotch(currentNotch + notchesMoved);

        // TODO notify
        eventTags.onNext(notches[currentNotch].tag);
    }

    /**
     * @return An Observable stream of event tags
     *  from the specified notches
     */
    public Observable<Object> eventTags() {
        return eventTags;
    }

    public Observable<MagnetoMode> modeChanges() {
        return eventTags().lift(pickInstancesOf(MagnetoMode.class));
    }

    public void setCurrentNotch(final int notchIndex) {
        if (notchIndex < 0 || notchIndex >= notches.length) {
            throw new IllegalArgumentException("Invalid notch index " + notchIndex);
        }

        currentNotch = notchIndex;

        // TODO animate
        currentRotation = notches[currentNotch].rotationDegrees;
        postInvalidateOnAnimation();
    }

    public void setCurrentMode(final MagnetoMode mode) {
        // cheating? perhaps...
        setCurrentNotch(mode.ordinal());
    }

}
