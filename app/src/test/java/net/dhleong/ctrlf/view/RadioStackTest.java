package net.dhleong.ctrlf.view;

import android.app.Application;
import android.view.LayoutInflater;
import net.dhleong.ctrlf.BaseViewModuleTest;
import net.dhleong.ctrlf.R;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.RadioStatus;
import net.dhleong.ctrlf.model.SimEvent;
import net.dhleong.ctrlf.module.TestModule;
import net.dhleong.ctrlf.ui.FineDialView;
import net.dhleong.ctrlf.view.RadioStackTest.RadioTestModule;
import org.junit.Before;
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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;

/**
 * @author dhleong
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 17)
public class RadioStackTest extends BaseViewModuleTest<RadioStackView, RadioTestModule> {

    private static final int INITIAL_COM_ACTIVE = 127_500;
    private static final int INITIAL_COM_STANDBY = 118_500;
    private static final int INITIAL_NAV_ACTIVE = 111_250;
    private static final int INITIAL_NAV_STANDBY = 111_000;

    @Override
    protected RadioStackView inflateView(final Application context) {
        return view = (RadioStackView) LayoutInflater
                .from(context)
                .inflate(R.layout.widget_radio_stack, null);
    }

    @Override
    protected RadioTestModule createModule() {
        return new RadioTestModule();
    }

    @Before
    public void setUp() {
        super.setUp();

        view.navCom1.setComFrequency(INITIAL_COM_ACTIVE);
        view.navCom1.setComStandbyFrequency(INITIAL_COM_STANDBY);
        view.navCom1.setNavFrequency(INITIAL_NAV_ACTIVE);
        view.navCom1.setNavStandbyFrequency(INITIAL_NAV_STANDBY);
        view.xpndr.setTransponderCode(1200);
    }

    @Test
    public void moveCom1() {

        // nothing published yet
        assertThat(module.com1).isEmpty();

        // simulate a drag
        view.navCom1.comDial.performDetentsMoved(FineDialView.STATE_OUTER, 1);

        // we start disabled (no power) so nothing should happen
        assertThat(module.com1).isEmpty();

        // okay, now enable
        view.navCom1.setEnabled(true);
        view.navCom1.comDial.performDetentsMoved(FineDialView.STATE_OUTER, 1);

        // we now have our new frequency!
        assertThat(module.com1).containsExactly(INITIAL_COM_STANDBY + 1000);
    }

    @Test
    public void moveCom1Slightly() {

        // nothing published yet
        assertThat(module.com1).isEmpty();

        view.navCom1.setEnabled(true);
        view.navCom1.comDial.performDetentsMoved(FineDialView.STATE_INNER, 1);
        assertThat(module.com1).containsExactly(INITIAL_COM_STANDBY + 50);

        view.navCom1.comDial.performClick();
        view.navCom1.comDial.performDetentsMoved(FineDialView.STATE_INNER, 1);
        assertThat(module.com1).endsWith(INITIAL_COM_STANDBY + 75);

        view.navCom1.comDial.performClick();
        view.navCom1.comDial.performDetentsMoved(FineDialView.STATE_INNER, 1);
        assertThat(module.com1).endsWith(INITIAL_COM_STANDBY + 125);
    }

    @Test
    public void moveNav1Slightly() {

        // nothing published yet
        assertThat(module.nav1).isEmpty();

        view.navCom1.setEnabled(true);
        view.navCom1.navDial.performDetentsMoved(FineDialView.STATE_INNER, 1);
        assertThat(module.nav1).containsExactly(INITIAL_NAV_STANDBY + 50);

        view.navCom1.navDial.performClick();
        view.navCom1.navDial.performDetentsMoved(FineDialView.STATE_INNER, 1);
        assertThat(module.nav1).endsWith(INITIAL_NAV_STANDBY + 75);

        view.navCom1.navDial.performClick();
        view.navCom1.navDial.performDetentsMoved(FineDialView.STATE_INNER, 1);
        assertThat(module.nav1).endsWith(INITIAL_NAV_STANDBY + 125);
    }

    @Test
    public void moveNav1() {

        // nothing published yet
        assertThat(module.nav1).isEmpty();

        // simulate a drag
        view.navCom1.navDial.performDetentsMoved(FineDialView.STATE_OUTER, 1);

        // we start disabled (no power) so nothing should happen
        assertThat(module.nav1).isEmpty();

        // okay, now enable
        view.navCom1.setEnabled(true);
        view.navCom1.navDial.performDetentsMoved(FineDialView.STATE_OUTER, 1);

        // we now have our new frequency!
        assertThat(module.nav1).containsExactly(INITIAL_NAV_STANDBY + 1000);
    }

    @Test
    public void receiveStatus() {
        assertThat(view.navCom1.getComFrequency()).isEqualTo(INITIAL_COM_ACTIVE);
        assertThat(view.navCom1.getNavFrequency()).isEqualTo(INITIAL_NAV_ACTIVE);

        module.dataObjectsSubject.onNext(new RadioStatus(true, 118_000, 119_250, 112_000, 114_250, 3015));
        assertThat(view.navCom1.getComFrequency()).isEqualTo(118_000);
        assertThat(view.navCom1.getComStandbyFrequency()).isEqualTo(119_250);
        assertThat(view.navCom1.getNavFrequency()).isEqualTo(112_000);
        assertThat(view.navCom1.getNavStandbyFrequency()).isEqualTo(114_250);
        assertThat(view.xpndr.getTransponderCode()).isEqualTo(3015);
    }

    @Test
    public void receiveNoPower() {
        assertThat(view.navCom1.getComFrequency()).isEqualTo(INITIAL_COM_ACTIVE);
        assertThat(view.navCom1.getNavFrequency()).isEqualTo(INITIAL_NAV_ACTIVE);

        // with avionics disabled, we use a negative frequency
        //  to draw the views as "off"
        module.dataObjectsSubject.onNext(new RadioStatus(false, 118_000, 119_250, 112_000, 114_250, 3015));
        assertThat(view.navCom1).isDisabled();
        assertThat(view.navCom1.getComFrequency()).isEqualTo(-1);
        assertThat(view.navCom1.getComStandbyFrequency()).isEqualTo(-1);
        assertThat(view.navCom1.getNavFrequency()).isEqualTo(-1);
        assertThat(view.navCom1.getNavStandbyFrequency()).isEqualTo(-1);
        assertThat(view.xpndr).isDisabled();
        assertThat(view.xpndr.getTransponderCode()).isEqualTo(-1);

        // providing power should not lose the last frequency
        view.navCom1.setEnabled(true);
        view.xpndr.setEnabled(true);
        assertThat(view.navCom1.getComFrequency()).isEqualTo(118_000);
        assertThat(view.navCom1.getComStandbyFrequency()).isEqualTo(119_250);
        assertThat(view.navCom1.getNavFrequency()).isEqualTo(112_000);
        assertThat(view.navCom1.getNavStandbyFrequency()).isEqualTo(114_250);
        assertThat(view.xpndr.getTransponderCode()).isEqualTo(3015);
    }

    @Test
    public void swapCom1() {
        assertThat(module.clickEvents).isEmpty();

        view.navCom1.comSwap.performClick();

        assertThat(module.clickEvents).containsExactly(SimEvent.COM1_SWAP);
    }

    @Test
    public void swapNav1() {
        assertThat(module.clickEvents).isEmpty();

        view.navCom1.navSwap.performClick();

        assertThat(module.clickEvents).containsExactly(SimEvent.NAV1_SWAP);
    }

    @Test
    public void transponder() {
        assertThat(module.transponder).isEmpty();

        view.xpndr.numbers.get(3).performClick();
        assertThat(module.transponder).contains(3200);

        view.xpndr.numbers.get(4).performClick();
        assertThat(module.transponder).contains(3400);

        view.xpndr.numbers.get(5).performClick();
        assertThat(module.transponder).contains(3450);

        view.xpndr.numbers.get(6).performClick();
        assertThat(module.transponder).contains(3456);

        // aaaand wrap
        view.xpndr.numbers.get(7).performClick();
        assertThat(module.transponder).contains(7456);
    }

    @Test
    public void transponderNotOverridden() {
        view.xpndr.setEnabled(true);
        assertThat(module.transponder).isEmpty();

        view.xpndr.numbers.get(3).performClick();
        assertThat(module.transponder).contains(3200);

        // there should be no change
        view.xpndr.setEnabled(true);
        assertThat(view.xpndr.getTransponderCode()).isEqualTo(3200);

        // disable (lose power)
        view.xpndr.setEnabled(false);
        assertThat(view.xpndr.getTransponderCode()).isEqualTo(-1);

        // re-enable; stilo should be no change
        view.xpndr.setEnabled(true);
        assertThat(view.xpndr.getTransponderCode()).isEqualTo(3200);

    }

    @Test
    public void autopilotAltitude() {
        assertThat(module.apAltitudes).isEmpty();

        // as above, we start out disabled
        view.ap.dial.performDetentsMoved(FineDialView.STATE_OUTER, 1);
        assertThat(module.apAltitudes).isEmpty();

        view.ap.setEnabled(true);
        view.ap.dial.performDetentsMoved(FineDialView.STATE_OUTER, 1);
        assertThat(module.apAltitudes).containsExactly(1000);

        view.ap.dial.performDetentsMoved(FineDialView.STATE_INNER, 1);
        assertThat(module.apAltitudes).containsExactly(1000, 1100);
    }

    static class RadioTestModule extends TestModule {

        final List<Integer> com1 = new ArrayList<>();
        final List<Integer> nav1 = new ArrayList<>();
        final List<Integer> transponder = new ArrayList<>();
        final List<Integer> apAltitudes = new ArrayList<>();

        final List<SimEvent> clickEvents = new ArrayList<>();

        @Override
        protected void mockConnection(final Connection mock) {
            doAnswer(storeTransponderParam(transponder))
                    .when(mock).sendEvent(eq(SimEvent.SET_TRANSPONDER), anyInt());
            doAnswer(storeFrequencyParam(com1))
                    .when(mock).sendEvent(eq(SimEvent.COM1_STANDBY), anyInt());
            doAnswer(storeFrequencyParam(nav1))
                    .when(mock).sendEvent(eq(SimEvent.NAV1_STANDBY), anyInt());
            doAnswer(storeParam(apAltitudes))
                    .when(mock).sendEvent(eq(SimEvent.SET_AP_ALTITUDE), anyInt());

            doAnswer(storeEvent(clickEvents))
                    .when(mock).sendEvent(any(SimEvent.class), eq(0));
        }

    }
}
