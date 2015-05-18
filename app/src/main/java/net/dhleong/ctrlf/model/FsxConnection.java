package net.dhleong.ctrlf.model;

import android.util.Log;
import flightsim.simconnect.SimConnect;
import flightsim.simconnect.SimConnectConstants;
import flightsim.simconnect.SimConnectPeriod;
import flightsim.simconnect.recv.DispatcherTask;
import flightsim.simconnect.recv.ExceptionHandler;
import flightsim.simconnect.recv.OpenHandler;
import flightsim.simconnect.recv.RecvException;
import flightsim.simconnect.recv.RecvOpen;
import flightsim.simconnect.recv.RecvSimObjectData;
import flightsim.simconnect.recv.SimObjectDataHandler;
import net.dhleong.ctrlf.util.IOAction;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.ReplaySubject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author dhleong
 */
public class FsxConnection
        implements Connection,
                   SimObjectDataHandler,
                   ExceptionHandler,
                   OpenHandler {

    private static final String TAG = "ctrlf:Fsx";
    private static final String APP_NAME = "net.dhleong.ctrlf";

    // 0 means the user's plane
    private static final int CLIENT_ID = 0;

    enum GroupId {
        GROUP_0
    }

    enum DataType {
        RADIO_STATUS,
        LIGHT_STATUS;

        static final DataType[] types = values();
        static DataType fromInt(final int input) {
            return types[input];
        }
    }

    final BehaviorSubject<IOException> ioexs = BehaviorSubject.create();

    final BehaviorSubject<RadioStatus> radioStatusSubject = BehaviorSubject.create();
    final BehaviorSubject<LightsStatus> lightsStatusSubject = BehaviorSubject.create();

    // this lets us (potentially) queue up events before we're ready
    final ReplaySubject<PendingEvent> eventQueue = ReplaySubject.createWithSize(16);

    private final Set<SimEvent> mappedEvents = new HashSet<>();

    SimConnect simConnect;
    DispatchThread thread;

    public FsxConnection() {
        ioexs.subscribe(new Action1<IOException>() {
            @Override
            public void call(final IOException e) {
                e.printStackTrace();
            }
        });

    }

    /** Called when the connection is established */
    void init(final SimConnect sc) throws IOException {

        // register listeners
        final DispatcherTask dt = new DispatcherTask(sc);
        dt.addOpenHandler(this);
        dt.addExceptionHandler(this);
        dt.addSimObjectDataHandler(this);

        // map events
        // TODO maybe it wouldn't be terrible to put the SimConnect name into the enum?
        mapEvent(sc, SimEvent.COM1_STANDBY, "COM_STBY_RADIO_SET");
        mapEvent(sc, SimEvent.COM1_SWAP, "COM_STBY_RADIO_SWAP");
        mapEvent(sc, SimEvent.SET_TRANSPONDER, "XPNDR_SET");

        mapEvent(sc, SimEvent.STROBES_TOGGLE, "STROBES_TOGGLE");
        mapEvent(sc, SimEvent.PANEL_LIGHTS_TOGGLE, "PANEL_LIGHTS_TOGGLE");
        mapEvent(sc, SimEvent.LANDING_LIGHTS_TOGGLE, "LANDING_LIGHTS_TOGGLE");
        mapEvent(sc, SimEvent.BEACON_LIGHTS_TOGGLE, "TOGGLE_BEACON_LIGHTS");
        mapEvent(sc, SimEvent.TAXI_LIGHTS_TOGGLE, "TOGGLE_TAXI_LIGHTS");
        mapEvent(sc, SimEvent.NAV_LIGHTS_TOGGLE, "TOGGLE_NAV_LIGHTS");

        mapEvent(sc, SimEvent.SET_AP_ALTITUDE, "AP_ALT_VAR_SET_ENGLISH");

        // bind data types
        RadioStatus.bindDataDefinition(sc, DataType.RADIO_STATUS);
        LightsStatus.bindDataDefinition(sc, DataType.LIGHT_STATUS);

        // rx subscription
        eventQueue.subscribeOn(Schedulers.io())
                  .subscribe(new IOAction<PendingEvent>(ioexs) {
                      @Override
                      protected void perform(final PendingEvent pendingEvent)
                              throws IOException {

                          if (!mappedEvents.contains(pendingEvent.event)) {
                              throw new IllegalArgumentException("SimEvent "
                                      + pendingEvent.event + " was not mapped!");
                          }

                          sc.transmitClientEvent(CLIENT_ID,
                                  pendingEvent.event,
                                  pendingEvent.param,
                                  GroupId.GROUP_0,
                                  SimConnectConstants.EVENT_FLAG_GROUPID_IS_PRIORITY);
                      }
                  });

        // prepare listener thread
        thread = new DispatchThread(sc, dt);
        thread.start();

        // finally, request some data. We could perhaps rather use
        //  the change events, but we need these anyway, and 1/s shouldn't
        //  put that much strain on the network....
        // NB: just be lazy and reuse the data type as the request id
        simConnect.requestDataOnSimObject(DataType.RADIO_STATUS, DataType.RADIO_STATUS,
                CLIENT_ID, SimConnectPeriod.SECOND);

        // we only need the initial state for this
        simConnect.requestDataOnSimObject(DataType.LIGHT_STATUS, DataType.LIGHT_STATUS,
                CLIENT_ID, SimConnectPeriod.ONCE);
    }

    @Override
    public void sendEvent(final SimEvent event, final int param) {
        eventQueue.onNext(new PendingEvent(event, param));
    }

    @Override
    public void handleOpen(final SimConnect simConnect, final RecvOpen recvOpen) {
        Log.v(TAG, "opened! " + recvOpen);
    }

    @Override
    public void handleSimObject(final SimConnect simConnect,
            final RecvSimObjectData data) {

        final DataType request = DataType.fromInt(data.getRequestID());
        switch (request) {
        case RADIO_STATUS:
            // parse and dispatch
            radioStatusSubject.onNext(new RadioStatus(data));
            break;
        case LIGHT_STATUS:
            // parse and dispatch
            lightsStatusSubject.onNext(new LightsStatus(data));
            break;
        }
    }

    @Override
    public void handleException(final SimConnect simConnect, final RecvException e) {
        Log.w(TAG, "exception: " + e + ":" + e.getException());
        // TODO ?
    }

    @Override
    public Observable<RadioStatus> radioStatus() {
        return radioStatusSubject;
    }

    @Override
    public Observable<LightsStatus> lightsStatus() {
        return lightsStatusSubject;
    }

    @Override
    public Observable<Connection> connect(final String host, final int port) {
        return Observable.create(new OnSubscribe<Connection>() {
            @Override
            public void call(final Subscriber<? super Connection> subscriber) {
                try {
                    simConnect = new SimConnect(APP_NAME, host, port);
                    init(simConnect);
                    subscriber.onNext(FsxConnection.this);
                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void disconnect() {

        final DispatchThread thread = this.thread;
        if (thread != null) thread.cancel();

        final SimConnect thisConnection = simConnect;
        if (thisConnection == null) return;

        Observable.create(new OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {
                try {
                    thisConnection.close();
                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io())
          .subscribe(); // just do it (tm)
    }

    private void mapEvent(final SimConnect sc, final SimEvent simEvent,
            final String simConnectEventId) throws IOException {
        sc.mapClientEventToSimEvent(simEvent, simConnectEventId);
        mappedEvents.add(simEvent);
    }

    private static class DispatchThread extends Thread {
        private final SimConnect sc;
        private final DispatcherTask task;

        boolean running = true;

        public DispatchThread(final SimConnect sc, DispatcherTask task) {
            this.sc = sc;
            this.task = task;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    sc.callDispatch(task);
                } catch (IOException e) {
                    if (running) {
                        // TODO we should probably do something with this....
                        e.printStackTrace();
                    }
                }
            }
        }

        void cancel() {
            running = false;
        }
    }

    static class PendingEvent {
        final SimEvent event;
        final int param;

        public PendingEvent(final SimEvent event, final int param) {
            this.event = event;
            this.param = param;
        }
    }
}
