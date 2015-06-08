package net.dhleong.ctrlf.util;

import org.junit.Before;
import org.junit.Test;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class OverridePreventerTest {

    PublishSubject<Integer> input;
    PublishSubject<Integer> output;
    OverridePreventer<Integer> overrides;

    private List<Integer> inputStream = new ArrayList<>();

    @Before
    public void setUp() {
        input = PublishSubject.create();
        output = PublishSubject.create();
        overrides = OverridePreventer.create();

        input.lift(overrides.prevent())
             .subscribe(storeIn(inputStream));
        output.doOnNext(overrides).subscribe();
    }

    @Test
    public void noOutputMeansNoInputFiltered() {
        input.onNext(1);
        assertThat(inputStream).containsExactly(1);

        input.onNext(2);
        assertThat(inputStream).containsExactly(1, 2);
    }

    @Test
    public void ignoreUntilInputMatchesOutput() {
        output.onNext(42);

        input.onNext(1);
        assertThat(inputStream).isEmpty();
        input.onNext(2);
        assertThat(inputStream).isEmpty();

        input.onNext(42);
        assertThat(inputStream).containsExactly(42);
    }

    private <T> Action1<T> storeIn(final List<T> destination) {
        return new Action1<T>() {
            @Override
            public void call(final T item) {
                destination.add(item);
            }
        };
    }

}
