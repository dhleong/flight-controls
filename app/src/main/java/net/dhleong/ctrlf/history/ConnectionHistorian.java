package net.dhleong.ctrlf.history;

import rx.Observable;

import java.util.List;

/**
 * @author dhleong
 */
public interface ConnectionHistorian {

    void connect(final HistoricalConnection connection);

    void delete(final HistoricalConnection connection);

    Observable<List<HistoricalConnection>> load();
}
