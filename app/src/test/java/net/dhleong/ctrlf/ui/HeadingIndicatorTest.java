package net.dhleong.ctrlf.ui;

import android.app.Application;
import net.dhleong.ctrlf.BaseViewModuleTest;
import net.dhleong.ctrlf.model.AutoPilotStatus;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.HeadingStatus;
import net.dhleong.ctrlf.model.SimEvent;
import net.dhleong.ctrlf.module.TestModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;

/**
 * @author dhleong
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 17)
public class HeadingIndicatorTest extends BaseViewModuleTest<HeadingIndicatorView, HeadingIndicatorTest.HeadingIndicatorModule> {

    @Override
    protected HeadingIndicatorView inflateView(final Application context) {
        return new HeadingIndicatorView(context);
    }

    @Override
    protected HeadingIndicatorModule createModule() {
        return new HeadingIndicatorModule();
    }

    @Test
    public void initialStatus() {
        assertThat(view.heading).isEqualTo(0);
        assertThat(view.headingBug).isEqualTo(0);

        module.dataObjectsSubject.onNext(new HeadingStatus(127, 0));
        assertThat(view.heading).isEqualTo(127);
        assertThat(view.headingBug).isEqualTo(0);

        module.dataObjectsSubject.onNext(new AutoPilotStatus(true, true, 0, 42));
        assertThat(view.heading).isEqualTo(127);
        assertThat(view.headingBug).isEqualTo(42);
    }

    @Test
    public void headingBugSet() {
        assertThat(view.heading).isEqualTo(0);
        assertThat(view.headingBug).isEqualTo(0);

        module.dataObjectsSubject.onNext(new AutoPilotStatus(true, true, 0, 2));
        assertThat(view.headingBug).isEqualTo(2);

        view.bugDial.performDetentsMoved(FineDialView.STATE_INNER, 5);
        assertThat(module.setBugs).containsExactly(7);

        view.bugDial.performDetentsMoved(FineDialView.STATE_INNER, -10);
        assertThat(module.setBugs).endsWith(357);
    }

    public static class HeadingIndicatorModule extends TestModule {

        List<Integer> setBugs = new ArrayList<>();

        @Override
        protected void mockConnection(final Connection mock) {
            doAnswer(storeParam(setBugs))
                    .when(mock).sendEvent(eq(SimEvent.SET_AP_HEADING), anyInt());
        }
    }
}
