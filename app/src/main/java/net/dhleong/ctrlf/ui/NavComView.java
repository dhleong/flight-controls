package net.dhleong.ctrlf.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import net.dhleong.ctrlf.ui.art.FrequencyArtist;
import net.dhleong.ctrlf.ui.base.BaseLedView;
import net.dhleong.ctrlf.util.RadioUtil;
import net.dhleong.ctrlf.util.RxUtil;
import rx.Observable;
import rx.android.view.ViewObservable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

/**
 * One piece of NavCom equipment
 *
 * @author dhleong
 */
public class NavComView extends BaseLedView {

    static final int DEFAULT_FONT_SIZE = 70;

    /**
     * How many khz to move the frequency per outer detent
     */
    static final Integer OUTER_DETENTS = 1000;
    static final Integer INNER_DETENTS = 50; // TODO "pull" for 25

    // TODO: Actually, we have several of these
    private final FrequencyArtist comFrequencyArtist = new FrequencyArtist();
    private final FrequencyArtist comStandbyArtist = new FrequencyArtist();
    private final RectF frequencyRect = new RectF();

    // public for easy functional testing
    public final SwapButton comSwap;
    public final FineDialView comDial;

    private int comFrequency, comStandbyFrequency;

    private final BehaviorSubject<Integer> comStandbySubject = BehaviorSubject.create();
    private final Observable<Void> comSwaps;

    @SuppressWarnings("FieldCanBeLocal")
    private final Action1<Integer> setComStandbyFrequency = new Action1<Integer>() {
        @Override
        public void call(final Integer khz) {
            setComStandbyFrequency(khz);
        }
    };

    public NavComView(final Context context) {
        this(context, null);
    }

    public NavComView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode()) {
            setComFrequency(128_500);
            setComStandbyFrequency(118_500);
        }

        // build kids
        comSwap = new SwapButton(context);
        comDial = new FineDialView(context);
        addView(comSwap);
        addView(comDial);

        comSwaps = ViewObservable.clicks(comSwap)
                .map(RxUtil.CLICK_TO_VOID);
        comSwaps.subscribe(new Action1<Void>() {
            @Override
            public void call(final Void aVoid) {
                final int oldActive = comFrequency;
                final int oldStandby = comStandbyFrequency;
                setComFrequency(oldStandby);
                setComStandbyFrequency(oldActive);
            }
        });

        // connect events
        comDial.detents(INNER_DETENTS, OUTER_DETENTS)
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(final Integer detents) {
                        return comStandbyArtist.getFrequency() + detents;
                    }
                })
                .map(RadioUtil.COM_FREQ_LIMIT)
                .doOnNext(setComStandbyFrequency)
                .subscribe(comStandbySubject);
    }

    @Override
    public int getDefaultFontSize() {
        return DEFAULT_FONT_SIZE;
    }

    public int getComFrequency() {
        return comFrequencyArtist.getFrequency();
    }

    public int getComStandbyFrequency() {
        return comStandbyArtist.getFrequency();
    }

    public Observable<Integer> comStandbyFrequencies() {
        return comStandbySubject;
    }
    public Observable<Void> comFrequencySwaps() {
        return comSwaps;
    }

    public void setComFrequency(final int khz) {
        // NB: We could just invalidate the com area....
        if (khz > 0) comFrequency = khz;
        comFrequencyArtist.setFrequency(khz);
        invalidate();
    }
    public void setComStandbyFrequency(final int khz) {
        // NB: We could just invalidate the standby area....
        if (khz > 0) comStandbyFrequency = khz;
        comStandbyArtist.setFrequency(khz);
        invalidate();
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);

        if (enabled) {
            setComFrequency(comFrequency);
            setComStandbyFrequency(comStandbyFrequency);
        } else {
            setComFrequency(-1);
            setComStandbyFrequency(-1);
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();

        canvas.save();
        canvas.translate(paddingLeft, paddingTop);
        drawFrequency(canvas, comFrequencyArtist);

        canvas.translate(frequencyRect.width(), 0);
        drawFrequency(canvas, comStandbyArtist);

        canvas.restore();
    }

    private void drawFrequency(final Canvas canvas, final FrequencyArtist artist) {
        canvas.save();
        canvas.clipRect(frequencyRect);
        canvas.drawColor(ledBgColor);
        artist.draw(canvas);
        canvas.restore();
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

        // TODO build proper specs?
        comDial.measure(widthMeasureSpec, heightMeasureSpec);
        comSwap.measure(widthMeasureSpec, heightMeasureSpec);

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
            height = (int) (2 * fontSize);

            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize - comDial.getMeasuredHeight());
            }
        }

        final int unpaddedWidth = width - paddingLeft - paddingRight;
//        final int unpaddedHeight = height - paddingTop - paddingBottom;

        frequencyRect.set(0, 0, unpaddedWidth / 4, height / 2);
        comFrequencyArtist.setDrawRect(frequencyRect);
        comStandbyArtist.setDrawRect(frequencyRect);

        setMeasuredDimension(width, (int) (frequencyRect.height()
                + comDial.getMeasuredHeight()
                + paddingTop + paddingBottom));

        onMeasured();
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {

        final int paddingLeft = getPaddingLeft();
        final int left = (int) (frequencyRect.width() + (frequencyRect.width() / 2f));
        final int top = (int) (frequencyRect.height() + getPaddingTop());

        final int dialWidth = comDial.getMeasuredWidth();
        final int dialHeight = comDial.getMeasuredHeight();
        final int dialHalfWidth = (int) (dialWidth / 2f);

        comDial.layout(left - dialHalfWidth, top, left + dialHalfWidth, top + dialHeight);

        // align vertically with the dial
        final int swapTop = top + (dialHeight / 2) - (comSwap.getMeasuredHeight() / 2);
        comSwap.layout(l + paddingLeft,
                swapTop,
                l + paddingLeft + comSwap.getMeasuredWidth(),
                swapTop + comSwap.getMeasuredHeight());
    }

}
