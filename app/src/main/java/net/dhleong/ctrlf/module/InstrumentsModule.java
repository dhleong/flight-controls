package net.dhleong.ctrlf.module;

import dagger.Module;
import dagger.Provides;
import net.dhleong.ctrlf.model.AltitudeStatus;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.HeadingStatus;
import net.dhleong.ctrlf.model.SimEvent;
import net.dhleong.ctrlf.util.RxUtil;
import net.dhleong.ctrlf.util.scopes.Named;
import rx.Observable;
import rx.Observer;

import static net.dhleong.ctrlf.util.RxUtil.pickInstancesOf;

/**
 * @author dhleong
 */
@Module
public class InstrumentsModule {

    @Provides Observable<HeadingStatus> provideHeadingStatus(Connection conn) {
        return conn.dataObjects().lift(pickInstancesOf(HeadingStatus.class));
    }

    @Provides Observable<AltitudeStatus> provideAltitudeStatus(Connection conn) {
        return conn.dataObjects().lift(pickInstancesOf(AltitudeStatus.class));
    }

    @Provides @Named("KohlsmanMb16") Observer<Integer> provideKohlsmanSetter(Connection conn) {
        return RxUtil.doSend(conn, SimEvent.ALTIMETER_KOHLSMAN_16);
    }


}
