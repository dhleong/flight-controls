package net.dhleong.ctrlf.ui;

import android.app.Application;
import net.dhleong.ctrlf.BaseViewModuleTest;
import net.dhleong.ctrlf.model.AutoPilotStatus;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.SimData;
import net.dhleong.ctrlf.model.SimEvent;
import net.dhleong.ctrlf.module.TestModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import rx.subjects.BehaviorSubject;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * @author dhleong
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 17)
public class AutoPilotTest extends BaseViewModuleTest<SimpleAutoPilotView, AutoPilotTest.SimpleAutoPilotModule> {

    @Override
    protected SimpleAutoPilotView inflateView(final Application context) {
        return new SimpleAutoPilotView(context);
    }

    @Override
    protected SimpleAutoPilotModule createModule() {
        return new SimpleAutoPilotModule();
    }


    @Test
    public void autopilotButtons() {
        assertThat(module.clickEvents).isEmpty();

        view.allButtons.get(0).performClick();
        assertThat(module.clickEvents).containsExactly(SimEvent.AP_MASTER_TOGGLE);

        view.allButtons.get(1).performClick();
        assertThat(module.clickEvents).endsWith(SimEvent.AP_HEADING_TOGGLE);

        view.allButtons.get(2).performClick();
        assertThat(module.clickEvents).endsWith(SimEvent.AP_NAV_TOGGLE);

        view.allButtons.get(5).performClick();
        assertThat(module.clickEvents).endsWith(SimEvent.AP_ALTITUDE_TOGGLE);
    }

    @Test
    public void autopilotStatus() {
        // just provide power
        view.setEnabled(true);

        assertThat(view.getTargetAltitude()).isEqualTo(0);

        module.dataObjectsSubject.onNext(new AutoPilotStatus(true, true, 3500, 0));
        assertThat(view.getTargetAltitude()).isEqualTo(3500);
    }

    @Test
    public void hideWhenUnavailable() {
        // just provide power
        view.setEnabled(true);
        assertThat(view).isVisible();

        module.dataObjectsSubject.onNext(new AutoPilotStatus(false, false, 3500, 0));
        assertThat(view).isGone();
    }

    public static class SimpleAutoPilotModule extends TestModule {

        final List<SimEvent> clickEvents = new ArrayList<>();
        final BehaviorSubject<SimData> dataObjectsSubject = BehaviorSubject.create();

        @Override
        protected void mockConnection(final Connection mock) {
            when(mock.dataObjects()).thenReturn(dataObjectsSubject);

            doAnswer(storeEvent(clickEvents))
                    .when(mock).sendEvent(any(SimEvent.class), eq(0));
        }
    }
}
