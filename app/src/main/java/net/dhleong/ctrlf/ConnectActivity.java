package net.dhleong.ctrlf;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.ButterKnife.Setter;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.OnClick;
import net.dhleong.ctrlf.history.ConnectionHistorian;
import net.dhleong.ctrlf.history.HistoricalConnection;
import net.dhleong.ctrlf.model.Connection;
import net.dhleong.ctrlf.model.Connection.Lifecycle;
import net.dhleong.ctrlf.util.UiUtil;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;
import java.util.ArrayList;
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
    @Inject SharedPreferences prefs;
    @Inject ConnectionHistorian historian;
    @Inject Observable<Lifecycle> lifecycle;

    @InjectView(R.id.toolbar) Toolbar toolbar;
    @InjectView(R.id.fab) FloatingActionButton fab;
    @InjectView(R.id.root) View root;
    @InjectView(R.id.host) TextView host;
    @InjectView(R.id.port) TextView port;
    @InjectView(R.id.empty) View emptyView;
    @InjectView(R.id.connect) TextView connect;
    @InjectView(R.id.connections) RecyclerView connections;
    @InjectView(R.id.new_connection) View newConnections;
    @InjectView(R.id.new_connection_toolbar) Toolbar newConnectionToolbar;
    @InjectViews({R.id.connections, R.id.connect, R.id.fab,
            R.id.host, R.id.port}) List<View> allViews;

    final CompositeSubscription subscriptions = new CompositeSubscription();

    HistoryAdapter adapter;
    HistoricalConnection pendingConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        ButterKnife.inject(this);

        final App app = (App) getApplication();
        app.getAppComponent().inject(this);

        setSupportActionBar(toolbar);

        adapter = new HistoryAdapter();
        connections.setLayoutManager(new LinearLayoutManager(this));
        connections.setAdapter(adapter);

        newConnectionToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        newConnectionToolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                onBackPressed();
            }
        });

        subscriptions.add(
                lifecycle.observeOn(AndroidSchedulers.mainThread())
                         .subscribe(new Action1<Lifecycle>() {
                             @Override
                             public void call(final Lifecycle lifecycle) {
                                 switch (lifecycle) {
                                 case DISCONNECTED:
                                     onDisconnected(null);
                                     break;
                                 case CONNECTED:
                                     onConnected();
                                     break;
                                 }
                             }
                         })
        );

        subscriptions.add(
            historian.load()
                     .observeOn(AndroidSchedulers.mainThread())
                     .subscribe(adapter)
        );
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

        case R.id.action_fake_list:
            if (connections.getAdapter() == adapter) {
                final List<HistoricalConnection> dummies = new ArrayList<>();
                for (int i=10001; i < 10021; i++) {
                    dummies.add(new HistoricalConnection("192.168.1.1", i));
                }

                final HistoryAdapter adapter = new HistoryAdapter();
                adapter.call(dummies);
                connections.setAdapter(adapter);
            } else {
                connections.setAdapter(adapter);
                adapter.call(adapter.list); // updates the empty view
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (newConnections.getVisibility() == View.VISIBLE) {
            closeFab();
            return;
        }

        super.onBackPressed();
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

        connections.animate().alpha(0.5f);
        connect(new HistoricalConnection(hostRaw.trim(), portNo));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @OnClick(R.id.fab) void openFab() {
        // might this be better as a dialog?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            newConnections.setVisibility(View.VISIBLE);
            newConnections.setTranslationY(0);

            final float fabRadius = fab.getWidth() / 2f;
            ViewAnimationUtils.createCircularReveal(newConnections,
                    (int) (fab.getX() + fabRadius),
                    (int) (fab.getY() + fabRadius),
                    fabRadius,
                    newConnections.getHeight() + newConnections.getWidth() / 2)
                .start();

            // we *could* put this into the layout so it gets revealed as well...
            UiUtil.animateStatusBarColor(
                    getWindow(),
                    R.color.primary_dark_material_dark,
                    R.color.primary_dark_material_light
            );

        } else {
            newConnections.setAlpha(0);
            newConnections.setVisibility(View.VISIBLE);
            newConnections.animate().alpha(1);
        }
    }

    void closeFab() {

        if (newConnections.getVisibility() == View.GONE) {
            // nothing to do
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark_material_dark));
        }

        newConnections.animate()
                      .translationY(newConnections.getHeight())
                      .setInterpolator(new FastOutLinearInInterpolator())
                      .setDuration(175)
                      .withEndAction(new Runnable() {
                          @Override
                          public void run() {
                              newConnections.setVisibility(View.GONE);
                          }
                      });
    }

    void connect(final HistoricalConnection info) {
        Snackbar.make(pickRoot(), R.string.connecting, Snackbar.LENGTH_SHORT).show();
        ButterKnife.apply(allViews, ENABLED, false);
        connections.animate().alpha(0.5f);

        pendingConnection = info;
        subscriptions.add(
            connection.connect(info.getHost(), info.getPort())
                      .subscribe(new Observer<Connection>() {
                          @Override
                          public void onCompleted() {
                              Log.v("ctrlf", "Connection ready");
                              // TODO some UI?
                          }

                          @Override
                          public void onError(final Throwable throwable) {
                              Log.w("ctrlf", "Error connecting to sim", throwable);
                              onDisconnected(throwable);
                          }

                          @Override
                          public void onNext(final Connection connection) {

                          }
                      })
        );
    }

    /** Called on successful connection */
    void onConnected() {
        closeFab();

        if (pendingConnection != null) {
            // it'd be null for dummy connection
            historian.connect(pendingConnection);
        }

        connections.animate().alpha(1);
        ButterKnife.apply(allViews, ENABLED, true);
        startActivity(new Intent(this, ControlsActivity.class));
    }

    void onDisconnected(final Throwable throwable) {
        connections.animate().alpha(1);
        ButterKnife.apply(allViews, ENABLED, true);
        Snackbar.make(pickRoot(),
                pickDisconnectedMessage(throwable),
                Snackbar.LENGTH_LONG).show();
    }

    private View pickRoot() {
        return newConnections.getVisibility() == View.VISIBLE
            ? newConnections
            : root;
    }

    private CharSequence pickDisconnectedMessage(final Throwable throwable) {
        if (throwable == null) {
            return getString(R.string.sim_disconnect);
        }

        final String base = throwable.getMessage();
        if (base.startsWith("failed") || base.startsWith("Failed")) {
            return base;
        }

        return getString(R.string.connect_error, base);
    }

    class HistoryHolder
            extends ViewHolder
            implements OnClickListener {

        private HistoricalConnection myConnection;

        public HistoryHolder(final View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            itemView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(final View v, final MotionEvent event) {
                    // steal input while connecting
                    if (!connections.isEnabled()) return true;
                    return false;
                }
            });
        }

        void bind(final HistoricalConnection connection) {
            myConnection = connection;
            ((TextView) itemView).setText(connection.toString());
        }

        @Override
        public void onClick(final View v) {
            Log.v("ctrlf", "Select " + myConnection);
            connect(myConnection);
        }
    }

    class HistoryAdapter
            extends RecyclerView.Adapter<HistoryHolder>
            implements Action1<List<HistoricalConnection>> {

        private LayoutInflater inflater = LayoutInflater.from(ConnectActivity.this);

        private List<HistoricalConnection> list;

        @Override
        public HistoryHolder onCreateViewHolder(
                final ViewGroup parent, final int viewType) {
            return new HistoryHolder(inflater.inflate(
                    R.layout.listitem_history, parent, false));
        }

        @Override
        public void onBindViewHolder(final HistoryHolder holder, final int position) {
            holder.bind(list.get(position));
        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }

        @Override
        public void call(final List<HistoricalConnection> list) {
            final List<HistoricalConnection> old = this.list;
            this.list = list;

            final int oldSize = old == null ? 0 : old.size();
            final int newSize = list.size();
            if (oldSize == 0 || Math.abs(oldSize - newSize) > 1) {
                notifyDataSetChanged();
            } else if (oldSize == newSize) {
                // an element was selected
                final int oldPosition = old.indexOf(list.get(0));
                if (oldPosition != 0) {
                    notifyItemMoved(oldPosition, 0);
                }
            } else if (oldSize < newSize) {
                // new element (we always insert up front)
                notifyItemInserted(0);
            } else {
                // deleted element
                boolean found = false;
                for (int i=0; i < oldSize; i++) {
                    if (!list.contains(old.get(i))) {
                        notifyItemRemoved(i);
                        found = true;
                        break;
                    }
                }

                // just in case
                if (!found) notifyDataSetChanged();
            }

            emptyView.setVisibility(newSize == 0 ? View.VISIBLE : View.GONE);
        }

    }
}
