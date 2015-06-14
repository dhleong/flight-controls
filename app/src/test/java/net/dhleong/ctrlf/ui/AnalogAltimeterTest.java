package net.dhleong.ctrlf.ui;

import android.app.Application;
import net.dhleong.ctrlf.BaseViewModuleTest;
import net.dhleong.ctrlf.model.AltitudeStatus;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.SimEvent;
import net.dhleong.ctrlf.module.TestModule;
import net.dhleong.ctrlf.ui.AnalogAltimeterTest.AnalogAltimeterModule;
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
public class AnalogAltimeterTest extends BaseViewModuleTest<AnalogAltimeter, AnalogAltimeterModule> {

    @Override
    protected AnalogAltimeter inflateView(final Application context) {
        return new AnalogAltimeter(context);
    }

    @Override
    protected AnalogAltimeterModule createModule() {
        return new AnalogAltimeterModule();
    }

    @Test
    public void update() {
        view.setKohlsmanMb(1013);
        view.altitude = 0;

        // stored internally as *16
        // we "should" use a getter that returns as set,
        //  but we want to test the internal accuracy, too
        assertThat(view.kohlsmanMb).isEqualTo(1013 * 16);

        module.dataObjectsSubject.onNext(new AltitudeStatus(1000, 0, 1014));
        assertThat(view.altitude).isEqualTo(1000);
        assertThat(view.kohlsmanMb).isEqualTo(1014 * 16);
    }

    @Test
    public void detents() {
        view.setKohlsmanMb(1013);

        view.dial.performDetentsMoved(FineDialView.STATE_INNER, 1);
        assertThat(module.kohlsmans).containsExactly(1013 * 16 + 4);
        module.kohlsmans.clear();

        view.dial.performDetentsMoved(FineDialView.STATE_INNER, -1);
        assertThat(module.kohlsmans).containsExactly(1013 * 16);
        module.kohlsmans.clear();

        view.dial.performDetentsMoved(FineDialView.STATE_INNER, 2);
        assertThat(module.kohlsmans).containsExactly(1013 * 16 + 8);
    }

    @Test
    public void dontOverride() {
        view.setKohlsmanMb(1013);
        view.altitude = 0;

        // stored internally as *16
        // we "should" use a getter that returns as set,
        //  but we want to test the internal accuracy, too
        assertThat(view.kohlsmanMb).isEqualTo(1013 * 16);
        view.dial.performDetentsMoved(FineDialView.STATE_INNER, 4);

        // no change
        module.dataObjectsSubject.onNext(new AltitudeStatus(1000, 0, 1013));
        assertThat(view.altitude).isEqualTo(0);
        assertThat(view.kohlsmanMb).isEqualTo(1014 * 16);

        // now change
        module.dataObjectsSubject.onNext(new AltitudeStatus(1000, 0, 1014));
        assertThat(view.altitude).isEqualTo(1000);
        assertThat(view.kohlsmanMb).isEqualTo(1014 * 16);

        // etc
        module.dataObjectsSubject.onNext(new AltitudeStatus(1100, 0, 1015));
        assertThat(view.altitude).isEqualTo(1100);
        assertThat(view.kohlsmanMb).isEqualTo(1015 * 16);
    }

    public static class AnalogAltimeterModule extends TestModule {

        final List<Integer> kohlsmans = new ArrayList<>();

        @Override
        protected void mockConnection(final Connection mock) {
            doAnswer(storeParam(kohlsmans))
                    .when(mock).sendEvent(any(SimEvent.class), anyInt());
        }
    }

}
