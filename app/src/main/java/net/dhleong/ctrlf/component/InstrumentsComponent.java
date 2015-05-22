package net.dhleong.ctrlf.component;

import dagger.Subcomponent;
import net.dhleong.ctrlf.module.AutoPilotModule;
import net.dhleong.ctrlf.module.InstrumentsModule;
import net.dhleong.ctrlf.ui.HeadingIndicatorView;

import javax.inject.Singleton;

/**
 * @author dhleong
 */
@Singleton
@Subcomponent(modules = {AutoPilotModule.class, InstrumentsModule.class})
public interface InstrumentsComponent {

    void inject(HeadingIndicatorView view);
}
