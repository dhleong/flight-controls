package net.dhleong.ctrlf.module;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import net.dhleong.ctrlf.history.ConnectionHistorian;
import net.dhleong.ctrlf.history.HistoricalConnection;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author dhleong
 */
public class PrefsHistorianTest {

    SharedPreferences prefs;
    Editor editor;

    @Before
    public void setUp() {
        prefs = mock(SharedPreferences.class);
        editor = mock(Editor.class);

        when(prefs.edit()).thenReturn(editor);
        when(editor.putString(anyString(), anyString()))
                .thenReturn(editor);
    }

    @Test
    public void deserializeEmpty() {
        mockHistory("");
        assertThat(loadedHistory()).isEmpty();
        mockHistory(null);
        assertThat(loadedHistory()).isEmpty();
    }

    @Test
    public void deserializeOne() {
        mockHistory("192.168.1.1:1234:");
        assertThat(loadedHistory())
                .containsExactly(new HistoricalConnection("192.168.1.1", 1234));
    }

    @Test
    public void deserializeMultiple() {
        mockHistory("192.168.1.1:1234:192.168.2.2:5678:");
        assertThat(loadedHistory())
                .containsExactly(
                        new HistoricalConnection("192.168.1.1", 1234),
                        new HistoricalConnection("192.168.2.2", 5678)
                );
    }

    @Test
    public void addOne() {
        mockHistory(null);
        final ConnectionHistorian history = newHistorian();

        history.connect(new HistoricalConnection("123", 456));
        assertThat(generated(history))
                .containsExactly(new HistoricalConnection("123", 456));
        verifyWritten("123:456:");
    }

    @Test
    public void insert() {
        mockHistory("1:1:");
        final ConnectionHistorian history = newHistorian();

        history.connect(new HistoricalConnection("2", 2));
        assertThat(generated(history))
                .containsExactly(
                        new HistoricalConnection("2", 2),
                        new HistoricalConnection("1", 1)
                );
        verifyWritten("2:2:1:1:");
    }

    @Test
    public void delete() {
        mockHistory("1:1:");
        final ConnectionHistorian history = newHistorian();
        assertThat(loaded(history))
                .containsExactly(new HistoricalConnection("1", 1));

        history.delete(new HistoricalConnection("1", 1));
        assertThat(generated(history)).isEmpty();
        verifyWritten("");
    }

    @Test
    public void connect() {
        mockHistory("1:1:2:2:");
        final ConnectionHistorian history = newHistorian();
        assertThat(loaded(history))
            .containsExactly(
                    new HistoricalConnection("1", 1),
                    new HistoricalConnection("2", 2)
            );

        // connecting shifts it to the top
        history.connect(new HistoricalConnection("2", 2));
        assertThat(generated(history))
            .containsExactly(
                    new HistoricalConnection("2", 2),
                    new HistoricalConnection("1", 1)
            );

        verifyWritten("2:2:1:1:");
    }

    List<HistoricalConnection> loadedHistory() {
        return loaded(newHistorian());
    }

    List<HistoricalConnection> loaded(final ConnectionHistorian history) {
        return history.load()
                      .first()
                      .toBlocking()
                      .single();
    }

    List<HistoricalConnection> generated(final ConnectionHistorian history) {
        return history.load()
                      .skip(1)
                      .first()
                      .toBlocking()
                      .single();
    }


    ConnectionHistorian newHistorian() {
        return new PrefsModule().provideHistorian(prefs);
    }

    void mockHistory(final String rawHistory) {
        when(prefs.getString(eq(PrefsModule.HISTORY), anyString()))
                .thenReturn(rawHistory);
    }

    void verifyWritten(final String rawHistory) {
        verify(editor).putString(eq(PrefsModule.HISTORY), eq(rawHistory));
    }

}
