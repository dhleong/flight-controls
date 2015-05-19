package net.dhleong.ctrlf;

import android.os.Bundle;
import net.dhleong.ctrlf.util.AppCompatPreferenceActivity;

/**
 * @author dhleong
 */
public class PrefsActivity extends AppCompatPreferenceActivity {

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.prefs);
    }
}
