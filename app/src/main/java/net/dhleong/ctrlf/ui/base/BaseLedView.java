package net.dhleong.ctrlf.ui.base;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ViewGroup;
import net.dhleong.ctrlf.R;
import net.dhleong.ctrlf.ui.art.FrameArtist;

/**
 * Common functionality for Views with LED displays
 *  that do their own layout. These views also
 *  have a Frame, so call through to #onMeasured()
 *  after calling setMeasuredDimension
 *
 * @author dhleong
 */
public abstract class BaseLedView extends ViewGroup {

    private FrameArtist frameArtist = new FrameArtist();

    protected final int ledBgColor;

    protected float fontSize;

    public BaseLedView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        setWillNotDraw(false);

        setFontSize(TypedValue.COMPLEX_UNIT_SP, getDefaultFontSize());
        ledBgColor = getResources().getColor(R.color.led_bg);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // assume disabled initially
        // (we do this here instead of the constructor
        //  since subclasses won't have init'd yet)
        setEnabled(false);
    }

    protected abstract int getDefaultFontSize();

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

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        frameArtist.onDraw(canvas);
    }

    protected void onMeasured() {
        frameArtist.onMeasured(this);
    }

}
