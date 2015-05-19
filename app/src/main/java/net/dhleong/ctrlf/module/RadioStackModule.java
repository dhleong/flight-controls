package net.dhleong.ctrlf.module;

import dagger.Module;
import dagger.Provides;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.SimEvent;
import net.dhleong.ctrlf.util.Named;
import net.dhleong.ctrlf.util.RxUtil;
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
        return RxUtil.doSend(conn, SimEvent.COM1_STANDBY);
    }

    @Provides @Named("NAV1Swap") Observer<Void> provideNav1Swap(Connection conn) {
        return RxUtil.doSend(conn, SimEvent.NAV1_SWAP);
    }

    @Provides @Named("NAV1Standby") Observer<Integer> provideStandbyNav1Observer(Connection conn) {
        return RxUtil.doSend(conn, SimEvent.NAV1_STANDBY);
    }

    @Provides @Named("XPNDR") Observer<Integer> provideTransponder(Connection conn) {
        return RxUtil.doSend(conn, SimEvent.SET_TRANSPONDER);
    }

    @Provides @Named("APSetAltitude") Observer<Integer> provideAutopilotAltitude(Connection conn) {
        return RxUtil.doSend(conn, SimEvent.SET_AP_ALTITUDE);
    }

    @Provides @Named("APMaster") Observer<Void> provideAutopilotMaster(Connection conn) {
        return RxUtil.doSend(conn, SimEvent.AP_MASTER_TOGGLE);
    }
    @Provides @Named("APNav") Observer<Void> provideAutopilotNav(Connection conn) {
        return RxUtil.doSend(conn, SimEvent.AP_NAV_TOGGLE);
    }
    @Provides @Named("APApproach") Observer<Void> provideAutopilotApr(Connection conn) {
        return RxUtil.doSend(conn, SimEvent.AP_APR_TOGGLE);
    }
    @Provides @Named("APBackCourse") Observer<Void> provideAutopilotBackCourse(Connection conn) {
        return RxUtil.doSend(conn, SimEvent.AP_BACKCOURSE_TOGGLE);
    }
    @Provides @Named("APAltitude") Observer<Void> provideApAltitudeHold(Connection conn) {
        return RxUtil.doSend(conn, SimEvent.AP_ALTITUDE_TOGGLE);
    }
    @Provides @Named("APHeading") Observer<Void> provideApHeadingHold(Connection conn) {
        return RxUtil.doSend(conn, SimEvent.AP_HEADING_TOGGLE);
    }
}
