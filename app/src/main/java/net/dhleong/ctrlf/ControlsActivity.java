package net.dhleong.ctrlf;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;
import butterknife.InjectView;
import flightsim.simconnect.SimConnectPeriod;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.DataRequestPeriod;
import net.dhleong.ctrlf.model.DataType;
import net.dhleong.ctrlf.util.scopes.Pref;
import net.dhleong.ctrlf.view.RadioStackView;
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

    @Inject Connection connection;
    @Inject @Pref("screen_on") boolean keepScreenOn;
    @Inject Observable<Connection.Lifecycle> lifecycle;

    @InjectView(R.id.radio_stack) RadioStackView radioStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls);

        App.provideComponent(this).inject(this);
        ButterKnife.inject(this);

        radioStack.setKeepScreenOn(keepScreenOn);

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
        case DISCONNECTED:
        }
    }
}
