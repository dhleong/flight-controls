package net.dhleong.ctrlf.module;

import dagger.Module;
import dagger.Provides;
import net.dhleong.ctrlf.App;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.FsxConnection;

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
}
