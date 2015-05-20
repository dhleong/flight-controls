package net.dhleong.ctrlf.module;

import dagger.Module;
import net.dhleong.ctrlf.App;
import net.dhleong.ctrlf.model.Connection;
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

    public DummyAppModule(final App app) {
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
            public Observable<Connection> connect(final String host, final int port) {
                return Observable.empty();
            }

            @Override
            public void sendEvent(final SimEvent event, final int param) {
                // drop it on the floor
            }
        };
    }

    @Override
    protected boolean isDummy() {
        return true;
    }
}

