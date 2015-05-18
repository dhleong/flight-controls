package net.dhleong.ctrlf;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import net.dhleong.ctrlf.model.Connection;

import javax.inject.Inject;

/**
 * @author dhleong
 */
public class ControlsActivity extends ActionBarActivity {

    @Inject Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls);

        App.provideComponent(this).inject(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        connection.disconnect();
    }
}
