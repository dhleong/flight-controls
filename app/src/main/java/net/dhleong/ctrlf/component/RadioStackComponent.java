package net.dhleong.ctrlf.component;

import dagger.Subcomponent;
import net.dhleong.ctrlf.module.RadioStackModule;
import net.dhleong.ctrlf.view.RadioStackView;

import javax.inject.Singleton;

/**
 * @author dhleong
 */
@Singleton
@Subcomponent(modules = RadioStackModule.class)
public interface RadioStackComponent {

    void inject(RadioStackView view);

}
