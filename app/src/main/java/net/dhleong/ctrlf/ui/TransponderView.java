package net.dhleong.ctrlf.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import net.dhleong.ctrlf.ui.art.IntegerArtist;
import net.dhleong.ctrlf.ui.base.BaseLedView;
import net.dhleong.ctrlf.util.RxUtil;
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
public class TransponderView extends BaseLedView {

    static final int DEFAULT_FONT_SIZE = 80;
    static final int VFR_CODE = 1200;

    /**
     * Scale used to determine width of buttons.
     *  IE: this many buttons should fit on screen
     */
    public static final int PER_BUTTON_SCALE = 10;

    private IntegerArtist digits = new IntegerArtist(4);
    private final RectF digitsRect = new RectF();

    // public for testing
    public final List<Button> numbers;
    public final Button ident;

    private int cursor;
    private int oldTransponder = -1;

    final PublishSubject<Integer> transponderChangesSubject =
            PublishSubject.create();

    public TransponderView(final Context context) {
        this(context, null);
    }

    public TransponderView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        numbers = new ArrayList<>();
        for (int i=0; i < 8; i++) {
            final Button button = new TinyButtonView(context, String.valueOf(i));
            button.setMaxEms(1);
            numbers.add(button);
            ViewObservable.clicks(button)
                    .doOnNext(RxUtil.PERFORM_HAPTIC)
                    .map(new AddDigitAction(i))
                    .subscribe(transponderChangesSubject);
            addView(button, new ViewGroup.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        }

        ident = new TinyButtonView(context, "ID");
        ident.setEnabled(false);
        addView(ident);
    }

    @Override
    public int getDefaultFontSize() {
        return DEFAULT_FONT_SIZE;
    }

    public Observable<Integer> transponderChanges() {
        return transponderChangesSubject;
    }

    public int getTransponderCode() {
        return digits.toNumber();
    }

    public void setTransponderCode(int code) {
        digits.setNumber(code);
        invalidate();
    }

    @Override
    public void setEnabled(final boolean enabled) {

        if (enabled == isEnabled()) {
            return;
        }

        super.setEnabled(enabled);

        if (enabled) {
            final int old = oldTransponder;
            setTransponderCode(old > 0 ? old : VFR_CODE);
        } else {
            oldTransponder = digits.toNumber();
            digits.clear();
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        final int paddingTop = getPaddingTop();

        canvas.save();
        canvas.translate(ident.getRight(), paddingTop);
        canvas.clipRect(digitsRect);
        canvas.drawColor(ledBgColor);
        digits.draw(canvas);
        canvas.restore();
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
        final int width = (r - l) / PER_BUTTON_SCALE;
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
        onMeasured();
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
