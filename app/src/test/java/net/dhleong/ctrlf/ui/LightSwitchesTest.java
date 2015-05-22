package net.dhleong.ctrlf.ui;

import android.app.Application;
import net.dhleong.ctrlf.BaseViewModuleTest;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.LightsStatus;
import net.dhleong.ctrlf.model.SimEvent;
import net.dhleong.ctrlf.module.TestModule;
import net.dhleong.ctrlf.ui.LightSwitchesTest.LightsTestModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doAnswer;

/**
 * @author dhleong
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 17)
public class LightSwitchesTest extends BaseViewModuleTest<LightSwitchesView, LightsTestModule> {

    @Override
    protected LightSwitchesView inflateView(final Application context) {
        return new LightSwitchesView(context);
    }

    @Override
    protected LightsTestModule createModule() {
        return new LightsTestModule();
    }

    @Test
    public void correctEvents() {
        assertThat(module.toggledLights).isEmpty();

        final int len = LightSwitchesView.SWITCH_EVENTS.length;
        for (int i=0; i < len; i++) {
            view.getChildAt(i).performClick();
        }

        assertThat(module.toggledLights).containsExactly(LightSwitchesView.SWITCH_EVENTS);
    }

    @Test
    public void receivingDoesntTriggerSend() {
        assertThat(module.toggledLights).isEmpty();

        module.dataObjectsSubject.onNext(new LightsStatus(
                true, true, true, true, true, true));

        assertThat(module.toggledLights).isEmpty();
    }

    static class LightsTestModule extends TestModule {

        List<SimEvent> toggledLights = new ArrayList<>();

        @Override
        protected void mockConnection(final Connection mock) {
            doAnswer(storeEvent(toggledLights))
                    .when(mock).sendEvent(any(SimEvent.class), anyInt());
        }
    }
}
