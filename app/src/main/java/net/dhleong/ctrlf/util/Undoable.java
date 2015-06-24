package net.dhleong.ctrlf.util;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import net.dhleong.ctrlf.R;
import rx.functions.Action1;

/**
 * Utility for undoable actions on a RecyclerView
 *  using Snackbar actions
 * @author dhleong
 */
public class Undoable<T> {

    private Action1<T> onDelete;

    /** Your RecyclerView.Adapter must implement this */
    public interface Adapter<T> {
        T remove(final int position);
        void insert(final int position, final T item);
    }

    private final Runnable deleteRunnable = new Runnable() {
        @Override
        public void run() {
            onDelete.call(removed);
        }
    };

    private final RecyclerView view;
    private final Adapter<T> adapter;

    private T removed;
    private int position = -1;
    private CharSequence label;

    private Undoable(final RecyclerView view, final Adapter<T> adapter) {
        this.view = view;
        this.adapter = adapter;
    }

    public Undoable<T> remove(final int position) {
        this.position = position;
        return this;
    }

    /**
     * If you've already removed the item from the adapter,
     *  pass it along here so we can restore it on undo
     *
     * @param removed
     * @param position
     * @return
     */
    public Undoable<T> remove(final T removed, final int position) {
        this.removed = removed;
        this.position = position;
        return this;
    }

    public Undoable<T> withLabel(final CharSequence label) {
        this.label = label;
        return this;
    }

    public Undoable<T> onDelete(final Action1<T> onDelete) {
        this.onDelete = onDelete;
        return this;
    }

    public void perform() {
        checkNotNull(onDelete, "onDelete");
        if (position == -1) checkNotNull(null, "remove");

        if (removed == null) {
            removed = adapter.remove(position);
        }
        if (label == null) {
            label = view.getContext().getString(R.string.undoable_remove_generic);
        }

        if (removed == null) {
            // still no removed?
            throw new IllegalStateException("No item removed from " + adapter);
        }

        // NB: There's a bug in the current version of the library
        //  where we can't actually specify a custom duration,
        //  so we've pulled out the duration of LENGTH_LONG here.
        final long delay = 2750L;
        view.postDelayed(deleteRunnable, delay);

        Snackbar.make(view, label, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        // cancel the delete
                        view.removeCallbacks(deleteRunnable);

                        // return the item to the adapter
                        adapter.insert(position, removed);
                    }
                })
                .show();
    }

    public static <T> Undoable<T> from(final RecyclerView view,
            final Adapter<T> adapter) {
        return new Undoable<>(view, adapter);
    }

    static void checkNotNull(final Object item, final String name) {
        if (item == null) throw new IllegalArgumentException(name + "() must be called");
    }

}
