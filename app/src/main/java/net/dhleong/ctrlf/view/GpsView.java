package net.dhleong.ctrlf.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import net.dhleong.ctrlf.R;
import net.dhleong.ctrlf.ui.FineDialView;

import java.util.List;

/**
 * @author dhleong
 */
public class GpsView extends ViewGroup {

    @InjectView(R.id.dial) FineDialView dial;

    @InjectViews({R.id.range_up, R.id.range_down, R.id.direct,
    R.id.menu, R.id.clear, R.id.enter}) List<View> rightButtons;

    @InjectViews({R.id.nearest, R.id.obs, R.id.message, R.id.flight_plan,
    R.id.terrain, R.id.procedure}) List<View> bottomButtons;

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
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        dial.measure(widthMeasureSpec, heightMeasureSpec);

        final int spec = MeasureSpec.makeMeasureSpec(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        for (final View v : rightButtons) {
            v.measure(spec, spec);
        }
        for (final View v : bottomButtons) {
            v.measure(spec, spec);
        }
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

        final int rightH = dialT - pt;
        final int eachRight = rightH / rightButtons.size();
        int top = pt;
        for (final View v : rightButtons) {
            v.layout(pr - v.getMeasuredWidth(), top, pr, top + v.getMeasuredHeight());
            top += eachRight;
        }


        final int bottomW = dialL - pl;
        final int allOnBottom = bottomW / bottomButtons.size();
        final boolean splitRow = bottomButtons.get(0).getMeasuredWidth() > allOnBottom;
        final int eachBottom = splitRow ? bottomW / (bottomButtons.size() / 2) : allOnBottom;
        top = dialT;
        int left = pt;
        for (final View v : bottomButtons) {
            final int width = v.getMeasuredWidth();
            final int height = v.getMeasuredHeight();
            v.layout(left, top, left + width, top + height);
            left += eachBottom;

            if (splitRow && left + width / 2 >= dialL) {
                top += v.getMeasuredHeight();
                left = pt;
            }
        }

    }
}
