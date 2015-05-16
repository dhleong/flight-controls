package net.dhleong.ctrlf.ui;

import android.content.Context;
import android.graphics.Canvas;
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
    private final FrequencyArtist comFrequencyArtist = new FrequencyArtist();
    private final FrequencyArtist comStandbyArtist = new FrequencyArtist();
    private final RectF frequencyRect = new RectF();

    private final FineDialView comDial;

    private float fontSize;

    public NavComView(final Context context) {
        this(context, null);
    }

    public NavComView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        setWillNotDraw(false);

        setFontSize(TypedValue.COMPLEX_UNIT_SP, DEFAULT_FONT_SIZE);

        if (attrs != null) {
            // TODO
        }

        // TODO is there a default one in the attrs?
//        if (isInEditMode()) { // FIXME commented out for testing only
            setComFrequency(128_500);
            setComStandbyFrequency(118_500);
//        }

        // build kids
        comDial = new FineDialView(context);
        addView(comDial);
    }

    public int getComFrequency() {
        return comFrequencyArtist.getFrequency();
    }

    public void setFontSize(final int unit, final int size) {
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        setFontSize(TypedValue.applyDimension(unit, size, metrics));
    }

    public void setFontSize(final float px) {
        if (Math.abs(fontSize - px) > 0.01f) {
            fontSize = px;
            requestLayout();
        }
    }

    public void setComFrequency(final int khz) {
        // NB: We could just invalidate the com area....
        comFrequencyArtist.setFrequency(khz);
        invalidate();
    }
    public void setComStandbyFrequency(final int khz) {
        // NB: We could just invalidate the standby area....
        comStandbyArtist.setFrequency(khz);
        invalidate();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();

        canvas.save();
        canvas.translate(paddingLeft, paddingTop);
        drawFrequency(canvas, comFrequencyArtist);

        canvas.translate(frequencyRect.width(), 0);
        drawFrequency(canvas, comStandbyArtist);

        canvas.restore();
    }

    private void drawFrequency(final Canvas canvas, final FrequencyArtist artist) {
        canvas.save();
        canvas.clipRect(frequencyRect);
        canvas.drawColor(0xff111111);
        artist.draw(canvas);
        canvas.restore();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        // TODO build proper specs?
        comDial.measure(widthMeasureSpec, heightMeasureSpec);

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
            height = (int) (2 * fontSize);

            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize - comDial.getMeasuredHeight());
            }
        }

        final int unpaddedWidth = width - paddingLeft - paddingRight;
//        final int unpaddedHeight = height - paddingTop - paddingBottom;

        frequencyRect.set(0, 0, unpaddedWidth / 4, height / 2);
        comFrequencyArtist.setDrawRect(frequencyRect);
        comStandbyArtist.setDrawRect(frequencyRect);

        setMeasuredDimension(width, height + comDial.getMeasuredHeight());
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {

        final int left = (int) (frequencyRect.width() + (frequencyRect.width() / 2f));
        final int top = (int) (frequencyRect.height() + getPaddingTop());

        final int dialWidth = comDial.getMeasuredWidth();
        final int dialHeight = comDial.getMeasuredHeight();
        final int dialHalfWidth = (int) (dialWidth / 2f);

        comDial.layout(left - dialHalfWidth, top, left + dialHalfWidth, top + dialHeight);
    }
}
