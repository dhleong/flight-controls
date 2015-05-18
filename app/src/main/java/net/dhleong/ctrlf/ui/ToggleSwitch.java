package net.dhleong.ctrlf.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.Switch;

/**
 * TODO Nice vertical toggle switches
 *
 * @author dhleong
 */
public class ToggleSwitch extends Switch {
    public ToggleSwitch(final Context context) {
        this(context, null);
    }

    public ToggleSwitch(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
    }
}
