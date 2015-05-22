package net.dhleong.ctrlf.module;

import android.content.Context;
import android.util.Log;
import dagger.Module;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.DataRequestPeriod;
import net.dhleong.ctrlf.model.DataType;
import net.dhleong.ctrlf.model.LightsStatus;
import net.dhleong.ctrlf.model.RadioStatus;
import net.dhleong.ctrlf.model.SimData;
import net.dhleong.ctrlf.model.SimEvent;
import rx.Observable;

/**
 * @author dhleong
 */
@Module
public class DummyAppModule extends AppModule {

    private static final String TAG = "ctrlf:dummy";

    public DummyAppModule(final Context app) {
        super(app);
    }

    @Override
    protected Connection buildConnection() {
        return new Connection() {
            @Override
            public void disconnect() {
            }

            @Override
            public Observable<SimData> dataObjects() {
                return Observable.just(
                        new RadioStatus(true, 124_500, 118_000, 112_000, 114_250),
                        new LightsStatus(true, false, false, true, true, false)
                );
            }

            @Override
            public Observable<Lifecycle> lifecycleEvents() {
                return Observable.just(
                        Lifecycle.CONNECTED,
                        Lifecycle.SIM_START
                );
            }

            @Override
            public Observable<Connection> connect(final String host, final int port) {
                return Observable.empty();
            }

            @Override
            public void requestData(final DataType type, final DataRequestPeriod period) {
                // ignore; we provide what want want to already
                Log.v(TAG, "requested " + type + " at " + period);
            }

            @Override
            public void sendEvent(final SimEvent event, final int param) {
                // drop it on the floor (but log for testing)
                Log.v(TAG, "sent " + event + ":" + param);
            }
        };
    }

    @Override
    protected boolean isDummy() {
        return true;
    }
}

