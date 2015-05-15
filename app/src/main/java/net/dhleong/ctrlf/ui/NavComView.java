package net.dhleong.ctrlf.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ViewGroup;
import net.dhleong.ctrlf.ui.art.FrequencyArtist;

/**
 * One piece of NavCom equipment
 *
 * @author dhleong
 */
public class NavComView extends ViewGroup {

    static final int DEFAULT_FONT_SIZE = 70;

    // TODO: Actually, we have several of these
    private final FrequencyArtist frequencyArtist = new FrequencyArtist();
    private final RectF frequencyRect = new RectF();

    private float fontSize;
    private Paint paint = new Paint();

    public NavComView(final Context context) {
        this(context, null);
    }

    public NavComView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        setWillNotDraw(false);

        paint.setColor(0xff000000);

        fontSize = DEFAULT_FONT_SIZE * getResources().getDisplayMetrics().density; // a default size

        if (attrs != null) {
            // TODO
        }

        // TODO is there a default one in the attrs?
//        if (isInEditMode()) { // FIXME commented out for testing only
            setFrequency(128_500);
//        }
    }

    public int getFrequency() {
        return frequencyArtist.getFrequency();
    }

    public void setFontSize(final int unit, final int size) {
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        final float oldFontSize = fontSize;
        fontSize = TypedValue.applyDimension(unit, size, metrics);

        if (Math.abs(oldFontSize - fontSize) > 0.01f) {
            requestLayout();
        }
    }

    public void setFrequency(final int khz) {
        frequencyArtist.setFrequency(khz);
        invalidate();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawText("" + frequencyRect, 25, 25, paint);

        canvas.save();
        canvas.clipRect(frequencyRect);
        canvas.drawColor(0xff111111);
        frequencyArtist.draw(canvas);
        canvas.restore();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width, height;
        if (widthMode == MeasureSpec.EXACTLY
                || widthMode == MeasureSpec.AT_MOST) {
            // take as much as we can
            width = widthSize;
        } else {
            // uhh....
            width = (int) (fontSize * 24);
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            // so be it
            height = heightSize;
        } else {
            height = (int) (4 * fontSize);

            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize);
            }
        }

        frequencyRect.set(0, 0, width / 4, height / 4);
        frequencyArtist.setDrawRect(frequencyRect);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {

    }
}
