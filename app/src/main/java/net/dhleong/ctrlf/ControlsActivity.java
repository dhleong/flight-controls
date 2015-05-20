package net.dhleong.ctrlf;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;
import butterknife.InjectView;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.util.scopes.Pref;
import net.dhleong.ctrlf.view.RadioStackView;

import javax.inject.Inject;

/**
 * @author dhleong
 */
public class ControlsActivity extends AppCompatActivity {

    @Inject Connection connection;
    @Inject @Pref("screen_on") boolean keepScreenOn;

    @InjectView(R.id.radio_stack) RadioStackView radioStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls);

        App.provideComponent(this).inject(this);
        ButterKnife.inject(this);

        radioStack.setKeepScreenOn(keepScreenOn);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        connection.disconnect();
    }
}
