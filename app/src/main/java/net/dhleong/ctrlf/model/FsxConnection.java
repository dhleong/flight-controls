package net.dhleong.ctrlf.model;

import flightsim.simconnect.SimConnect;
import flightsim.simconnect.SimConnectConstants;
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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author dhleong
 */
public class FsxConnection implements Connection {

    private static final String APP_NAME = "net.dhleong.ctrlf";

    // 0 means the user's plane
    private static final int CLIENT_ID = 0;

    enum GROUP_ID {
        GROUP_0
    }

    enum RadioEvents {
        COM1_STANDBY
    }

    final BehaviorSubject<IOException> ioexs = BehaviorSubject.create();

    final BehaviorSubject<Integer> standbyCom1Subject = BehaviorSubject.create();

    SimConnect simConnect;

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

        sc.mapClientEventToSimEvent(RadioEvents.COM1_STANDBY, "COM_STBY_RADIO_SET");

        standbyCom1Subject.subscribeOn(Schedulers.io())
                          .debounce(250, TimeUnit.MILLISECONDS)
                          .subscribe(new IOAction<Integer>(ioexs) {
                              @Override
                              protected void perform(final Integer frequency) throws
                                      IOException {
                                  final int param = RadioUtil.frequencyAsParam(frequency);
                                  sc.transmitClientEvent(CLIENT_ID, RadioEvents.COM1_STANDBY,
                                          param,
                                          GROUP_ID.GROUP_0,
                                          SimConnectConstants.EVENT_FLAG_GROUPID_IS_PRIORITY);
                              }
                          });
    }

    @Override
    public Observer<Integer> getStandbyCom1Observer(){
        return standbyCom1Subject;
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
}
