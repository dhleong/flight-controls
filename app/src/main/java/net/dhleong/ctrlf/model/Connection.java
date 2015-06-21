package net.dhleong.ctrlf.model;

import rx.Observable;

/**
 * @author dhleong
 */
public interface Connection {

    enum Lifecycle {
        CONNECTING,

        CONNECTED,
        SIM_START,
        SIM_STOP,
        DISCONNECTED;
    }

    void disconnect();

    Observable<SimData> dataObjects();

    Observable<Lifecycle> lifecycleEvents();

    Observable<Connection> connect(final String host, final int port);

    void requestData(final DataType type, DataRequestPeriod period);

    void sendEvent(final SimEvent event, final int param);
}
