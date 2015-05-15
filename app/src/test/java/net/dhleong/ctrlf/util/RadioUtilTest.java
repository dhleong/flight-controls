package net.dhleong.ctrlf.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class RadioUtilTest {

    @Test
    public void radioFrequencyConversion() {
        assertThat(convert(127975)).isEqualTo(0x2797);
    }

    static int convert(final int input) {
        return RadioUtil.frequencyAsParam(input);
    }
}
