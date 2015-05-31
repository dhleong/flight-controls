package net.dhleong.ctrlf.model;

import android.util.Log;
import flightsim.simconnect.SimConnect;
import flightsim.simconnect.SimConnectConstants;
import flightsim.simconnect.SimConnectPeriod;
import flightsim.simconnect.recv.DispatcherTask;
import flightsim.simconnect.recv.ExceptionHandler;
import flightsim.simconnect.recv.OpenHandler;
import flightsim.simconnect.recv.QuitHandler;
import flightsim.simconnect.recv.RecvException;
import flightsim.simconnect.recv.RecvOpen;
import flightsim.simconnect.recv.RecvQuit;
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
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dhleong
 */
public class FsxConnection
        implements Connection,
                   SimObjectDataHandler,
                   ExceptionHandler,
                   OpenHandler,
                   QuitHandler {

    private static final String TAG = "ctrlf:Fsx";
    private static final String APP_NAME = "net.dhleong.ctrlf";

    // 0 means the user's plane
    private static final int CLIENT_ID = 0;

    enum GroupId {
        GROUP_0
    }

    private static class SimConnectDataTypes {
        final Map<DataType, Method> binders = new HashMap<>();
        final Map<DataType, Constructor<? extends SimData>> constructors = new HashMap<>();

        SimConnectDataTypes() {
            for (final DataType type : DataType.VALUES) {
                final Class<? extends SimData> impl = type.implementationType;
                try {
                    final Method binder = impl
                            .getDeclaredMethod("bindDataDefinition", SimConnect.class, Enum.class);
                    binder.setAccessible(true);
                    binders.put(type, binder);

                    final Constructor<? extends SimData> ctor = impl
                            .getDeclaredConstructor(RecvSimObjectData.class);
                    ctor.setAccessible(true);
                    constructors.put(type, ctor);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }

        public void bindDataDefinition(final DataType type, final SimConnect connect) {
            try {
                binders.get(type).invoke(null, connect, type);
            } catch (final Exception e) {
                throw new RuntimeException("Could not construct " + type, e);
            }
        }

        public SimData construct(final DataType type, final RecvSimObjectData data) {
            try {
                return constructors.get(type).newInstance(data);
            } catch (final Exception e) {
                throw new RuntimeException("Could not construct " + type, e);
            }
        }
    }
    static final SimConnectDataTypes dataTypes = new SimConnectDataTypes();

    final BehaviorSubject<IOException> ioexs = BehaviorSubject.create();

    // these let us (potentially) queue up events before we're ready
    final ReplaySubject<SimData> dataObjectsSubject = ReplaySubject.createWithSize(16);
    final ReplaySubject<PendingEvent> eventQueue = ReplaySubject.createWithSize(16);
    final Subject<Lifecycle, Lifecycle> lifecycleSubject = PublishSubject.create();

    Lifecycle currentState = Lifecycle.DISCONNECTED;

    SimConnect simConnect;
    DispatchThread thread;

    public FsxConnection() {
        ioexs.subscribe(new Action1<IOException>() {
            @Override
            public void call(final IOException e) {
                e.printStackTrace();
            }
        });

        // keep track of the latest state
        lifecycleSubject.subscribe(new Action1<Lifecycle>() {
            @Override
            public void call(final Lifecycle lifecycle) {
                currentState = lifecycle;
            }
        });
    }

    /** Called when the connection is established */
    void init(final SimConnect sc) throws IOException {

        // register listeners
        final DispatcherTask dt = new DispatcherTask(sc);
        dt.addOpenHandler(this);
        dt.addQuitHandler(this);
        dt.addExceptionHandler(this);
        dt.addSimObjectDataHandler(this);

        // map events
        for (final SimEvent ev : SimEvent.values()) {
            sc.mapClientEventToSimEvent(ev, ev.getSimConnectEventName());
        }

        // bind data VALUES
        for (final DataType type : DataType.VALUES) {
            dataTypes.bindDataDefinition(type, sc);
        }

        // rx subscription
        eventQueue.observeOn(Schedulers.io())
                  .subscribe(new IOAction<PendingEvent>(ioexs) {
                      @Override
                      protected void perform(final PendingEvent pendingEvent)
                              throws IOException {

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
    }

    @Override
    public void requestData(final DataType type, final DataRequestPeriod period) {
        final SimConnectPeriod simPeriod;
        switch (period) {
        case SINGLE: simPeriod = SimConnectPeriod.ONCE; break;
        case FAST: simPeriod = SimConnectPeriod.SIM_FRAME; break;
        default:
        case SLOW: simPeriod = SimConnectPeriod.SECOND; break;
        }

        try {
            requestData(type, simPeriod);
        } catch (IOException e) {
            ioexs.onNext(e);
        }
    }

    @Override
    public void sendEvent(final SimEvent event, final int param) {
        eventQueue.onNext(new PendingEvent(event, param));
    }

    @Override
    public void handleOpen(final SimConnect simConnect, final RecvOpen recvOpen) {
        Log.v(TAG, "opened! " + recvOpen);
        lifecycleSubject.onNext(Lifecycle.SIM_START);
    }

    @Override
    public void handleQuit(final SimConnect simConnect, final RecvQuit recvQuit) {
        Log.v(TAG, "quit! " + recvQuit);
        lifecycleSubject.onNext(Lifecycle.SIM_QUIT);
    }

    @Override
    public void handleSimObject(final SimConnect simConnect,
            final RecvSimObjectData data) {

        final DataType request = DataType.fromInt(data.getRequestID());
        dataObjectsSubject.onNext(parseSimObject(data, request));
    }

    @Override
    public void handleException(final SimConnect simConnect, final RecvException e) {
        Log.w(TAG, "exception: " + e + ":" + e.getException());
        // TODO ?
    }

    @Override
    public Observable<SimData> dataObjects() {
        return dataObjectsSubject;
    }

    @Override
    public Observable<Lifecycle> lifecycleEvents() {
        // create a new one each time so it can start with the current state
        final Observable<Lifecycle> initialEvents;
        switch (currentState) {
        case CONNECTED:
            initialEvents = Observable.just(Lifecycle.CONNECTED);
            break;
        case SIM_START:
            initialEvents = Observable.just(Lifecycle.CONNECTED, Lifecycle.SIM_START);
            break;
        default:
            initialEvents = Observable.empty();
        }

        // subscribe to the subject immediately, recording for playback.
        // I'm not entirely sure why we can't just pass the subject in
        //  directly to concat()---perhaps subscription is deferred until
        //  the initialEvents are consumed?---but this is simple enough
        final ReplaySubject<Lifecycle> subscribed = ReplaySubject.create();
        lifecycleSubject.subscribe(subscribed);

        return Observable.concat(initialEvents, subscribed);
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
                    lifecycleSubject.onNext(Lifecycle.CONNECTED);
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

        lifecycleSubject.onNext(Lifecycle.DISCONNECTED);
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

    private void requestData(final DataType type, final SimConnectPeriod period) throws IOException {
        // NB: just be lazy and reuse the data type as the request id
        simConnect.requestDataOnSimObject(type, type, CLIENT_ID, period);
    }

    private static SimData parseSimObject(final RecvSimObjectData data, final DataType request) {
        // we'll leave this method intact in case we need to special case anything
        return dataTypes.construct(request, data);
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
