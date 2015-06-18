package net.dhleong.ctrlf.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.TypedValue;
import net.dhleong.ctrlf.R;

/**
 * @author dhleong
 */
public class TinyButtonView extends AppCompatButton {
    private final Paint paint;
    private CharSequence text;
    private Paint.FontMetrics fontMetrics;

    public TinyButtonView(final Context context, final AttributeSet attrs) {
        this(context, extractText(context, attrs));
    }

    public TinyButtonView(final Context context, final CharSequence text) {
        super(context);
        this.text = text;

        setWillNotDraw(false);

        setSupportBackgroundTintMode(PorterDuff.Mode.OVERLAY);
        setSupportBackgroundTintList(context.getResources().getColorStateList(R.color.button_activatable));

        // somewhat arbitrary font sizes...
        final int sizeSp;
        switch (text.length()) {
        case 1: sizeSp = 18; break;
        case 2: sizeSp = 17; break;
        default:
        case 3: sizeSp = 16; break;
        }

        paint = new Paint();
        paint.setColor(0xff111111);
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, sizeSp, getResources().getDisplayMetrics()));

        fontMetrics = paint.getFontMetrics();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        final float top = (getMeasuredHeight() / 2f)
                + (paint.getTextSize() / 2f)
                - fontMetrics.descent / 2f;
        final float centerX = (getRight() - getLeft()) / 2f;

        canvas.drawText(text, 0, text.length(), centerX, top, paint);
    }

    static CharSequence extractText(final Context context, final AttributeSet attrs) {
        final TypedArray a = context
                .obtainStyledAttributes(attrs, R.styleable.Ctrlf_TinyButton);
        final CharSequence text =
                a.getText(R.styleable.Ctrlf_TinyButton_android_text);
        a.recycle();
        return text;
    }
}
