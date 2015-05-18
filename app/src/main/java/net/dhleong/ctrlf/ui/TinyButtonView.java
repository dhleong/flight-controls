package net.dhleong.ctrlf.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.TypedValue;
import android.widget.Button;

/**
 * @author dhleong
 */
public class TinyButtonView extends Button {
    private final Paint paint;
    private CharSequence text;

    public TinyButtonView(final Context context, final CharSequence text) {
        super(context);
        this.text = text;

        setWillNotDraw(false);

        paint = new Paint();
        paint.setColor(0xff111111);
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 18, getResources().getDisplayMetrics()));
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        final float top = (getMeasuredHeight() / 2f) + (paint.getTextSize() / 2f);
        final float centerX = (getRight() - getLeft()) / 2f;

        canvas.drawText(text, 0, text.length(), centerX, top, paint);
    }

}
