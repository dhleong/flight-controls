package net.dhleong.ctrlf.history;

import rx.Observable;

import java.util.List;

/**
 * @author dhleong
 */
public interface ConnectionHistorian {

    void connect(final HistoricalConnection connection);

    void delete(final HistoricalConnection connection);

    void insert(int position, HistoricalConnection removed);

    Observable<List<HistoricalConnection>> load();

}
