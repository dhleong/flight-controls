package net.dhleong.ctrlf.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import net.dhleong.ctrlf.R;
import net.dhleong.ctrlf.ui.art.FrameArtist;
import net.dhleong.ctrlf.ui.art.IntegerArtist;
import rx.Observable;
import rx.android.view.OnClickEvent;
import rx.android.view.ViewObservable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dhleong
 */
public class TransponderView extends ViewGroup {

    static final int DEFAULT_FONT_SIZE = 80;
    static final int VFR_CODE = 1200;

    private IntegerArtist digits = new IntegerArtist(4);
    private FrameArtist frameArtist = new FrameArtist();
    private final RectF digitsRect = new RectF();

    final List<Button> numbers;
    final Button ident;

    private int ledBgColor;

    private int cursor;
    private float fontSize;

    final PublishSubject<Integer> transponderChangesSubject =
            PublishSubject.create();

    public TransponderView(final Context context) {
        this(context, null);
    }

    public TransponderView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        setWillNotDraw(false);

        // TODO do we need to bother attrs?
        setFontSize(TypedValue.COMPLEX_UNIT_SP, DEFAULT_FONT_SIZE);
        ledBgColor = getResources().getColor(R.color.led_bg);

        // assume disabled
        setEnabled(false);

        numbers = new ArrayList<>();
        for (int i=0; i < 8; i++) {
            final Button button = new TinyButtonView(context, String.valueOf(i));
            button.setMaxEms(1);
            numbers.add(button);
            ViewObservable.clicks(button)
                    .map(new AddDigitAction(i))
                    .subscribe(transponderChangesSubject);
            addView(button, new ViewGroup.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        }

        ident = new TinyButtonView(context, "ID");
        addView(ident);
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

    public Observable<Integer> transponderChanges() {
        return transponderChangesSubject;
    }

    public void setTransponderCode(int code) {

        int divisor = 1;
        for (int i=3; i >= 0; i--) {
            int digit = (code / divisor) % 10;
            digits.setDigit(i, digit);

            divisor *= 10;
        }

        invalidate();
    }

    @Override
    public void setEnabled(final boolean enabled) {

        if (enabled == isEnabled()) {
            return;
        }

        super.setEnabled(enabled);

        if (enabled) {
            // TODO probably, remember the last value and restore
            setTransponderCode(VFR_CODE);
        } else {
            digits.clear();
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();

        canvas.save();
        canvas.translate(ident.getRight(), paddingTop);
        canvas.clipRect(digitsRect);
        canvas.drawColor(ledBgColor);
        digits.draw(canvas);
        canvas.restore();

        frameArtist.onDraw(canvas);
    }

    @Override
    protected void onLayout(final boolean changed,
            final int l, final int t, final int r, final int b) {

        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();

        final Button example = numbers.get(0);
        final int buttonHeight = example.getMeasuredHeight();
        final int buttonTop = (b - t)
                - buttonHeight
                - getPaddingBottom();
        final int buttonBottom = buttonTop + buttonHeight;
        final int width = (r - l) / 10; // TODO remove magic number
        int buttonLeft = paddingLeft;
        for (final View number : numbers) {
            number.layout(buttonLeft, buttonTop,
                    buttonLeft + width,
                    buttonBottom);
            buttonLeft += width;
        }

        ident.layout(paddingLeft, paddingTop,
                width * 2,
                paddingTop + ident.getMeasuredHeight());
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

        final int buttonWidthSpec =
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        for (final Button button : numbers) {
            button.measure(buttonWidthSpec, heightMeasureSpec);
        }
        ident.measure(buttonWidthSpec, heightMeasureSpec);

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
            height = (int) fontSize
                + paddingTop + paddingBottom;

            final Button number = numbers.get(0);
            height += number.getMeasuredHeight();

            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize);
            }
        }

        final int unpaddedWidth = width - paddingLeft - paddingRight;

        digitsRect.set(0, 0, unpaddedWidth * 3 / 4, fontSize);
        digits.setDrawRect(digitsRect);

        setMeasuredDimension(width, height);

        frameArtist.onMeasured(this);
    }

    private class AddDigitAction implements Func1<OnClickEvent, Integer> {
        private final int digit;

        public AddDigitAction(final int digit) {
            this.digit = digit;
        }

        @Override
        public Integer call(final OnClickEvent onClickEvent) {
            digits.setDigit(cursor, digit);
            cursor = (cursor + 1) % 4;
            invalidate();
            return digits.toNumber();
        }
    }
}
