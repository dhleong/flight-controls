package net.dhleong.ctrlf.component;

import dagger.Component;
import net.dhleong.ctrlf.App;
import net.dhleong.ctrlf.ConnectActivity;
import net.dhleong.ctrlf.ControlsActivity;
import net.dhleong.ctrlf.module.AppModule;

import javax.inject.Singleton;

/**
 * @author dhleong
 */
@Singleton @Component(modules = AppModule.class)
public interface AppComponent {

    App app();
    AppModule appModule();

    void inject(ConnectActivity activity);
    void inject(ControlsActivity activity);

    RadioStackComponent newRadioStackComponent();

}
