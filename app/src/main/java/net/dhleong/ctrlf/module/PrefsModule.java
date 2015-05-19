package net.dhleong.ctrlf.module;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import dagger.Module;
import dagger.Provides;
import net.dhleong.ctrlf.App;
import net.dhleong.ctrlf.util.Named;

import javax.inject.Singleton;

/**
 * @author dhleong
 */
@Module
public class PrefsModule {

    @Provides @Singleton SharedPreferences providePrefs(final App context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides @Named("screen_on") boolean provideScreenOnPref(SharedPreferences prefs) {
        return prefs.getBoolean("pref_screen_on", true);
    }
}
