package net.dhleong.ctrlf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.ButterKnife.Setter;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.OnClick;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.util.scopes.IsDummyMode;
import net.dhleong.ctrlf.util.scopes.Pref;
import rx.Observer;
import rx.functions.Action0;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;
import java.util.List;

import static net.dhleong.ctrlf.module.PrefsModule.LAST_HOST;
import static net.dhleong.ctrlf.module.PrefsModule.LAST_PORT;

public class ConnectActivity
        extends AppCompatActivity {

    static final Setter<? super View, Boolean> ENABLED = new Setter<View, Boolean>() {
        @Override
        public void set(final View view, final Boolean aBoolean, final int i) {
            view.setEnabled(aBoolean);
        }
    };

    @Inject Connection connection;
    @Inject SharedPreferences prefs;
    @Inject @Pref(LAST_HOST) String lastHost;
    @Inject @Pref(LAST_PORT) String lastPort;
    @Inject @IsDummyMode boolean isDummyMode;

    @InjectView(R.id.host) TextView host;
    @InjectView(R.id.port) TextView port;
    @InjectView(R.id.connect) TextView connect;
    @InjectViews({R.id.host, R.id.port, R.id.connect}) List<View> allViews;

    final CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        ButterKnife.inject(this);

        final App app = (App) getApplication();
        app.getAppComponent().inject(this);

        host.setText(lastHost);
        port.setText(lastPort);

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

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            connect();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        subscriptions.unsubscribe();
    }

    @OnClick(R.id.connect) void connect() {

        final String hostRaw = String.valueOf(host.getText()).trim();
        final String portRaw = String.valueOf(port.getText()).trim();

        final int portNo;
        try {
            portNo = Integer.parseInt(portRaw);
            port.setError(null); // ensure cleared
        } catch (NumberFormatException e) {
            port.setError(getString(R.string.illegal_port));
            return;
        }

        // save values
        prefs.edit()
             .putString(LAST_HOST, hostRaw)
             .putString(LAST_PORT, portRaw)
             .apply();

        ButterKnife.apply(allViews, ENABLED, false);

        subscriptions.add(
            connection.connect(hostRaw, portNo)
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
                              Log.w("ctrlf", "Error connecting to sim", throwable);
                              Toast.makeText(ConnectActivity.this,
                                      getString(R.string.connect_error, throwable.getMessage()),
                                      Toast.LENGTH_LONG).show();
                          }

                          @Override
                          public void onNext(final Connection connection) {

                          }
                      })
        );
    }

    /** Called on successful connection */
    void onConnected() {
        startActivity(new Intent(this, ControlsActivity.class));
    }
}
