package net.dhleong.ctrlf.view;

import android.app.Application;
import android.view.LayoutInflater;
import net.dhleong.ctrlf.App;
import net.dhleong.ctrlf.R;
import net.dhleong.ctrlf.TestProvider;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.RadioStatus;
import net.dhleong.ctrlf.module.TestModule;
import net.dhleong.ctrlf.ui.FineDialView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;
import rx.subjects.ReplaySubject;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author dhleong
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 17)
public class RadioStackTest {

    private static final int INITIAL_COM_ACTIVE = 127_500;
    private static final int INITIAL_COM_STANDBY = 118_500;

    Application context;
    RadioTestModule module;

    RadioStackView view;

    @Before
    public void setUp() {

        module = new RadioTestModule();
        App.provider = TestProvider.from(module);

        context = Robolectric.application;
        view = (RadioStackView) LayoutInflater
                .from(context)
                .inflate(R.layout.widget_radio_stack, null);
        view.onAttachedToWindow();

        view.navCom1.setComFrequency(INITIAL_COM_ACTIVE);
        view.navCom1.setComStandbyFrequency(INITIAL_COM_STANDBY);
    }

    @Test
    public void moveCom1() {

        // nothing published yet
        assertThat(module.com1).isEmpty();

        // simulate a drag
        view.navCom1.comDial.performDetentsMoved(FineDialView.STATE_OUTER, 1);

        // we now have our new frequency!
        assertThat(module.com1).containsExactly(INITIAL_COM_STANDBY + 1000);
    }

    @Test
    public void receiveStatus() {
        assertThat(view.navCom1.getComFrequency()).isEqualTo(INITIAL_COM_ACTIVE);

        module.radioStatusSubject.onNext(new RadioStatus(true, 118_000, 119_250));
        assertThat(view.navCom1.getComFrequency()).isEqualTo(118_000);
        assertThat(view.navCom1.getComStandbyFrequency()).isEqualTo(119_250);
    }

    @Test
    public void receiveNoPower() {
        assertThat(view.navCom1.getComFrequency()).isEqualTo(INITIAL_COM_ACTIVE);

        // with avionics disabled, we use a negative frequency
        //  to draw the views as "off"
        module.radioStatusSubject.onNext(new RadioStatus(false, 118_000, 119_250));
        assertThat(view.navCom1.isEnabled()).isEqualTo(false);
        assertThat(view.navCom1.getComFrequency()).isEqualTo(-1);
        assertThat(view.navCom1.getComStandbyFrequency()).isEqualTo(-1);

        // providing power should not lose the last frequency
        view.navCom1.setEnabled(true);
        assertThat(view.navCom1.getComFrequency()).isEqualTo(118_000);
        assertThat(view.navCom1.getComStandbyFrequency()).isEqualTo(119_250);
    }

    private class RadioTestModule extends TestModule {

        private final ReplaySubject<Integer> com1Subject = ReplaySubject.create();
        List<Integer> com1 = new ArrayList<>();

        BehaviorSubject<RadioStatus> radioStatusSubject = BehaviorSubject.create();

        RadioTestModule() {
            com1Subject.subscribe(new Action1<Integer>() {
                @Override
                public void call(final Integer integer) {
                    com1.add(integer);
                }
            });
        }

        @Override
        protected void mockConnection(final Connection mock) {
            when(mock.getStandbyCom1Observer()).thenReturn(com1Subject);
            when(mock.radioStatus()).thenReturn(radioStatusSubject);
        }
    }
}
