package net.dhleong.ctrlf.component;

import dagger.Component;
import net.dhleong.ctrlf.ConnectActivity;
import net.dhleong.ctrlf.ControlsActivity;
import net.dhleong.ctrlf.module.AppModule;
import net.dhleong.ctrlf.module.PrefsModule;

import javax.inject.Singleton;

/**
 * @author dhleong
 */
@Singleton
@Component(modules = {AppModule.class, PrefsModule.class})
public interface AppComponent {

    void inject(ConnectActivity activity);
    void inject(ControlsActivity activity);

    EngineComponent newEngineComponent();
    InstrumentsComponent newInstrumentComponent();
    LightsComponent newLightsComponent();
    RadioStackComponent newRadioStackComponent();
}
