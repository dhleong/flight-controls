package net.dhleong.ctrlf;

import net.dhleong.ctrlf.component.AppComponent;
import net.dhleong.ctrlf.component.DaggerAppComponent;
import net.dhleong.ctrlf.module.AppModule;

/**
 * @author dhleong
 */
public class TestProvider implements App.ComponentProvider {

    private final AppModule appModule;

    private TestProvider(final AppModule appModule) {
        this.appModule = appModule;
    }

    @Override
    public AppComponent provide() {
        return DaggerAppComponent.builder()
                .appModule(appModule)
                .build();
    }

    public static TestProvider from(AppModule appModule) {
        return new TestProvider(appModule);
    }
}
