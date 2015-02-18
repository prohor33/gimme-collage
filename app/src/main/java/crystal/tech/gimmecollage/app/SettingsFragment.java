package crystal.tech.gimmecollage.app;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = "SettingsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hide options menu
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.putOptionsMenuVisibility(false);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

}
