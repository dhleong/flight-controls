package net.dhleong.ctrlf.ui.art;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import net.dhleong.ctrlf.ui.art.LedArtist.DigitLedArtist;

/**
 * Composes some LedArtists to draw an LED display
 *  with a specified number of digits
 * @author dhleong
 */
public class IntegerArtist {

    static final float OFFSET = 0.2f;

    private final RectF inputRect;
    private final DigitLedArtist[] digits;

    private final Matrix matrix = new Matrix();

    private int number;

    public IntegerArtist(final int digitsCount) {
        digits = new DigitLedArtist[digitsCount];
        inputRect = new RectF(0, 0, digitsCount + (digitsCount * OFFSET), 1f);
        for (int i=0; i < digitsCount; i++) {
            digits[i] = new DigitLedArtist();
        }
    }

    public void setDrawRect(final RectF targetRect) {
        matrix.setRectToRect(inputRect, targetRect, Matrix.ScaleToFit.CENTER);
    }

    public void draw(final Canvas canvas) {
        canvas.save();
        canvas.concat(matrix);

        final int len = digits.length;
        for (int i=0; i < len; i++) {
            digits[i].draw(canvas);
            canvas.translate(1 + OFFSET, 0);
        }

        canvas.restore();
    }

    public void setDigit(final int index, final int value) {
        if (index < 0 || index >= digits.length) {
            throw new IllegalArgumentException("Illegal digit index: " + index);
        }

        digits[index].setDigit(value);
    }

    public int toNumber() {
        int multiplicand = 1;

        boolean wasCleared = true;
        int number = 0;
        final int len = digits.length;
        for (int i=len-1; i >= 0; i--) {
            final int digit = digits[i].getDigit();
            number += digit * multiplicand;
            multiplicand *= 10;

            wasCleared &= digit == -1;
        }

        return wasCleared ? -1 : number;
    }

    public void clear() {
        for (DigitLedArtist artist : digits) {
            artist.setDigit(-1);
        }
    }

    public void setNumber(final int code) {
        int divisor = 1;
        for (int i=digits.length-1; i >= 0; i--) {
            int digit = (code / divisor) % 10;
            setDigit(i, digit);

            divisor *= 10;
        }
    }
}
