package net.dhleong.ctrlf.module;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import dagger.Module;
import dagger.Provides;
import net.dhleong.ctrlf.util.scopes.Pref;

import javax.inject.Singleton;

/**
 * @author dhleong
 */
@Module
public class PrefsModule {

    public static final String SCREEN_ON = "screen_on";

    public static final String LAST_HOST = "last_host";
    public static final String LAST_PORT = "last_port";

    @Provides @Singleton SharedPreferences providePrefs(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides @Pref(SCREEN_ON) boolean provideScreenOnPref(SharedPreferences prefs) {
        return prefs.getBoolean("pref_screen_on", true);
    }

    @Provides @Pref(LAST_HOST) String provideLastHost(SharedPreferences prefs) {
        return prefs.getString(LAST_HOST, "");
    }

    @Provides @Pref(LAST_PORT) String provideLastPort(SharedPreferences prefs) {
        return prefs.getString(LAST_PORT, "");
    }

}
