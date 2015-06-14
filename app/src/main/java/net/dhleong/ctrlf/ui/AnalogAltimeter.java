package net.dhleong.ctrlf.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.os.SystemClock;
import android.util.AttributeSet;
import net.dhleong.ctrlf.App;
import net.dhleong.ctrlf.R;
import net.dhleong.ctrlf.model.AltitudeStatus;
import net.dhleong.ctrlf.ui.art.PathArtist;
import net.dhleong.ctrlf.ui.base.BaseInstrumentView;
import net.dhleong.ctrlf.util.OverridePreventer;
import net.dhleong.ctrlf.util.UiUtil;
import net.dhleong.ctrlf.util.scopes.Named;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

import javax.inject.Inject;
import java.util.Locale;

/**
 * @author dhleong
 */
public class AnalogAltimeter extends BaseInstrumentView {

    static class TenThousandsHand extends PathArtist<AnalogAltimeter> {
        public TenThousandsHand(final AnalogAltimeter view) {
            super(view);
        }

        @Override
        protected Path onCreatePath() {
            final float half = view.tickMinor / 2f;
            final float triangle = view.tickMajor;

            final float center = center();
            final Path path = new Path();
            path.moveTo(center, view.totalOffset);
            path.rLineTo(-half, 0);
            path.rLineTo(half, triangle);
            path.lineTo(center, center);
            path.lineTo(center, view.totalOffset + triangle);
            path.rLineTo(half, -triangle);
            path.close();

            return path;
        }
    }

    static class ThousandsHand extends PathArtist<AnalogAltimeter> {
        public ThousandsHand(final AnalogAltimeter view) {
            super(view);
        }

        @Override
        protected Path onCreatePath() {
            final float half = view.tickMinor / 2;
            final float triangle = view.tickMajor;

            final float center = center();
            final float small = center / 3f;

            final Path path = new Path();
            path.moveTo(center, center);
            path.rLineTo(-half, 0);
            path.rLineTo(-half, -small);
            path.rLineTo(2 * half, -triangle);
            path.rLineTo(2 * half, triangle);
            path.lineTo(center + half, center);
            path.close();

            return path;
        }
    }

    static class HundredsHand extends PathArtist<AnalogAltimeter> {
        public HundredsHand(final AnalogAltimeter view) {
            super(view);
        }

        @Override
        protected Path onCreatePath() {
            final float half = view.tickMinor / 2;
            final float triangle = view.tickMajor;

            final float center = center();
            final float small = center * 0.65f;

            final Path path = new Path();
            path.moveTo(center, center);
            path.rLineTo(-half, 0);
            path.rLineTo(0, -small);
            path.rLineTo(half, -triangle);
            path.rLineTo(half, triangle);
            path.lineTo(center + half, center);
            path.close();

            return path;
        }
    }

    private static final float TICK_MAJOR = 8;
    private static final float TICK_MINOR = 5;
    private static final float TICK_OFFSET = 10;
    private static final float DIAL_OFFSET = 10;

    /** in degrees */
    private static final float TICK_INTERVAL = 7.2f;
    private static final float DELTA_DEADZONE = 0.01f;

    /** Kohlsman is in millibars * 16 */
    private static final int KOHLSMAN_DETENTS = 4;
    private static final float DEFAULT_KOHLSMAN = 1013 * 16;

    final SmallDialView dial;

    @Inject @Named("KohlsmanMb16") Observer<Integer> kohlsmanObserver;
    @Inject Observable<AltitudeStatus> altitudeStatus;

    final OverridePreventer<Integer> kohlsmanOverrides = OverridePreventer.create();

    final Paint tickPaint, kohlsmanPaint, handPaint, colorPaint;
    final float tickMajor, tickMinor;
    final float tickOffset, dialOffset, totalOffset;
    final float textCenter;

    final TenThousandsHand tenThousandsHand = new TenThousandsHand(this);
    final ThousandsHand thousandsHand = new ThousandsHand(this);
    final HundredsHand hundredsHand = new HundredsHand(this);

    long lastFrame = 0;

    float altitude, altitudeDeltaRate;
    float kohlsmanMb = DEFAULT_KOHLSMAN;

    private Action1<? super Integer> setKohlsmanMb = new Action1<Integer>() {
        @Override
        public void call(final Integer millibarsTimes16) {
            kohlsmanMb = millibarsTimes16;
            invalidate();
        }
    };

    public AnalogAltimeter(final Context context) {
        this(context, null);
    }

    public AnalogAltimeter(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode()) {
            altitude = 3680;
            kohlsmanMb = 1013 * 16; // see above
        }

        App.provideComponent(this)
           .newInstrumentComponent()
           .inject(this);

        dial = new SmallDialView(context);
        addView(dial);

        final Resources res = getResources();
        final float density = res.getDisplayMetrics().density;

        tickMajor = TICK_MAJOR * density;
        tickMinor = TICK_MINOR * density;
        tickOffset = TICK_OFFSET * density;
        dialOffset = DIAL_OFFSET * density;
        totalOffset = tickOffset + dialOffset;

        tickPaint = new Paint();
        tickPaint.setColor(0xff111111);
        tickPaint.setStrokeCap(Paint.Cap.ROUND);
        tickPaint.setStrokeWidth(2 * density);
        tickPaint.setTextAlign(Paint.Align.CENTER);
        tickPaint.setTextSize(18 * density);
        tickPaint.setAntiAlias(true);

        kohlsmanPaint = new Paint(tickPaint);
        kohlsmanPaint.setTextAlign(Align.RIGHT);
        kohlsmanPaint.setTextSize(11 * density);

        handPaint = new Paint(tickPaint);
        handPaint.setStrokeWidth(2 * density);
        handPaint.setStyle(Style.FILL_AND_STROKE);

        colorPaint = new Paint(handPaint);
        colorPaint.setColor(UiUtil.resolveResource(this, R.color.altimeter_color));

        // pre-calculate for faster draws (and to avoid allocations)
        final FontMetrics metrics = tickPaint.getFontMetrics();
        textCenter = (tickPaint.getTextSize() + metrics.descent) / 2f;
    }

    public void setKohlsmanMb(final int millibars) {
        kohlsmanMb = millibars * 16;
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        subscriptions.add(
                altitudeStatus.observeOn(AndroidSchedulers.mainThread())
                              .lift(kohlsmanOverrides.prevent(new Func1<AltitudeStatus, Integer>() {
                                  @Override
                                  public Integer call(final AltitudeStatus status) {
                                      return Math.round(status.kohlsmanMb * 16); // kohlsman * 16
                                  }
                              }))
                              .subscribe(new Action1<AltitudeStatus>() {
                                  @Override
                                  public void call(final AltitudeStatus altitudeStatus) {
                                      final float oldAltitude = altitude;
                                      final float oldKohlsman = kohlsmanMb;
                                      altitude = altitudeStatus.altitude;
                                      altitudeDeltaRate = altitudeStatus.altitudeDeltaRate;
                                      kohlsmanMb = altitudeStatus.kohlsmanMb * 16; // kohlsman * 16

                                      if (Math.abs(altitudeDeltaRate) > DELTA_DEADZONE
                                              || Math.abs(altitude - oldAltitude) > 0
                                              || Math.abs(kohlsmanMb - oldKohlsman) > 0) {
                                          postInvalidateOnAnimation();
                                      }
                                  }
                              })
        );

        subscriptions.add(
                dial.detents(KOHLSMAN_DETENTS, KOHLSMAN_DETENTS)
                    .map(new Func1<Integer, Integer>() {
                        @Override
                        public Integer call(final Integer detents) {
                            return (int) kohlsmanMb + detents;
                        }
                    })
                    .doOnNext(setKohlsmanMb)
                    .doOnNext(kohlsmanOverrides)
                    .subscribe(kohlsmanObserver)
        );
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        final long now = SystemClock.uptimeMillis();
        if (lastFrame > 0) {
            long delta = now - lastFrame;
            onUpdate(delta);
        }
        lastFrame = now;

        canvas.save();
        canvas.translate(0, -dialOffset);

        final float width = getRight() - getLeft();
        final float center = width / 2f;
        onDrawMarkers(canvas, center);

        onDrawThousands(canvas, center, altitude / 1000f);
        onDrawHundreds(canvas, center, altitude / 100f);
        onDrawTenThousands(canvas, center, altitude / 10000f);

        canvas.drawCircle(center, center, tickMinor, tickPaint);

        canvas.drawText(String.format(
                        Locale.US,
                        "%.2f",
                        mbToInHg(kohlsmanMb / 16f)), // we store in *16
                width - totalOffset - tickMajor,
                center,
                kohlsmanPaint);

        canvas.restore();

        if (Math.abs(altitudeDeltaRate) > DELTA_DEADZONE) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int dialWidth = getMeasuredWidth() / 4;
        int dialSpec = MeasureSpec.makeMeasureSpec(dialWidth, MeasureSpec.EXACTLY);

        dial.measure(dialSpec, dialSpec);
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r,
            final int b) {

        final int width = r - l;
        final int dialSize = dial.getMeasuredWidth();

        // NB: it's typically on the left, but to avoid issues with being
        //  too close to the heading bug dial (for now) we'll put it
        //  on the right
//        dial.layout(0, width - dialSize, dialSize, width);
        dial.layout(width - dialSize, width - dialSize, width, width);
    }

    private void onDrawMarkers(final Canvas canvas, final float center) {
        canvas.save();

        int feet = 0;
        for (float degree=0; degree < 360; degree += TICK_INTERVAL) {
            final float length = feet % 100 == 0
                ? tickMajor
                : tickMinor;

            final float end = totalOffset + length;
            canvas.drawLine(center, totalOffset, center, end, tickPaint);

            if (feet % 100 == 0) {
                canvas.save();

                // rotate back on the center of the number
                // so it is drawn vertically
                canvas.rotate(-degree,
                        center,
                        end + textCenter);

                final String hundredsFeet = String.valueOf(feet / 100);
                canvas.drawText(hundredsFeet,
                        center,
                        end + tickPaint.getTextSize(),
                        tickPaint);

                canvas.restore();
            }

            canvas.rotate(TICK_INTERVAL, center, center);
            feet += 20;
        }

        canvas.restore();
    }

    private void onDrawTenThousands(final Canvas canvas, final float center,
            final float tensThousands) {
        canvas.save();
        rotateFeet(canvas, center, tensThousands);
        tenThousandsHand.draw(canvas, handPaint);
        canvas.restore();
    }

    private void onDrawThousands(final Canvas canvas, final float center,
            final float thousands) {
        canvas.save();
        rotateFeet(canvas, center, thousands);
        thousandsHand.draw(canvas, colorPaint);
        canvas.restore();
    }

    private void onDrawHundreds(final Canvas canvas, final float center,
            final float hundreds) {
        canvas.save();
        rotateFeet(canvas, center, hundreds);
        hundredsHand.draw(canvas, colorPaint);
        canvas.restore();
    }


    private void rotateFeet(final Canvas canvas, final float center,
            final float hundredsFeet) {
        // 36 degrees = 100 feet
        canvas.rotate(hundredsFeet * 36f, center, center);
    }

    private void onUpdate(final long deltaMillis) {
        altitude += altitudeDeltaRate * (deltaMillis / 1000f);
    }

    static float mbToInHg(final float millibars) {
        // from: http://www.srh.noaa.gov/images/epz/wxcalc/pressureConversion.pdf
        return 0.0295300f * millibars;
    }

}
