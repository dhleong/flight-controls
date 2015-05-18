package net.dhleong.ctrlf.ui.art;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.view.View;

/**
 * @author dhleong
 */
public class FrameArtist {

    private final RectF frameRect = new RectF();
    private Paint framePaint;
    private float radius;

    public void onDraw(final Canvas canvas) {
        final Paint paint = framePaint;
        if (paint != null) {
            canvas.drawRoundRect(frameRect, radius, radius, paint);
        }
    }

    public void onMeasured(final View view) {

        frameRect.set(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        frameRect.left += view.getPaddingLeft() / 2;
        frameRect.top += view.getPaddingTop() / 2;
        frameRect.right -= view.getPaddingRight() / 2;
        frameRect.bottom -= view.getPaddingBottom() / 2;

        preparePaint(view.getContext());
    }

    private void preparePaint(final Context context) {
        if (framePaint != null) return;

        final float density = context.getResources().getDisplayMetrics().density;
        framePaint = new Paint();
        framePaint.setStyle(Style.STROKE);
        framePaint.setColor(0xffCCCCCC);
        framePaint.setStrokeWidth(4 * density);

        radius = 8 * density;
    }
}
