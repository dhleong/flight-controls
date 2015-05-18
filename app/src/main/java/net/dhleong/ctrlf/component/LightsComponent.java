package net.dhleong.ctrlf.component;

import dagger.Subcomponent;
import net.dhleong.ctrlf.module.LightsModule;
import net.dhleong.ctrlf.ui.LightSwitchesView;

import javax.inject.Singleton;

/**
 * @author dhleong
 */
@Singleton
@Subcomponent(modules = LightsModule.class)
public interface LightsComponent {

    void inject(LightSwitchesView view);

}
