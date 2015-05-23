package net.dhleong.ctrlf.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import net.dhleong.ctrlf.App;
import net.dhleong.ctrlf.R;
import net.dhleong.ctrlf.model.AutoPilotStatus;
import net.dhleong.ctrlf.ui.art.IntegerArtist;
import net.dhleong.ctrlf.ui.base.BaseLedView;
import net.dhleong.ctrlf.util.RxUtil;
import net.dhleong.ctrlf.util.scopes.Named;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static net.dhleong.ctrlf.util.RxUtil.limitRange;

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

    /**
     * Scale used to determine width of buttons.
     *  IE: this many buttons should fit on screen
     */
    public static final int PER_BUTTON_SCALE = 9;

    final IntegerArtist altitudeArtist = new IntegerArtist(5);
    final RectF altitudeRect = new RectF();
    final RectF ledRect = new RectF();

    // public for easier testing
    public final FineDialView dial;
    public final List<TinyButtonView> allButtons;

    private final TinyButtonView apMaster, heading, nav, apr, backCourse, altitude;

    @Inject Observable<AutoPilotStatus> status;
    @Inject @Named("APSetAltitude") Observer<Integer> apSetAltitudeObserver;
    @Inject @Named("APMaster") Observer<Void> apMasterObserver;
    @Inject @Named("APNav") Observer<Void> apNavObserver;
    @Inject @Named("APApproach") Observer<Void> apApproachObserver;
    @Inject @Named("APBackCourse") Observer<Void> apBackCourseObserver;
    @Inject @Named("APAltitude") Observer<Void> apAltitudeObserver;
    @Inject @Named("APHeading") Observer<Void> apHeadingObserver;

    private CompositeSubscription subscriptions = new CompositeSubscription();

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

        apMaster = new TinyButtonView(context, context.getString(R.string.btn_autopilot));
        heading = new TinyButtonView(context, context.getString(R.string.btn_ap_heading));
        nav = new TinyButtonView(context, context.getString(R.string.btn_ap_nav));
        apr = new TinyButtonView(context, context.getString(R.string.btn_ap_apr));
        backCourse = new TinyButtonView(context, context.getString(R.string.btn_ap_rev));
        altitude = new TinyButtonView(context, context.getString(R.string.btn_ap_altitude));

        allButtons = Arrays.asList(apMaster, heading, nav, apr, backCourse, altitude);
        for (final View v : allButtons) {
            addView(v);
        }

        // inject
        App.provideComponent(this)
           .newRadioStackComponent()
           .inject(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // connect events
        subscriptions.add(
                dial.detents(INNER_DETENTS, OUTER_DETENTS)
                    .map(new Func1<Integer, Integer>() {
                        @Override
                        public Integer call(final Integer integer) {
                            return altitudeArtist.toNumber() + integer;
                        }
                    })
                    .map(limitRange(0, 99000)) // not sure what sim does, but will keep the numbers from getting weird
                    .doOnNext(setTargetAltitude)
                    .subscribe(apSetAltitudeObserver)
        );

        subscriptions.add(
                status.observeOn(AndroidSchedulers.mainThread())
                      .subscribe(new Action1<AutoPilotStatus>() {
                          @Override
                          public void call(final AutoPilotStatus autoPilotStatus) {
                              setVisibility(autoPilotStatus.available ? View.VISIBLE : View.GONE);
                              setTargetAltitude(autoPilotStatus.altitude);

                              apMaster.setActivated(autoPilotStatus.master);
                          }
                      })
        );

        bindTo(apMaster, apMasterObserver);
        bindTo(nav, apNavObserver);
        bindTo(apr, apApproachObserver);
        bindTo(backCourse, apBackCourseObserver);
        bindTo(altitude, apAltitudeObserver);
        bindTo(heading, apHeadingObserver);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        subscriptions.unsubscribe();
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

    public int getTargetAltitude() {
        return altitudeArtist.toNumber();
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
        final int width = (r - l) / PER_BUTTON_SCALE;
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

    void bindTo(final View view, final Observer<Void> observer) {
        subscriptions.add(
                ViewObservable.clicks(view)
                              .doOnNext(RxUtil.PERFORM_HAPTIC)
                              .map(RxUtil.CLICK_TO_VOID)
                              .subscribe(observer)
        );
    }

}
