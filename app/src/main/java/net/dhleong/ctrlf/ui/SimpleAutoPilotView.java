package net.dhleong.ctrlf.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import net.dhleong.ctrlf.R;
import net.dhleong.ctrlf.ui.art.IntegerArtist;
import net.dhleong.ctrlf.ui.base.BaseLedView;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

import java.util.Arrays;
import java.util.List;

import static net.dhleong.ctrlf.util.RadioUtil.limitRange;

/**
 * Buttons-based AutoPilot view, based on the one
 *  used in the default Cessna cockpit (assumes the
 *  heading bug is set elsewhere)
 *
 * @author dhleong
 */
public class SimpleAutoPilotView extends BaseLedView {

    private static final int DEFAULT_FONT_SIZE = 40;

    private static final int INNER_DETENTS = 100;
    private static final int OUTER_DETENTS = 1000;

    final IntegerArtist altitudeArtist = new IntegerArtist(5);
    final RectF altitudeRect = new RectF();
    final RectF ledRect = new RectF();

    // public for easier testing
    public final FineDialView dial;
    public final List<TinyButtonView> allButtons;

    private final BehaviorSubject<Integer> altitudeSubject = BehaviorSubject.create();

    @SuppressWarnings("FieldCanBeLocal")
    private final Action1<? super Integer> setTargetAltitude = new Action1<Integer>() {
        @Override
        public void call(final Integer integer) {
            setTargetAltitude(integer);
        }
    };

    public SimpleAutoPilotView(final Context context) {
        this(context, null);
    }

    public SimpleAutoPilotView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        dial = new FineDialView(context);
        addView(dial);

        final TinyButtonView apMaster = new TinyButtonView(context, context.getString(R.string.btn_autopilot));
        final TinyButtonView heading = new TinyButtonView(context, context.getString(R.string.btn_ap_heading));
        final TinyButtonView nav = new TinyButtonView(context, context.getString(R.string.btn_ap_nav));
        final TinyButtonView apr = new TinyButtonView(context, context.getString(R.string.btn_ap_nav)); // FIXME what?
        final TinyButtonView rev = new TinyButtonView(context, context.getString(R.string.btn_ap_nav)); // FIXME what?
        final TinyButtonView altitude = new TinyButtonView(context, context.getString(R.string.btn_ap_altitude));

        allButtons = Arrays.asList(apMaster, heading, nav, apr, rev, altitude);
        for (final View v : allButtons) {
            addView(v);
        }

        // connect events
        dial.detents(INNER_DETENTS, OUTER_DETENTS)
            .map(new Func1<Integer, Integer>() {
                @Override
                public Integer call(final Integer integer) {
                    System.out.println("SPIN " + altitudeArtist.toNumber() + " + " + integer);
                    return altitudeArtist.toNumber() + integer;
                }
            })
            .map(limitRange(0, 99999))
            .doOnNext(setTargetAltitude)
            .subscribe(altitudeSubject);
    }

    public Observable<Integer> targetAltitudes() {
        return altitudeSubject;
    }

    @Override
    public int getDefaultFontSize() {
        return DEFAULT_FONT_SIZE;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        if (enabled == isEnabled()) {
            return;
        }

        super.setEnabled(enabled);

        if (enabled) {
            altitudeArtist.setNumber(0);
        } else {
            altitudeArtist.clear();
        }
    }

    public void setTargetAltitude(final int altitude) {
        if (isEnabled()) {
            altitudeArtist.setNumber(altitude);
            invalidate();
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();

        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();

        canvas.translate(paddingLeft, paddingTop);
        canvas.clipRect(ledRect);
        canvas.drawColor(ledBgColor);

        canvas.translate(altitudeRect.width(), 0);
        altitudeArtist.draw(canvas);


        canvas.restore();
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r,
            final int b) {

        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        final int paddingRight = getPaddingRight();

        final Button example = allButtons.get(0);
        final int buttonHeight = example.getMeasuredHeight();
        final int buttonTop = (b - t)
                - buttonHeight
                - getPaddingBottom();
        final int buttonBottom = buttonTop + buttonHeight;
        final int width = (r - l) / 9; // TODO remove magic number
        int buttonLeft = paddingLeft;
        for (final View number : allButtons) {
            number.layout(buttonLeft, buttonTop,
                    buttonLeft + width,
                    buttonBottom);
            buttonLeft += width;
        }

        final int dialWidth = dial.getMeasuredWidth();
        final int dialLeft = (r - l) - dialWidth - paddingRight;
        dial.layout(dialLeft, paddingTop,
                dialLeft + dialWidth,
                paddingTop + dial.getMeasuredHeight());
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
        for (final Button button : allButtons) {
            button.measure(buttonWidthSpec, heightMeasureSpec);
        }

        dial.measure(widthMeasureSpec, heightMeasureSpec);

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
            height = (int) (2 * fontSize
                    + paddingTop
                    + paddingBottom);

            final Button button = allButtons.get(0);
            height += button.getMeasuredHeight();

            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize);
            }
        }

        final int unpaddedWidth = width - paddingLeft - paddingRight;

        altitudeRect.set(0, 0, unpaddedWidth / 4, fontSize);
        ledRect.set(0, 0, altitudeRect.width() * 2, altitudeRect.height() * 2);
        altitudeArtist.setDrawRect(altitudeRect);

        setMeasuredDimension(width, height);
        onMeasured();
    }
}
