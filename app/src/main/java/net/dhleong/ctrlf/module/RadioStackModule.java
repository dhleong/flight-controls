package net.dhleong.ctrlf.module;

import dagger.Module;
import dagger.Provides;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.RadioStatus;
import net.dhleong.ctrlf.model.SimEvent;
import net.dhleong.ctrlf.util.Named;
import net.dhleong.ctrlf.util.RxUtil;
import rx.Observable;
import rx.Observer;

/**
 *
 * @author dhleong
 */
@Module
public class RadioStackModule {

    @Provides @Named("COM1Swap") Observer<Void> provideCom1Swap(Connection conn) {
        return RxUtil.doSend(conn, SimEvent.COM1_SWAP);
    }

    @Provides @Named("COM1Standby") Observer<Integer> provideStandbyCom1Observer(Connection conn) {
//        return conn.getStandbyCom1Observer();
        return RxUtil.doSend(conn, SimEvent.COM1_STANDBY);
    }

    @Provides @Named("XPNDR") Observer<Integer> provideTransponder(Connection conn) {
//        return conn.getTransponderObserver();
        return RxUtil.doSend(conn, SimEvent.SET_TRANSPONDER);
    }

}
