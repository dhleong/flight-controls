package net.dhleong.ctrlf;

import android.app.Application;
import android.view.View;
import net.dhleong.ctrlf.module.TestModule;
import org.junit.Before;
import org.robolectric.Robolectric;

/**
 * @author dhleong
 */
public abstract class BaseViewModuleTest<ViewType extends View, ModuleType extends TestModule> {

    public Application context;
    public ModuleType module;

    public ViewType view;

    @Before
    public void setUp() {

        module = createModule();
        App.provider = TestProvider.from(module);

        context = Robolectric.application;
        view = inflateView(context);
        Robolectric.shadowOf(view).callOnAttachedToWindow();
    }

    protected abstract ViewType inflateView(final Application context);

    protected abstract ModuleType createModule();
}
