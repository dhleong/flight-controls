package net.dhleong.ctrlf.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.OnLongClick;
import net.dhleong.ctrlf.App;
import net.dhleong.ctrlf.R;
import net.dhleong.ctrlf.model.SimEvent;
import net.dhleong.ctrlf.ui.FineDialView;
import net.dhleong.ctrlf.util.RxUtil;
import net.dhleong.ctrlf.util.scopes.Named;
import rx.Observer;
import rx.android.view.ViewObservable;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;
import java.util.List;

import static net.dhleong.ctrlf.util.RxUtil.toObject;

/**
 * @author dhleong
 */
public class GpsView extends ViewGroup {
    static final SimEvent[] RIGHT_EVENTS = {
            SimEvent.GPS_ZOOM_OUT,
            SimEvent.GPS_ZOOM_IN,
            SimEvent.GPS_DIRECT,
            SimEvent.GPS_MENU,
            SimEvent.GPS_CLEAR,
            SimEvent.GPS_ENTER
    };

    static final SimEvent[] BOTTOM_EVENTS = {
            SimEvent.GPS_NEAREST,
            SimEvent.GPS_OBS,
            SimEvent.GPS_MESSAGE,
            SimEvent.GPS_FLIGHT_PLAN,
            SimEvent.GPS_TERRAIN,
            SimEvent.GPS_PROCEDURE
    };

    @InjectView(R.id.dial) FineDialView dial;
    @InjectView(R.id.menu) View menu;
    @InjectView(R.id.display) TextureView display;

    @InjectViews({R.id.range_up, R.id.range_down, R.id.direct,
    R.id.menu, R.id.clear, R.id.enter}) List<View> rightButtons;

    @InjectViews({R.id.nearest, R.id.obs, R.id.message, R.id.flight_plan,
    R.id.terrain, R.id.procedure}) List<View> bottomButtons;

    @Inject @Named("GpsToggle") Observer<Void> toggleGpsObserver;
    @Inject Observer<SimEvent> clicksObserver;

    final CompositeSubscription subscriptions = new CompositeSubscription();

    public GpsView(final Context context) {
        this(context, null);
    }

    public GpsView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ButterKnife.inject(this);

        App.provideComponent(this)
           .newGpsComponent()
           .inject(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        bindClickEvents(rightButtons, RIGHT_EVENTS);
        bindClickEvents(bottomButtons, BOTTOM_EVENTS);

        subscriptions.add(
            ViewObservable.clicks(dial)
                          .doOnNext(RxUtil.PERFORM_HAPTIC)
                          .map(toObject(SimEvent.GPS_CURSOR))
                          .subscribe(clicksObserver)
        );

        subscriptions.add(
                ViewObservable.clicks(display)
                              .doOnNext(RxUtil.PERFORM_HAPTIC)
                              .map(RxUtil.CLICK_TO_VOID)
                              .subscribe(toggleGpsObserver)
        );

        subscriptions.add(
            dial.innerDetents()
                .map(upOrDown(SimEvent.GPS_PAGE_KNOB_INC, SimEvent.GPS_PAGE_KNOB_DEC))
                .subscribe(clicksObserver)
        );

        subscriptions.add(
                dial.outerDetents()
                    .map(upOrDown(SimEvent.GPS_GROUP_KNOB_INC, SimEvent.GPS_GROUP_KNOB_DEC))
                    .subscribe(clicksObserver)
        );
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        subscriptions.unsubscribe();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        dial.measure(widthMeasureSpec, heightMeasureSpec);

        // measure the widest button and have all of them be the same width
        final int anySpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        menu.measure(anySpec, anySpec);

        final int width = MeasureSpec.makeMeasureSpec(
                menu.getMeasuredWidth(), MeasureSpec.EXACTLY);
        for (final View v : rightButtons) {
            v.measure(width, anySpec);
        }
        for (final View v : bottomButtons) {
            v.measure(width, anySpec);
        }

        final int totalWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int totalHeight = MeasureSpec.getSize(heightMeasureSpec);
        final int displayWidth = totalWidth
                - getPaddingLeft()
                - getPaddingRight()
                - menu.getMeasuredWidth();
        final int displayHeight = totalHeight
                - getPaddingTop()
                - getPaddingBottom()
                - dial.getMeasuredHeight();
        final int displayWidthSpec = MeasureSpec.makeMeasureSpec(
                displayWidth, MeasureSpec.EXACTLY);
        final int displayHeightSpec = MeasureSpec.makeMeasureSpec(
                displayHeight, MeasureSpec.EXACTLY);

        display.measure(displayWidthSpec, displayHeightSpec);
    }

    @Override
    protected void onLayout(final boolean changed,
            final int l, final int t, final int r, final int b) {

        final int pl = getPaddingLeft();
        final int pt = getPaddingTop();

        final int w = r - l - getPaddingRight() - pl;
        final int h = b - t - pt - getPaddingBottom();
        final int pr = w + pl;

        final int dialL = pr - dial.getMeasuredWidth();
        final int dialT = h + pt - dial.getMeasuredHeight();
        dial.layout(dialL, dialT,
                dialL + dial.getMeasuredWidth(),
                dialT + dial.getMeasuredHeight());

        display.layout(pl, pt,
                pl + display.getMeasuredWidth(),
                pt + display.getMeasuredHeight());

        final int rightH = dialT - pt;
        final int eachRight = rightH / rightButtons.size();
        final int width = menu.getMeasuredWidth(); // all buttons are the same
        final int eachRightLeft = pr - width;
        int top = pt;
        int height = menu.getMeasuredHeight();
        for (final View v : rightButtons) {
            v.layout(eachRightLeft, top, pr, top + height);
            top += eachRight;
        }

        final int bottomW = dialL - pl;
        final int allOnBottom = bottomW / bottomButtons.size();
        final boolean splitRow = bottomButtons.get(0).getMeasuredWidth() > allOnBottom;
        final int eachBottom = splitRow ? bottomW / (bottomButtons.size() / 2) : allOnBottom;
        top = splitRow ? dialT : dialT + bottomButtons.get(0).getMeasuredHeight();
        int left = pl;
        for (final View v : bottomButtons) {
            v.layout(left, top, left + width, top + v.getMeasuredHeight());
            left += eachBottom;

            if (splitRow && left + width / 2 >= dialL) {
                top += v.getMeasuredHeight();
                left = pt;
            }
        }

    }

    /**
     * We don't do enough of this to create an RxAndroid observable.
     *  It's tempting to use JakeWharton's NotRxAndroid but it still
     *  calls itself an RFC and warns that it can be deleted at any time...
     */
    @OnLongClick(R.id.clear) boolean onClearAll(final View v) {
        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        clicksObserver.onNext(SimEvent.GPS_CLEAR_ALL);
        return true;
    }

    void bindClickEvents(final List<View> views, final SimEvent[] events) {
        int i = 0;
        for (final View v : views) {
            final SimEvent thisEvent = events[i++];
            subscriptions.add(
                    ViewObservable.clicks(v)
                                  .doOnNext(RxUtil.PERFORM_HAPTIC)
                                  .map(toObject(thisEvent))
                                  .subscribe(clicksObserver)
            );
        }
    }

    /**
     * Returns a function that maps integers to
     *  one of two Objects based on whether that
     *  number is positive or negative.
     */
    static <T> Func1<? super Integer, SimEvent> upOrDown(
            final SimEvent up, final SimEvent down) {
        return new Func1<Integer, SimEvent>() {
            @Override
            public SimEvent call(final Integer direction) {
                return direction > 0 ? up : down;
            }
        };
    }


}
