package net.dhleong.ctrlf.model;

import rx.Observable;
import rx.Observer;

/**
 * @author dhleong
 */
public interface Connection {

    void disconnect();

    Observable<Connection> connect(final String host, final int port);

    Observer<Integer> getStandbyCom1Observer();

}
