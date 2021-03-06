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

    public static final Func1<? super Integer, Integer> COM_FREQ_LIMIT = RxUtil.limitRange(MIN_COM_FREQ, MAX_COM_FREQ);
    public static final Func1<? super Integer, Integer> NAV_FREQ_LIMIT = RxUtil.limitRange(MIN_NAV_FREQ, MAX_NAV_FREQ);

    public static final Func1<? super Integer, Integer> FREQ_AS_PARAM =
            new Func1<Integer, Integer>() {
                @Override
                public Integer call(final Integer freq) {
                    return frequencyAsParam(freq);
                }
            };
    public static final Func1<? super Integer, Integer> XPNDR_AS_PARAM =
            new Func1<Integer, Integer>() {
                @Override
                public Integer call(final Integer integer) {
                    return transponderAsParam(integer);
                }
            };

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

        // then it becomes hex: 0x2797
        return transponderAsParam(firstDropped);
    }

    public static int paramAsFrequency(final int param) {
        // the 100000 is assumed
        int frequency = 100000 + paramAsTransponder(param, 10);

        final int lastDigit = param & 0xf;
        if (lastDigit == 2 || lastDigit == 7) {
            // see above
            frequency += 5;
        }

        return frequency;
    }

    public static int transponderAsParam(final int code) {
        int param = 0;
        int divisor = 1;
        for (int i=0; i < 4; i++) {
            // pick off the digit, then re-interpret as hex
            final int digit = (code / divisor) % 10;
            param += digit * Math.pow(16, i);

            divisor *= 10;
        }

        return param;
    }

    public static int paramAsTransponder(final int param) {
        return paramAsTransponder(param, 1);
    }

    private static int paramAsTransponder(final int param, 
            final int initialPower) {

        // inspired by Integer.toHexString
        int frequency = 0;
        int power = initialPower;
        int index = 7;
        int number = param;
        do {
            final int digit = number & 0xf;
            frequency += digit * power;

            power *= 10;
        } while ((number >>>= 4) != 0 || (--index < 0));

        return frequency;
    }
}
