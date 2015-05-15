package net.dhleong.ctrlf.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class RadioUtilTest {

    @Test
    public void radioFrequencyConversion() {
        assertThat(convert(127_975)).isEqualTo(0x2797);
        assertThat(convert(128_975)).isEqualTo(0x2897);
        assertThat(convert(122_450)).isEqualTo(0x2245);
    }

    static int convert(final int input) {
        return RadioUtil.frequencyAsParam(input);
    }
}
