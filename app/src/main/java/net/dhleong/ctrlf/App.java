package net.dhleong.ctrlf;

import android.app.Application;
import android.content.Context;
import android.view.View;
import net.dhleong.ctrlf.component.AppComponent;
import net.dhleong.ctrlf.component.DaggerAppComponent;
import net.dhleong.ctrlf.module.AppModule;
import net.dhleong.ctrlf.module.DummyAppModule;

/**
 * @author dhleong
 */
public class App extends Application {

    // enable stubbing for functional tests
    public interface ComponentProvider {
        AppComponent provide();
    }
    public static ComponentProvider provider;

    private AppComponent mAppComponent;
    private boolean dummyMode;

    @Override
    public void onCreate() {
        super.onCreate();

        initComponent();
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }

    private void initComponent() {
        final AppModule module = dummyMode
                ? new DummyAppModule(this)
                : new AppModule(this);
        mAppComponent = DaggerAppComponent.builder()
            .appModule(module)
            .build();
    }

    public static AppComponent provideComponent(final View view) {
        if (view.isInEditMode()) {
            return DaggerAppComponent.builder()
                    .appModule(new DummyAppModule(view.getContext()))
                    .build();
        } else {
            return provideComponent(view.getContext());
        }
    }
    public static AppComponent provideComponent(final Context context) {
        final ComponentProvider provider = App.provider;
        if (provider != null) return provider.provide();

        final App app = (App) context.getApplicationContext();
        return app.getAppComponent();
    }

    public static void toggleDummyMode(final Context context) {
        final App app = (App) context.getApplicationContext();
        app.dummyMode = !app.dummyMode;
        app.initComponent();
    }

}
