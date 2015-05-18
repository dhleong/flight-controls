package net.dhleong.ctrlf.module;

import dagger.Module;
import dagger.Provides;
import net.dhleong.ctrlf.App;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.FsxConnection;
import net.dhleong.ctrlf.model.RadioStatus;
import rx.Observable;

import javax.inject.Singleton;

/**
 * @author dhleong
 */
@Module
public class AppModule {

    App app;

    public AppModule(final App app) {
        this.app = app;
    }

    @Provides @Singleton AppModule provideAppModule() {
        return this;
    }

    @Provides @Singleton App provideAppContext() {
        return app;
    }

    @Provides @Singleton Connection provideConnection() { return new FsxConnection(); }

    @Provides @Singleton Observable<RadioStatus> provideStatusObservable(Connection conn) {
        return conn.radioStatus();
    }

}
