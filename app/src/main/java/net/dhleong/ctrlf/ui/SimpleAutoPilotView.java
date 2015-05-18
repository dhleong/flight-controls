package net.dhleong.ctrlf.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Buttons-based AutoPilot view, based on the one
 *  used in the default Cessna cockpit
 *
 * @author dhleong
 */
public class SimpleAutoPilotView extends ViewGroup {
    public SimpleAutoPilotView(final Context context) {
        this(context, null);
    }

    public SimpleAutoPilotView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r,
            final int b) {

        // TODO
    }
}
