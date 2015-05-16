package net.dhleong.ctrlf.module;

import dagger.Module;
import dagger.Provides;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.util.Named;
import rx.Observer;

/**
 * @author dhleong
 */
@Module
public class RadioStackModule {

    @Provides @Named("COM1") Observer<Integer> provideStandbyCom1Observer(Connection conn) {
        return conn.getStandbyCom1Observer();
    }
}
