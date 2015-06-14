package net.dhleong.ctrlf.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;

/**
 * @author dhleong
 */
public class UiUtil {
    /** @return the atan of y/x in the range [0, 2pi] */
    public static double angle(final float y, final float x) {
        return Math.PI + Math.atan2(y, x);
    }

    /**
     * Calculate the difference between the angles, taking into
     *  account crossovers (ie: down at 350, angle at 10 should be 20)
     */
    public static double angleDelta(final double downAngle, final double angle) {
        final double base = angle - downAngle;
        if (base > Math.PI) return Math.PI * 2 - base;
        if (base < -Math.PI) return Math.PI * 2 + base;
        return base;
    }

    /**
     * If the the resource points to an attribute, this
     *  will resolve the attribute
     */
    public static int resolveResource(final View view, final int resId) {

        final Context context = view.getContext();
        final Resources res = context.getResources();

        if (view.isInEditMode()) {
            // For some reason, the layout preview fails to retrieve the
            //  color using the technique below, while real Android fails
            //  to retrieve it using this method. Sigh.
            try {
                return res.getColor(resId);
            } catch (Resources.NotFoundException e) {
                // fall through
            }
        }

        try {
            final TypedValue value = new TypedValue();
            res.getValue(resId, value, true);


            if (value.type == TypedValue.TYPE_ATTRIBUTE) {
                final Resources.Theme theme = context.getTheme();
                theme.resolveAttribute(value.data, value, true);
            }

            return value.data;
        } catch (IllegalArgumentException e) {
            try {
                Class.forName("org.robolectric.Robolectric");

                // NB: we're running via robolectric; who cares?
                return 0;
            } catch (ClassNotFoundException e1) {
                // not robolectric!
                throw e;
            }
        }
    }
}
