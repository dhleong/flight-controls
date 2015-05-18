package net.dhleong.ctrlf.module;

import dagger.Module;
import dagger.Provides;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.RadioStatus;
import net.dhleong.ctrlf.util.Named;
import rx.Observable;
import rx.Observer;

/**
 * Is this module redundant...?
 *
 * @author dhleong
 */
@Module
public class RadioStackModule {

    @Provides Observable<RadioStatus> provideStatusObservable(Connection conn) {
        return conn.radioStatus();
    }

    @Provides @Named("COM1Swap") Observer<Void> provideCom1Swap(Connection conn) {
        return conn.getCom1SwapObserver();
    }

    @Provides @Named("COM1Standby") Observer<Integer> provideStandbyCom1Observer(Connection conn) {
        return conn.getStandbyCom1Observer();
    }

    @Provides @Named("XPNDR") Observer<Integer> provideTransponder(Connection conn) {
        return conn.getTransponderObserver();
    }

}
