package net.dhleong.ctrlf.module;

import dagger.Module;
import dagger.Provides;
import net.dhleong.ctrlf.model.AutoPilotStatus;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.SimEvent;
import net.dhleong.ctrlf.util.scopes.Named;
import net.dhleong.ctrlf.util.RxUtil;
import rx.Observable;
import rx.Observer;

import static net.dhleong.ctrlf.util.RxUtil.pickInstancesOf;

/**
 * AutoPilot gets its own module because it has so many things
 *
 * @author dhleong
 */
@Module
public class AutoPilotModule {

    @Provides Observable<AutoPilotStatus> provideStatus(Connection conn) {
        return conn.dataObjects().lift(pickInstancesOf(AutoPilotStatus.class));
    }

    @Provides @Named("APSetAltitude") Observer<Integer> provideAltitude(Connection conn) {
        return RxUtil.doSend(conn, SimEvent.SET_AP_ALTITUDE);
    }
    @Provides @Named("APHeadingBug") Observer<Integer> provideHeadingBug(Connection conn) {
        return RxUtil.doSend(conn, SimEvent.SET_AP_HEADING);
    }

    @Provides @Named("APMaster") Observer<Void> provideMaster(Connection conn) {
        return RxUtil.doSend(conn, SimEvent.AP_MASTER_TOGGLE);
    }
    @Provides @Named("APNav") Observer<Void> provideNav(Connection conn) {
        return RxUtil.doSend(conn, SimEvent.AP_NAV_TOGGLE);
    }
    @Provides @Named("APApproach") Observer<Void> provideApproach(Connection conn) {
        return RxUtil.doSend(conn, SimEvent.AP_APR_TOGGLE);
    }
    @Provides @Named("APBackCourse") Observer<Void> provideBackCourse(Connection conn) {
        return RxUtil.doSend(conn, SimEvent.AP_BACKCOURSE_TOGGLE);
    }
    @Provides @Named("APAltitude") Observer<Void> provideAltitudeHold(Connection conn) {
        return RxUtil.doSend(conn, SimEvent.AP_ALTITUDE_TOGGLE);
    }
    @Provides @Named("APHeading") Observer<Void> provideHeadingHold(Connection conn) {
        return RxUtil.doSend(conn, SimEvent.AP_HEADING_TOGGLE);
    }
}
