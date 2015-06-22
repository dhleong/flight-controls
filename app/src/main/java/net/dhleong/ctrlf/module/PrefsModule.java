package net.dhleong.ctrlf.module;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import dagger.Module;
import dagger.Provides;
import net.dhleong.ctrlf.history.ConnectionHistorian;
import net.dhleong.ctrlf.history.HistoricalConnection;
import net.dhleong.ctrlf.util.scopes.Pref;
import rx.Observable;
import rx.subjects.ReplaySubject;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dhleong
 */
@Module
public class PrefsModule {

    public static final String SCREEN_ON = "screen_on";

//    public static final String LAST_HOST = "last_host";
//    public static final String LAST_PORT = "last_port";

    public static final String HISTORY = "history";

    @Provides @Singleton SharedPreferences providePrefs(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides @Pref(SCREEN_ON) boolean provideScreenOnPref(SharedPreferences prefs) {
        return prefs.getBoolean("pref_screen_on", true);
    }

//    @Provides @Pref(LAST_HOST) String provideLastHost(SharedPreferences prefs) {
//        return prefs.getString(LAST_HOST, "");
//    }
//
//    @Provides @Pref(LAST_PORT) String provideLastPort(SharedPreferences prefs) {
//        return prefs.getString(LAST_PORT, "");
//    }

    /**
     * Lame implementation using shared prefs
     * @param prefs
     * @return
     */
    @Provides ConnectionHistorian provideHistorian(final SharedPreferences prefs) {
        final ReplaySubject<List<HistoricalConnection>> subject =
                ReplaySubject.create();

        final List<HistoricalConnection> list = extractList(prefs);
        subject.onNext(list);

        return new ConnectionHistorian() {
            @Override
            public void connect(final HistoricalConnection connection) {
                list.remove(connection);
                list.add(0, connection);

                update();
            }

            @Override
            public void delete(final HistoricalConnection connection) {
                list.remove(connection);
                update();
            }

            @Override
            public Observable<List<HistoricalConnection>> load() {
                return subject;
            }

            void update() {
                subject.onNext(list);
                serializeList(prefs, list);
            }
        };
    }

    static List<HistoricalConnection> extractList(final SharedPreferences prefs) {
        final List<HistoricalConnection> history = new ArrayList<>();
        final String raw = prefs.getString(HISTORY, null);
        if (raw == null) {
            return history;
        }

        int prev = 0, next = raw.indexOf(':', prev);
        while (next != -1) {
            final String host = raw.substring(prev, next);
            prev += host.length() + 1; // include the `:`
            next = raw.indexOf(':', prev);

            final String port = raw.substring(prev, next);
            prev += port.length() + 1; // include the `:`
            next = raw.indexOf(':', prev);

            history.add(new HistoricalConnection(host, Integer.parseInt(port)));
        }

        return history;
    }

    static void serializeList(final SharedPreferences prefs,
            final List<HistoricalConnection> list) {
        prefs.edit()
             .putString(HISTORY, serializeItems(list))
             .apply();
    }

    static String serializeItems(final List<HistoricalConnection> list) {
        final StringBuilder builder = new StringBuilder(64);
        for (final HistoricalConnection conn : list) {
            builder.append(conn.getHost())
                   .append(':')
                   .append(conn.getPort())
                   .append(':');
        }
        return builder.toString();
    }

}
