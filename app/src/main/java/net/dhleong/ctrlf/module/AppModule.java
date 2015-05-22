package net.dhleong.ctrlf.module;

import android.content.Context;
import dagger.Module;
import dagger.Provides;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.FsxConnection;
import net.dhleong.ctrlf.model.RadioStatus;
import net.dhleong.ctrlf.util.scopes.IsDummyMode;
import rx.Observable;

import javax.inject.Singleton;

import static net.dhleong.ctrlf.util.RxUtil.pickInstancesOf;

/**
 * @author dhleong
 */
@Module
public class AppModule {

    Context app;

    public AppModule(final Context app) {
        this.app = app;
    }

    @Provides @IsDummyMode boolean provideDummyMode() {
        return isDummy();
    }

    @Provides @Singleton AppModule provideAppModule() {
        return this;
    }

    @Provides @Singleton Connection provideConnection() { return buildConnection(); }

    @Provides @Singleton Observable<Connection.Lifecycle> provideLifecycleObservable(Connection conn) {
        return conn.lifecycleEvents();
    }

    @Provides @Singleton Observable<RadioStatus> provideRadioStatusObservable(Connection conn) {
        return conn.dataObjects().lift(pickInstancesOf(RadioStatus.class));
    }

    protected Connection buildConnection() {
        return new FsxConnection();
    }

    protected boolean isDummy() {
        return false;
    }
}

