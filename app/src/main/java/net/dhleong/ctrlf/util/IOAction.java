package net.dhleong.ctrlf.util;

import rx.Observer;
import rx.functions.Action1;

import java.io.IOException;

/**
 * @author dhleong
 */
public abstract class IOAction<T> implements Action1<T> {

    private final Observer<IOException> exceptionObserver;

    public IOAction(Observer<IOException> exceptionObserver) {
        this.exceptionObserver = exceptionObserver;
    }

    @Override
    public void call(final T t) {
        try {
            perform(t);
        } catch (IOException e) {
            exceptionObserver.onNext(e);
        }
    }

    protected abstract void perform(final T t) throws IOException;
}
