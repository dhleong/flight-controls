package net.dhleong.ctrlf;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.ButterKnife.Setter;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.OnClick;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.util.scopes.IsDummyMode;
import rx.Observer;
import rx.functions.Action0;

import javax.inject.Inject;
import java.util.List;


public class ConnectActivity
        extends AppCompatActivity {

    static final Setter<? super View, Boolean> ENABLED = new Setter<View, Boolean>() {
        @Override
        public void set(final View view, final Boolean aBoolean, final int i) {
            view.setEnabled(aBoolean);
        }
    };

    @Inject Connection connection;
    @Inject @IsDummyMode boolean isDummyMode;

    @InjectView(R.id.host) TextView host;
    @InjectView(R.id.port) TextView port;
    @InjectView(R.id.connect) TextView connect;
    @InjectViews({R.id.host, R.id.port, R.id.connect}) List<View> allViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        ButterKnife.inject(this);

        final App app = (App) getApplication();
        app.getAppComponent().inject(this);

        // TODO just restore previous one
        host.setText("192.168.1.30");
        port.setText("44506");

        if (isDummyMode) {
            connect.setText(R.string.connect_dummy);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_connect, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
        case R.id.action_settings:
            startActivity(new Intent(this, PrefsActivity.class));
            return true;

        case R.id.action_dummy:
            App.toggleDummyMode(this);

            // restart with the proper injections
            finish();
            startActivity(new Intent(this, ConnectActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.connect) void connect() {
        final int portNo;
        try {
            portNo = Integer.parseInt(String.valueOf(port.getText()));
            port.setError(null); // ensure cleared
        } catch (NumberFormatException e) {
            port.setError(getString(R.string.illegal_port));
            return;
        }

        ButterKnife.apply(allViews, ENABLED, false);
        connection.connect(host.getText().toString(), portNo)
            .finallyDo(new Action0() {
                @Override
                public void call() {
                    ButterKnife.apply(allViews, ENABLED, true);
                }
            })
            .subscribe(new Observer<Connection>() {
                @Override
                public void onCompleted() {
                    onConnected();
                }

                @Override
                public void onError(final Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void onNext(final Connection connection) {

                }
            });
    }

    /** Called on successful connection */
    void onConnected() {
        startActivity(new Intent(this, ControlsActivity.class));
    }
}
