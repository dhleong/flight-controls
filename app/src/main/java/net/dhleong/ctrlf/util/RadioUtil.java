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

        // then it becomes hex: 0x2797
        //  We should do this with just math instead of allocating
        //  a string every time, but this is a quick way to test
        return Integer.parseInt(String.valueOf(firstDropped), 16);
    }
}
