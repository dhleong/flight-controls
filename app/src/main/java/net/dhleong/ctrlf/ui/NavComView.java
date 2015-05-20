package net.dhleong.ctrlf.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;
import net.dhleong.ctrlf.ui.art.FrequencyArtist;
import net.dhleong.ctrlf.ui.base.BaseLedView;
import net.dhleong.ctrlf.util.RadioUtil;
import net.dhleong.ctrlf.util.RxUtil;
import rx.Observable;
import rx.android.view.ViewObservable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

import java.util.concurrent.atomic.AtomicBoolean;

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
    static final Integer INNER_DETENTS = 50;
    static final Integer INNER_DETENTS_PULLED = 25;

    private final FrequencyArtist comFrequencyArtist = new FrequencyArtist();
    private final FrequencyArtist comStandbyArtist = new FrequencyArtist();
    private final FrequencyArtist navFrequencyArtist = new FrequencyArtist();
    private final FrequencyArtist navStandbyArtist = new FrequencyArtist();
    private final RectF frequencyRect = new RectF();

    // public for easy functional testing
    public final SwapButton comSwap;
    public final FineDialView comDial;
    public final SwapButton navSwap;
    public final FineDialView navDial;

    private int comFrequency, comStandbyFrequency;
    private int navFrequency, navStandbyFrequency;

    private AtomicBoolean comPulled = new AtomicBoolean(false);
    private AtomicBoolean navPulled = new AtomicBoolean(false);

    private final BehaviorSubject<Integer> comStandbySubject = BehaviorSubject.create();
    private final Observable<Void> comSwaps;

    private final BehaviorSubject<Integer> navStandbySubject = BehaviorSubject.create();
    private final Observable<Void> navSwaps;

    @SuppressWarnings("FieldCanBeLocal")
    private final Action1<Integer> setComStandbyFrequency = new Action1<Integer>() {
        @Override
        public void call(final Integer khz) {
            setComStandbyFrequency(khz);
        }
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final Action1<Integer> setNavStandbyFrequency = new Action1<Integer>() {
        @Override
        public void call(final Integer khz) {
            setNavStandbyFrequency(khz);
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

        navSwap = new SwapButton(context);
        navDial = new FineDialView(context);
        addView(navSwap);
        addView(navDial);

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

        navSwaps = ViewObservable.clicks(navSwap)
                                 .map(RxUtil.CLICK_TO_VOID);
        navSwaps.subscribe(new Action1<Void>() {
            @Override
            public void call(final Void aVoid) {
                final int oldActive = navFrequency;
                final int oldStandby = navStandbyFrequency;
                setNavFrequency(oldStandby);
                setNavStandbyFrequency(oldActive);
            }
        });

        // connect events
        comDial.detents(switchPulled(comPulled), OUTER_DETENTS)
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(final Integer detents) {
                        return comStandbyArtist.getFrequency() + detents;
                    }
                })
                .map(RadioUtil.COM_FREQ_LIMIT)
                .doOnNext(setComStandbyFrequency)
                .subscribe(comStandbySubject);
        navDial.detents(switchPulled(navPulled), OUTER_DETENTS)
               .map(new Func1<Integer, Integer>() {
                   @Override
                   public Integer call(final Integer detents) {
                       return navStandbyArtist.getFrequency() + detents;
                   }
               })
               .map(RadioUtil.NAV_FREQ_LIMIT)
               .doOnNext(setNavStandbyFrequency)
               .subscribe(navStandbySubject);

        comDial.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                comPulled.set(!comPulled.get());
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }
        });
        navDial.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                navPulled.set(!navPulled.get());
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }
        });
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

    public int getNavFrequency() {
        return navFrequencyArtist.getFrequency();
    }

    public int getNavStandbyFrequency() {
        return navStandbyArtist.getFrequency();
    }

    public Observable<Integer> comStandbyFrequencies() {
        return comStandbySubject;
    }
    public Observable<Void> comFrequencySwaps() {
        return comSwaps;
    }

    public Observable<Integer> navStandbyFrequencies() {
        return navStandbySubject;
    }
    public Observable<Void> navFrequencySwaps() {
        return navSwaps;
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

    public void setNavFrequency(final int khz) {
        // NB: We could just invalidate the Nav area....
        if (khz > 0) navFrequency = khz;
        navFrequencyArtist.setFrequency(khz);
        invalidate();
    }
    public void setNavStandbyFrequency(final int khz) {
        // NB: We could just invalidate the standby area....
        if (khz > 0) navStandbyFrequency = khz;
        navStandbyArtist.setFrequency(khz);
        invalidate();
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);

        if (enabled) {
            setComFrequency(comFrequency);
            setComStandbyFrequency(comStandbyFrequency);
            setNavFrequency(navFrequency);
            setNavStandbyFrequency(navStandbyFrequency);
        } else {
            setComFrequency(-1);
            setComStandbyFrequency(-1);
            setNavFrequency(-1);
            setNavStandbyFrequency(-1);
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

        canvas.translate(frequencyRect.width(), 0);
        drawFrequency(canvas, navFrequencyArtist);

        // after the third since our X will be nice
        //  and aligned ono the center
        final Paint paint = comFrequencyArtist.getPaint();
        final float oldStrokeWidth = paint.getStrokeWidth();
        paint.setStrokeWidth(1);
        canvas.drawLine(0, 0, 0, frequencyRect.height(), paint);
        paint.setStrokeWidth(oldStrokeWidth);

        canvas.translate(frequencyRect.width(), 0);
        drawFrequency(canvas, navStandbyArtist);

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
        navDial.measure(widthMeasureSpec, heightMeasureSpec);
        navSwap.measure(widthMeasureSpec, heightMeasureSpec);

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
        navFrequencyArtist.setDrawRect(frequencyRect);
        navStandbyArtist.setDrawRect(frequencyRect);

        setMeasuredDimension(width, (int) (frequencyRect.height()
                + comDial.getMeasuredHeight()
                + paddingTop + paddingBottom));

        onMeasured();
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {

        final int paddingLeft = getPaddingLeft();
        final int comDialLeft = (int) (frequencyRect.width() + (frequencyRect.width() / 2f));
        final int navDialLeft = (int) (3 * frequencyRect.width() + (frequencyRect.width() / 2f));
        final int comSwapLeft = paddingLeft;
        final int navSwapLeft = paddingLeft + (int) (2 * frequencyRect.width());

        layoutDialAndSwap(comDial, comSwap, comDialLeft, comSwapLeft);
        layoutDialAndSwap(navDial, navSwap, navDialLeft, navSwapLeft);
    }

    private void layoutDialAndSwap(final FineDialView dial, final SwapButton swap,
            final int dialLeft, final int swapLeft) {

        final int top = (int) (frequencyRect.height() + getPaddingTop());

        final int dialWidth = dial.getMeasuredWidth();
        final int dialHeight = dial.getMeasuredHeight();
        final int dialHalfWidth = (int) (dialWidth / 2f);

        dial.layout(dialLeft - dialHalfWidth, top, dialLeft + dialHalfWidth, top + dialHeight);

        // align vertically with the dial
        final int swapTop = top + (dialHeight / 2) - (swap.getMeasuredHeight() / 2);
        swap.layout(swapLeft,
                swapTop,
                swapLeft + swap.getMeasuredWidth(),
                swapTop + swap.getMeasuredHeight());
    }

    private static Func0<Integer> switchPulled(final AtomicBoolean isPulled) {
        return new Func0<Integer>() {
            @Override
            public Integer call() {
                return isPulled.get() ? INNER_DETENTS_PULLED : INNER_DETENTS;
            }
        };
    }

}
