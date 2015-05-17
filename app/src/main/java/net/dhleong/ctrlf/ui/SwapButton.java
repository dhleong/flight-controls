package net.dhleong.ctrlf.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * The frequency swap button for NavComView
 *
 * @author dhleong
 */
public class SwapButton extends Button {

    private final Paint paint;
    private final float arrowSize;

    public SwapButton(final Context context) {
        this(context, null);
    }

    public SwapButton(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        final float density = getResources().getDisplayMetrics().density;
        paint = new Paint();
        paint.setStrokeWidth(4 * density);
        paint.setColor(0xff000000);
        paint.setStrokeCap(Cap.ROUND);
        paint.setAntiAlias(true);

        arrowSize = 8 * density;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        final int x = getPaddingLeft();
        final int y = getMeasuredHeight() / 2;
        final int w = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        final float rx = x + w;

        canvas.drawLine(x, y, rx, y, paint);
        canvas.drawLine(x, y, x + arrowSize, y - arrowSize, paint);
        canvas.drawLine(x, y, x + arrowSize, y + arrowSize, paint);

        canvas.drawLine(rx, y, rx - arrowSize, y - arrowSize, paint);
        canvas.drawLine(rx, y, rx - arrowSize, y + arrowSize, paint);
        // TODO arrow heads
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        // TODO respect the spec
        final float density = getResources().getDisplayMetrics().density;
        final int width = (int) (60 * density);
        final int height = (int) (48 * density);
        setMeasuredDimension(width, height);
    }
}
