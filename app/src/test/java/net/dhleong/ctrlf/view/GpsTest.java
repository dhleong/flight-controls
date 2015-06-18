package net.dhleong.ctrlf.view;

import android.app.Application;
import android.view.LayoutInflater;
import android.view.View;
import net.dhleong.ctrlf.BaseViewModuleTest;
import net.dhleong.ctrlf.R;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.SimEvent;
import net.dhleong.ctrlf.module.TestModule;
import net.dhleong.ctrlf.ui.FineDialView;
import net.dhleong.ctrlf.view.GpsTest.GpsTestModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;

/**
 * @author dhleong
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 17)
public class GpsTest extends BaseViewModuleTest<GpsView, GpsTestModule> {

    @Override
    protected GpsView inflateView(final Application context) {
        return (GpsView) LayoutInflater.from(context)
                                       .inflate(R.layout.widget_gps, null);
    }

    @Override
    protected GpsTestModule createModule() {
        return new GpsTestModule();
    }

    @Test
    public void correctClickEvents() {
        assertClickSends(R.id.range_up, SimEvent.GPS_ZOOM_OUT);
        assertClickSends(R.id.range_down, SimEvent.GPS_ZOOM_IN);
        assertClickSends(R.id.direct, SimEvent.GPS_DIRECT);
        assertClickSends(R.id.menu, SimEvent.GPS_MENU);
        assertClickSends(R.id.clear, SimEvent.GPS_CLEAR);
        assertClickSends(R.id.enter, SimEvent.GPS_ENTER);

        assertClickSends(R.id.dial, SimEvent.GPS_CURSOR);

        assertClickSends(R.id.nearest, SimEvent.GPS_NEAREST);
        assertClickSends(R.id.obs, SimEvent.GPS_OBS);
        assertClickSends(R.id.message, SimEvent.GPS_MESSAGE);
        assertClickSends(R.id.flight_plan, SimEvent.GPS_FLIGHT_PLAN);
        assertClickSends(R.id.terrain, SimEvent.GPS_TERRAIN);
        assertClickSends(R.id.procedure, SimEvent.GPS_PROCEDURE);
    }

    @Test
    public void longClickClearAll() {
        view.findViewById(R.id.clear).performLongClick();
        assertEventExactly(SimEvent.GPS_CLEAR_ALL);
    }

    @Test
    public void correctDialEvents() {
        assertDialSends(FineDialView.STATE_INNER,  1, SimEvent.GPS_PAGE_KNOB_INC);
        assertDialSends(FineDialView.STATE_INNER, -1, SimEvent.GPS_PAGE_KNOB_DEC);

        assertDialSends(FineDialView.STATE_OUTER,  1, SimEvent.GPS_GROUP_KNOB_INC);
        assertDialSends(FineDialView.STATE_OUTER, -1, SimEvent.GPS_GROUP_KNOB_DEC);
    }

    void assertClickSends(final int viewId, final SimEvent expectedEvent) {
        final View thisView = view.findViewById(viewId);
        thisView.performClick();
        assertEventExactly(expectedEvent);
    }

    void assertDialSends(final int state, final int direction,
            final SimEvent expectedEvent) {
        view.dial.performDetentsMoved(state, direction);
        assertEventExactly(expectedEvent);
    }

    void assertEventExactly(final SimEvent expectedEvent) {
        assertThat(module.events).hasSize(1);
        assertThat(module.events.get(0)).isEqualTo(expectedEvent);
        module.events.clear(); // make way for the next guy
    }


    public static class GpsTestModule extends TestModule {

        final List<SimEvent> events = new ArrayList<>();

        @Override
        protected void mockConnection(final Connection mock) {
            doAnswer(storeEvent(events))
                    .when(mock)
                    .sendEvent(any(SimEvent.class), eq(0));
        }
    }
}
