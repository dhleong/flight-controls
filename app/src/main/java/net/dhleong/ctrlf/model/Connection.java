package net.dhleong.ctrlf.model;

import rx.Observable;
import rx.Observer;

/**
 * @author dhleong
 */
public interface Connection {

    void disconnect();

    Observer<Integer> getTransponderObserver();

    Observable<RadioStatus> radioStatus();

    Observable<Connection> connect(final String host, final int port);

    Observer<Void> getCom1SwapObserver();

    Observer<Integer> getStandbyCom1Observer();

}
