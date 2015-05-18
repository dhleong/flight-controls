package net.dhleong.ctrlf.module;

import dagger.Module;
import dagger.Provides;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.LightsStatus;
import net.dhleong.ctrlf.model.SimEvent;
import rx.Observable;
import rx.Observer;

/**
 * @author dhleong
 */
@Module
public class LightsModule {

    @Provides Observable<LightsStatus> provideLightsStatus(final Connection conn) {
        return conn.lightsStatus();
    }

    @Provides Observer<SimEvent> provideLightToggler(final Connection conn) {
        return new Observer<SimEvent>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(final Throwable e) {
                // ???
            }

            @Override
            public void onNext(final SimEvent simEvent) {
                conn.sendEvent(simEvent, 0);
            }
        };
    }
}
