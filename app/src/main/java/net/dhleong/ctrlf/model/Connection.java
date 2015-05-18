package net.dhleong.ctrlf.model;

import rx.Observable;

/**
 * @author dhleong
 */
public interface Connection {

    void disconnect();

    Observable<SimData> dataObjects();

    Observable<Connection> connect(final String host, final int port);

    void sendEvent(final SimEvent event, final int param);
}
