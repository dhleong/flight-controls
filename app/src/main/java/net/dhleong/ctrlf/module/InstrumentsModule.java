package net.dhleong.ctrlf.module;

import dagger.Module;
import dagger.Provides;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.HeadingStatus;
import rx.Observable;

import javax.inject.Singleton;

import static net.dhleong.ctrlf.util.RxUtil.pickInstancesOf;

/**
 * @author dhleong
 */
@Module
public class InstrumentsModule {

    @Provides @Singleton Observable<HeadingStatus> provideHeadingStatus(Connection conn) {
        return conn.dataObjects().lift(pickInstancesOf(HeadingStatus.class));
    }
}
