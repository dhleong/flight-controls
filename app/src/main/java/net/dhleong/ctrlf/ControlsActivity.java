package net.dhleong.ctrlf;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.DataRequestPeriod;
import net.dhleong.ctrlf.model.DataType;
import net.dhleong.ctrlf.util.scopes.Pref;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import javax.inject.Inject;

/**
 * @author dhleong
 */
public class ControlsActivity
        extends AppCompatActivity
        implements Action1<Connection.Lifecycle> {

    static class PanelAdapter extends PagerAdapter {

        private int childCount;

        public PanelAdapter(final int childCount) {
            this.childCount = childCount;
        }

        @Override
        public int getCount() {
            return childCount;
        }

        @Override
        public Object instantiateItem(final ViewGroup container, final int position) {
            return container.getChildAt(position);
        }

        @Override
        public boolean isViewFromObject(final View view, final Object object) {
            return view == object;
        }
    }

    @Inject Connection connection;
    @Inject @Pref("screen_on") boolean keepScreenOn;
    @Inject Observable<Connection.Lifecycle> lifecycle;

    @InjectView(R.id.panel_swapper) ViewPager panelSwapper;
//    @InjectView(R.id.radio_stack) RadioStackView radioStack;
//    @InjectView(R.id.instruments_panel) View instruments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls);

        App.provideComponent(this).inject(this);
        ButterKnife.inject(this);

        panelSwapper.setKeepScreenOn(keepScreenOn);
        panelSwapper.setAdapter(new PanelAdapter(panelSwapper.getChildCount()));

        lifecycle.observeOn(AndroidSchedulers.mainThread())
                 .subscribe(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        connection.disconnect();
    }

    @Override
    public void call(final Connection.Lifecycle lifecycle) {
        switch (lifecycle) {
        case SIM_START:
            // finally, request some data. We could perhaps rather use
            //  the change events, but we need these anyway, and 1/s shouldn't
            //  put that much strain on the network....
            connection.requestData(DataType.RADIO_STATUS, DataRequestPeriod.SLOW);

            // when we add more to this, we'll want SECOND period, probably
            connection.requestData(DataType.AUTOPILOT_STATUS, DataRequestPeriod.SINGLE);

            // we only need the initial state for this
            connection.requestData(DataType.LIGHT_STATUS, DataRequestPeriod.SINGLE);
            break;

        case SIM_QUIT:
            Toast.makeText(this, R.string.sim_quit, Toast.LENGTH_SHORT).show();
            finish();
            break;
        case DISCONNECTED:
            if (!isFinishing()) {
                Toast.makeText(this, R.string.sim_disconnect, Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        }
    }
}
