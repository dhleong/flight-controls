package net.dhleong.ctrlf.util;

/**
 * @author dhleong
 */
public class RadioUtil {
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
}
