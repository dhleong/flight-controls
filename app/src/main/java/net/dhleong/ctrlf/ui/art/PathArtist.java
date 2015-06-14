package net.dhleong.ctrlf.ui.art;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/**
 * @author dhleong
 */
public abstract class PathArtist<T extends View> {

    protected final T view;

    private Path path;

    public PathArtist(T view) {
        this.view = view;
    }

    /**
     * Implement to create the Path that will be drawn.
     *  This will be called once before the first draw,
     *  then the path will be cached
     * @return A new Path
     */
    protected abstract Path onCreatePath();

    public void draw(final Canvas canvas, final Paint paint) {
        final Path existing = path;
        final Path toDraw;
        if (existing == null) {
            toDraw = path = onCreatePath();
        } else {
            toDraw = existing;
        }

        canvas.drawPath(toDraw, paint);
    }

    /** Convenience calculation */
    protected float width() {
        return view.getRight() - view.getLeft();
    }

    /** Convenience calculation */
    protected float center() {
        return width() / 2f;
    }

}
