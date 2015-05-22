package net.dhleong.ctrlf.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

/**
 * SmallDialView is a special case of FineDialView
 *  that only has the inner dial
 *
 * @author dhleong
 */
public class SmallDialView extends FineDialView {

    static final int DEFAULT_WIDTH = 48;

    public SmallDialView(final Context context) {
        super(context);
    }

    public SmallDialView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        superOnDraw(canvas);

        drawInnerDial(canvas);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

        final float density = getResources().getDisplayMetrics().density;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int dimen;
        if (widthMode == MeasureSpec.EXACTLY) {
            dimen = widthSize;
        } else {
            dimen = (int) (DEFAULT_WIDTH * density);
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            dimen = Math.min(dimen, heightSize);
        }

        setMeasuredDimension(dimen, dimen);

        width = dimen;
        center = width / 2;
        radiusInner = center;
    }

}
