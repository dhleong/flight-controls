package net.dhleong.ctrlf;

import android.app.Application;
import android.content.Context;
import android.view.View;
import net.dhleong.ctrlf.component.AppComponent;
import net.dhleong.ctrlf.component.DaggerAppComponent;
import net.dhleong.ctrlf.module.AppModule;

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

    @Override
    public void onCreate() {
        super.onCreate();

        mAppComponent = DaggerAppComponent.builder()
            .appModule(new AppModule(this))
            .build();
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }

    public static AppModule provideModule(final View view) {
        return provideModule(view.getContext());
    }
    public static AppModule provideModule(final Context context) {
        return provideComponent(context).appModule();
    }

    public static AppComponent provideComponent(final View view) {
        return provideComponent(view.getContext());
    }
    public static AppComponent provideComponent(final Context context) {
        final ComponentProvider provider = App.provider;
        if (provider != null) return provider.provide();

        final App app = (App) context.getApplicationContext();
        return app.getAppComponent();
    }

}
