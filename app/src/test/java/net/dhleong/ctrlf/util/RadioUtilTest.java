package net.dhleong.ctrlf.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class RadioUtilTest {

    @Test
    public void convertToParam() {
        assertThat(asParam(127_975)).isEqualTo(0x2797);
        assertThat(asParam(128_975)).isEqualTo(0x2897);
        assertThat(asParam(122_450)).isEqualTo(0x2245);
    }

    @Test
    public void convertToFrequency() {
        assertThat(asFrequency(0x2797)).isEqualTo(127_975);
        assertThat(asFrequency(0x2897)).isEqualTo(128_975);
        assertThat(asFrequency(0x2245)).isEqualTo(122_450);
    }

    static int asFrequency(final int input) {
        return RadioUtil.paramAsFrequency(input);
    }

    static int asParam(final int input) {
        return RadioUtil.frequencyAsParam(input);
    }
}
