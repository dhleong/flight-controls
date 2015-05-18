package net.dhleong.ctrlf.ui.art;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * @author dhleong
 */
public abstract class LedArtist {

    static final float SLANT = 0.1f;
    static final float FULL_SLANT = 2 * SLANT;

    private int currentState;
    private Paint activePaint, inactivePaint;

    public LedArtist() {
        activePaint = new Paint();
        activePaint.setAntiAlias(true);
        activePaint.setColor(0xffD61111);
        activePaint.setStrokeWidth(0.1f);
        activePaint.setStrokeCap(Paint.Cap.SQUARE);

        inactivePaint = new Paint(activePaint);
        inactivePaint.setColor(0xFF4E0011);
    }

    public void setState(int stateMask) {
        currentState = stateMask;
    }

    public void draw(final Canvas canvas) {
        final int state = currentState;
        int bit = 0x01;
        final int totalBits = getTotalBits();
        for (int i=0; i < totalBits; i++) {
            if ((state & bit) == bit) {
                drawBit(canvas, activePaint, bit);
            } else {
                drawBit(canvas, inactivePaint, bit);
            }

            bit <<= 1;
        }
    }

    abstract void drawBit(final Canvas canvas, final Paint paint, final int bit);

    abstract int getTotalBits();

    /** Draws a single digit */
    public static class DigitLedArtist extends LedArtist {

        /*
         * We assign a bit to each element of the digit:
         * <code>
         *  02 _
         * 04 | | 01
         *  08 -
         * 40 | | 10
         *  20 -
         * </code>
         */

        private static final int TOP_RIGHT = 0x01;
        private static final int TOP       = 0x02;
        private static final int TOP_LEFT  = 0x04;
        private static final int MIDDLE    = 0x08;
        private static final int LOW_RIGHT = 0x10;
        private static final int LOW       = 0x20;
        private static final int LOW_LEFT  = 0x40;

        private int myDigit;

        @Override
        void drawBit(final Canvas canvas, final Paint paint, final int bit) {
            final float angleOffset = SLANT;
            final float fullOffset = FULL_SLANT;
            final float middle = 0.5f;

            final float x1, y1, x2, y2;
            switch (bit) {
            case TOP_RIGHT:
                x1 = 1f; y1 = 0f;
                x2 = 1f - angleOffset; y2 = middle;
                break;
            case TOP:
                x1 = fullOffset; y1 = 0;
                x2 = 1f; y2 = 0f;
                break;
            case TOP_LEFT:
                x1 = fullOffset; y1 = 0;
                x2 = angleOffset; y2 = middle;
                break;
            case MIDDLE:
                x1 = angleOffset; y1 = middle;
                x2 = 1f - angleOffset; y2 = middle;
                break;
            case LOW_RIGHT:
                x1 = 1f - angleOffset; y1 = middle;
                x2 = 1f - fullOffset; y2 = 1f;
                break;
            case LOW:
                x1 = 0; y1 = 1f;
                x2 = 1f - fullOffset; y2 = 1f;
                break;
            case LOW_LEFT:
                x1 = 0; y1 = 1f;
                x2 = angleOffset; y2 = middle;
                break;

            default:
                throw new IllegalArgumentException("Illegal bit " + Integer.toHexString(bit));
            }

            canvas.drawLine(x1, y1, x2, y2, paint);
        }

        @Override
        int getTotalBits() {
            return 7;
        }

        /**
         * Set the digit to be drawn
         *
         * @param digit The digit in [0, 9], or -1 if the digit is "off."
         *              Any other value is illegal
         */
        public void setDigit(final int digit) {
            myDigit = digit;
            switch (digit) {
            case 0: setState(0x77); break;
            case 1: setState(0x11); break;
            case 2: setState(0x6B); break;
            case 3: setState(0x3B); break;
            case 4: setState(0x1D); break;
            case 5: setState(0x3E); break;
            case 6: setState(0x7E); break;
            case 7: setState(0x13); break;
            case 8: setState(0x7F); break;
            case 9: setState(0x1F); break;
            case -1: setState(0); break;
            default:
                throw new IllegalArgumentException("Invalid digit: " + digit + "; [0, 9] only");
            }
        }

        public int getDigit() {
            return myDigit;
        }
    }

    /** Draws punctuation. Intended to be drawn right on top of a Digit */
    public static class PunctuationLedArtist extends LedArtist {

        public static final int DECIMAL = 0x01;
        public static final int COLON = 0x03;

        @Override
        void drawBit(final Canvas canvas, final Paint paint, final int bit) {
            switch (bit) {
            case 0x01: // lower
                canvas.drawPoint(1f, 1f, paint);
                break;

            case 0x02: // upper
                canvas.drawPoint(1f + SLANT, 0.5f, paint);
                break;
            }
        }

        @Override
        int getTotalBits() {
            return 2;
        }
    }
}
