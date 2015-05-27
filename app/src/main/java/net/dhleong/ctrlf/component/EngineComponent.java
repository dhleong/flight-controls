package net.dhleong.ctrlf.component;

import dagger.Subcomponent;
import net.dhleong.ctrlf.module.EngineModule;
import net.dhleong.ctrlf.view.MagnetosView;

import javax.inject.Singleton;

/**
 * @author dhleong
 */
@Singleton
@Subcomponent(modules = EngineModule.class)
public interface EngineComponent {

    void inject(MagnetosView view);

}
