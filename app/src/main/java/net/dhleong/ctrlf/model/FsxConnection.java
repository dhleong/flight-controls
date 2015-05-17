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
import net.dhleong.ctrlf.util.RadioUtil;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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

    enum RadioEvent {
        COM1_SWAP, COM1_STANDBY
    }

    enum DataType {
        RADIO_STATUS;

        static final DataType[] types = values();
        static DataType fromInt(final int input) {
            return types[input];
        }
    }

    final BehaviorSubject<IOException> ioexs = BehaviorSubject.create();

    final PublishSubject<Void> com1SwapSubject = PublishSubject.create();
    final BehaviorSubject<Integer> standbyCom1Subject = BehaviorSubject.create();
    final BehaviorSubject<RadioStatus> radioStatusSubject = BehaviorSubject.create();

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
        sc.mapClientEventToSimEvent(RadioEvent.COM1_STANDBY, "COM_STBY_RADIO_SET");
        sc.mapClientEventToSimEvent(RadioEvent.COM1_SWAP, "COM_STBY_RADIO_SWAP");

        // bind data types
        RadioStatus.bindDataDefinition(sc, DataType.RADIO_STATUS);

        // rx subscriptions
        com1SwapSubject.subscribeOn(Schedulers.io())
                       .subscribe(new IOAction<Void>(ioexs) {
                           @Override
                           protected void perform(final Void aVoid) throws IOException {
                               sc.transmitClientEvent(CLIENT_ID, RadioEvent.COM1_SWAP,
                                       0, GroupId.GROUP_0,
                                       SimConnectConstants.EVENT_FLAG_GROUPID_IS_PRIORITY);
                           }
                       });
        standbyCom1Subject.subscribeOn(Schedulers.io())
                          .debounce(250, TimeUnit.MILLISECONDS)
                          .subscribe(new IOAction<Integer>(ioexs) {
                              @Override
                              protected void perform(final Integer frequency) throws
                                      IOException {
                                  final int param = RadioUtil.frequencyAsParam(frequency);
                                  sc.transmitClientEvent(CLIENT_ID, RadioEvent.COM1_STANDBY,
                                          param,
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
        }
    }

    @Override
    public void handleException(final SimConnect simConnect, final RecvException e) {
        Log.w(TAG, "exception: " + e + ":" + e.getException());
        // TODO ?
    }

    @Override
    public Observer<Void> getCom1SwapObserver() {
        return com1SwapSubject;
    }
    @Override
    public Observer<Integer> getStandbyCom1Observer() {
        return standbyCom1Subject;
    }

    @Override
    public Observable<RadioStatus> radioStatus() {
        return radioStatusSubject;
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
}
