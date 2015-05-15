package net.dhleong.ctrlf.ui.art;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * Composes some LedArtists to draw a Frequency display
 * @author dhleong
 */
public class FrequencyArtist {

    static final float OFFSET = 0.2f;
    static final RectF INPUT_RECT = new RectF(0, 0, 6 + (6 * OFFSET), 1f);

    // TODO add the decimal
    private final LedArtist.DigitLedArtist[] digits = new LedArtist.DigitLedArtist[6];
    private final LedArtist.PunctuationLedArtist punctuation = new LedArtist.PunctuationLedArtist();

    private final Matrix matrix = new Matrix();

    private int frequency;

    public FrequencyArtist() {
        for (int i=0; i < 6; i++) {
            digits[i] = new LedArtist.DigitLedArtist();
        }
    }

    public void setDrawRect(final RectF targetRect) {
        matrix.setRectToRect(INPUT_RECT, targetRect, Matrix.ScaleToFit.CENTER);
    }

    public void draw(final Canvas canvas) {
        canvas.save();
        canvas.concat(matrix);

        for (int i=0; i < 6; i++) {
            digits[i].draw(canvas);
            if (i == 2) {
                punctuation.draw(canvas);
            }

            canvas.translate(1 + OFFSET, 0);
        }

        canvas.restore();
    }

    public void setFrequency(final int khz) {
        frequency = khz;

        int divisor = 1;
        for (int i=0; i < 6; i++) {
            final int digit = (khz / divisor) % 10;
            digits[5 - i].setDigit(digit);

            divisor *= 10;
        }

        punctuation.setState(khz < 0 ? 0 : LedArtist.PunctuationLedArtist.DECIMAL);
    }

    public int getFrequency() {
        return frequency;
    }
}
