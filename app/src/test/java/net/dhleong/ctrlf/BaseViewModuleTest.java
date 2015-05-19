package net.dhleong.ctrlf;

import android.app.Application;
import android.view.View;
import android.view.ViewGroup;
import net.dhleong.ctrlf.module.TestModule;
import org.junit.Before;
import org.robolectric.Robolectric;

import java.util.ArrayList;
import java.util.List;

import static org.robolectric.Robolectric.shadowOf;

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

        // ensure all views are "attached" (that's where we
        //  bind to observers)
        final List<View> workspace = new ArrayList<>();
        workspace.add(view);
        while (!workspace.isEmpty()) {
            final View next = workspace.remove(0);
            if (next instanceof ViewGroup) {
                final ViewGroup group = (ViewGroup) next;
                for (int i=0; i < group.getChildCount(); i++) {
                    workspace.add(group.getChildAt(i));
                }
            }

            shadowOf(next).callOnAttachedToWindow();
        }
    }

    protected abstract ViewType inflateView(final Application context);

    protected abstract ModuleType createModule();
}
