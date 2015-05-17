package net.dhleong.ctrlf.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * The frequency swap button for NavComView
 *
 * @author dhleong
 */
public class SwapButton extends Button {

    private Paint paint;

    public SwapButton(final Context context) {
        this(context, null);
    }

    public SwapButton(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setStrokeWidth(4 * getResources().getDisplayMetrics().density);
        paint.setColor(0xff000000);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        final int x = getPaddingLeft();
        final int y = getPaddingTop();
        final int w = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();

        canvas.drawLine(x, y, x + w, y, paint);
        // TODO arrow heads
    }
}
