package net.dhleong.ctrlf.model;

import rx.Observable;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

/**
 * @author dhleong
 */
public interface Connection {

    enum Lifecycle {
        CONNECTED,
        SIM_START,
        SIM_QUIT,
        DISCONNECTED;

        /** Create the Subject that should be used for the lifecycleEvents method */
        public static Subject<Lifecycle,Lifecycle> createSubject() {
            // we use half the number of events so you shouldn't
            //  get a sequence like QUIT, DISCONNECT, CONNECT, START
            return ReplaySubject.createWithSize(2);
        }
    }

    void disconnect();

    Observable<SimData> dataObjects();

    Observable<Lifecycle> lifecycleEvents();

    Observable<Connection> connect(final String host, final int port);

    void requestData(final DataType type, DataRequestPeriod period);

    void sendEvent(final SimEvent event, final int param);
}
