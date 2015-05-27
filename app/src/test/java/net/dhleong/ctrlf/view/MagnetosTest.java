package net.dhleong.ctrlf.view;

import android.app.Application;
import net.dhleong.ctrlf.BaseViewModuleTest;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.EngineStatus;
import net.dhleong.ctrlf.model.EngineStatus.EngineType;
import net.dhleong.ctrlf.model.SimEvent;
import net.dhleong.ctrlf.module.TestModule;
import net.dhleong.ctrlf.ui.MagnetoSwitchView;
import net.dhleong.ctrlf.ui.MagnetoSwitchView.MagnetoMode;
import net.dhleong.ctrlf.view.MagnetosTest.MagnetosTestModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doAnswer;

/**
 * @author dhleong
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 17)
public class MagnetosTest extends BaseViewModuleTest<MagnetosView, MagnetosTestModule> {

    @Override
    protected MagnetosView inflateView(final Application context) {
        return new MagnetosView(context);
    }

    @Override
    protected MagnetosTestModule createModule() {
        return new MagnetosTestModule();
    }

    @Test
    public void correctCount() {
        module.dataObjectsSubject.onNext(new EngineStatus(EngineType.TURBOPROP,
                MagnetoMode.BOTH));
        assertThat(view)
                .isVisible()
                .hasChildCount(1);

        // jet planes don't have magnetos
        module.dataObjectsSubject.onNext(new EngineStatus(EngineType.JET,
                MagnetoMode.BOTH, MagnetoMode.BOTH));
        assertThat(view)
                .isNotVisible()
                .hasChildCount(0);

        module.dataObjectsSubject.onNext(new EngineStatus(EngineType.TURBOPROP,
                MagnetoMode.BOTH, MagnetoMode.BOTH));
        assertThat(view)
                .isVisible()
                .hasChildCount(2);

        // gliders also don't, but we'll lie here to
        //  make sure the right thing happens
        module.dataObjectsSubject.onNext(new EngineStatus(EngineType.NONE,
                MagnetoMode.BOTH, MagnetoMode.BOTH));
        assertThat(view)
                .isNotVisible()
                .hasChildCount(0);
    }

    @Test
    public void sendCorrectEvent() {
        // max four engines
        module.dataObjectsSubject.onNext(new EngineStatus(EngineType.TURBOPROP,
                MagnetoMode.BOTH, MagnetoMode.BOTH,
                MagnetoMode.BOTH, MagnetoMode.BOTH));

        getMagneto(0).performNotchMoved(1);
        getMagneto(1).performNotchMoved(1);
        getMagneto(2).performNotchMoved(1);
        getMagneto(3).performNotchMoved(1);

        assertThat(module.sentEvents).containsExactly(
                SimEvent.MAGNETO1_START,
                SimEvent.MAGNETO2_START,
                SimEvent.MAGNETO3_START,
                SimEvent.MAGNETO4_START
        );
    }

    @Test
    public void noConfusion() {
        // two engines
        module.dataObjectsSubject.onNext(new EngineStatus(EngineType.TURBOPROP,
                MagnetoMode.BOTH, MagnetoMode.BOTH));

        getMagneto(0).performNotchMoved(1);
        assertThat(module.sentEvents).containsExactly(SimEvent.MAGNETO1_START);
        module.sentEvents.clear();

        getMagneto(1).performNotchMoved(1);
        assertThat(module.sentEvents).containsExactly(SimEvent.MAGNETO2_START);
        module.sentEvents.clear();

        getMagneto(0).performNotchMoved(-1);
        assertThat(module.sentEvents).containsExactly(SimEvent.MAGNETO1_BOTH);
        module.sentEvents.clear();

        getMagneto(0).performNotchMoved(-1);
        assertThat(module.sentEvents).containsExactly(
                SimEvent.MAGNETO1_OFF,
                SimEvent.MAGNETO1_LEFT);
        module.sentEvents.clear();

        getMagneto(1).performNotchMoved(-1);
        assertThat(module.sentEvents).containsExactly(SimEvent.MAGNETO2_BOTH);
        module.sentEvents.clear();

        getMagneto(0).performNotchMoved(-1);
        assertThat(module.sentEvents).containsExactly(
                SimEvent.MAGNETO1_OFF,
                SimEvent.MAGNETO1_RIGHT);
        module.sentEvents.clear();

        getMagneto(1).performNotchMoved(-1);
        assertThat(module.sentEvents).containsExactly(
                SimEvent.MAGNETO2_OFF,
                SimEvent.MAGNETO2_LEFT);
        module.sentEvents.clear();

        getMagneto(0).performNotchMoved(-1);
        assertThat(module.sentEvents).containsExactly(SimEvent.MAGNETO1_OFF);
        module.sentEvents.clear();

        getMagneto(1).performNotchMoved(-1);
        assertThat(module.sentEvents).containsExactly(
                SimEvent.MAGNETO2_OFF,
                SimEvent.MAGNETO2_RIGHT);
        module.sentEvents.clear();

        getMagneto(1).performNotchMoved(-1);
        assertThat(module.sentEvents).containsExactly(SimEvent.MAGNETO2_OFF);
        module.sentEvents.clear();

    }

    private MagnetoSwitchView getMagneto(final int index) {
        return (MagnetoSwitchView) view.getChildAt(index);
    }

    public static class MagnetosTestModule extends TestModule {

        final List<SimEvent> sentEvents = new ArrayList<>();

        @Override
        protected void mockConnection(final Connection mock) {
            doAnswer(storeEvent(sentEvents))
                    .when(mock).sendEvent(any(SimEvent.class), anyInt());
        }
    }
}
