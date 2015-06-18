package net.dhleong.ctrlf.component;

import dagger.Subcomponent;
import net.dhleong.ctrlf.module.GpsModule;
import net.dhleong.ctrlf.view.GpsView;

import javax.inject.Singleton;

/**
 * @author dhleong
 */
@Singleton
@Subcomponent(modules = GpsModule.class)
public interface GpsComponent {

    void inject(GpsView view);
}
