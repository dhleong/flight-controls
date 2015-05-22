package net.dhleong.ctrlf.ui.base;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import rx.subscriptions.CompositeSubscription;

/**
 * All instruments want to be the same size;
 *  It is up to a special layout to resize them
 *  to fit the screen
 *
 * @author dhleong
 */
public class BaseInstrumentView extends View {

    /** dips */
    static final int MAX_WIDTH = 220;

    protected final CompositeSubscription subscriptions = new CompositeSubscription();

    public BaseInstrumentView(final Context context) {
        super(context);
    }

    public BaseInstrumentView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        subscriptions.unsubscribe();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int square;
        if (widthMode == MeasureSpec.EXACTLY) {
            // so be it...
            square = widthSize;
        } else {
            // uhh....
            square = (int) (MAX_WIDTH * getResources().getDisplayMetrics().density);

            if (widthMode == MeasureSpec.AT_MOST) {
                square = Math.min(square, widthSize);
            }
        }

        setMeasuredDimension(square, square);
    }
}
