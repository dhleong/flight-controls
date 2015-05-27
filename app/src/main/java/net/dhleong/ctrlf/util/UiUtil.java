package net.dhleong.ctrlf.util;

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
}
