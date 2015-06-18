package net.dhleong.ctrlf.module;

import dagger.Module;
import dagger.Provides;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.SimEvent;
import net.dhleong.ctrlf.util.RxUtil;
import net.dhleong.ctrlf.util.scopes.Named;
import rx.Observer;

/**
 * @author dhleong
 */
@Module
public class GpsModule {

    @Provides @Named("GpsToggle") Observer<Void> provideGpsToggleObserver(final Connection conn) {
        return RxUtil.doSend(conn, SimEvent.GPS_TOGGLE_SHOWN);
    }

    @Provides Observer<SimEvent> provideGpsButtonObserver(final Connection connection) {
        return new Observer<SimEvent>() {

            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(final Throwable e) {
                // ...?
            }

            @Override
            public void onNext(final SimEvent event) {
                connection.sendEvent(event, 0);
            }
        };
    }
}
