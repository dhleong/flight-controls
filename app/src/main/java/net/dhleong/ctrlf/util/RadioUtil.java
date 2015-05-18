package net.dhleong.ctrlf.util;

import rx.functions.Func1;

/**
 * @author dhleong
 */
public class RadioUtil {

    static final int MIN_COM_FREQ = 118_000;
    static final int MAX_COM_FREQ = 136_000;

    // TODO confirm that these are correct in sim
    static final int MIN_NAV_FREQ = 108_000;
    static final int MAX_NAV_FREQ = 117_950;

    public static final Func1<? super Integer, Integer> COM_FREQ_LIMIT = limitRange(MIN_COM_FREQ, MAX_COM_FREQ);
    public static final Func1<? super Integer, Integer> NAV_FREQ_LIMIT = limitRange(MIN_NAV_FREQ, MAX_NAV_FREQ);

    /**
     * Create a mapping function that pins the input to be within the
     *  provided bounds, inclusive
     */
    public static Func1<Integer, Integer> limitRange(final int lowerBound, final int upperBound) {
        return new Func1<Integer, Integer>() {
            @Override
            public Integer call(final Integer input) {
                return Math.min(upperBound, Math.max(lowerBound, input));
            }
        };
    }

    /**
     * See:
     * http://forum.avsim.net/topic/224152-setting-the-radio-frequencies/
     * @param khz
     * @return
     */
    public static int frequencyAsParam(int khz) {
        // eg 127.975 MHz -> 127975 kHz

        // the last place is assumed 0, or 5 if next is 2/7
        final int lastDropped = khz / 10; // -> 12797
        final int firstDropped = lastDropped - 10000; // -> 2797

        int param = 0;
        int divisor = 1;
        for (int i=0; i < 4; i++) {
            // pick off the digit, then re-interpret as hex
            final int digit = (firstDropped / divisor) % 10;
            param += digit * Math.pow(16, i);

            divisor *= 10;
        }

        // then it becomes hex: 0x2797
        return param;
    }

    public static int paramAsFrequency(final int param) {
        // the 100000 is assumed
        int frequency = 100000;

        // inspired by Integer.toHexString
        int power = 10;
        int index = 7;
        int number = param;
        do {
            final int digit = number & 0xf;
            frequency += digit * power;

            power *= 10;
        } while ((number >>>= 4) != 0 || (--index < 0));

        final int lastDigit = param & 0xf;
        if (lastDigit == 2 || lastDigit == 7) {
            // see above
            frequency += 5;
        }

        return frequency;
    }

}
