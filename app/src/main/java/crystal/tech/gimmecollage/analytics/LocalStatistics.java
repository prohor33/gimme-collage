package crystal.tech.gimmecollage.analytics;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by prohor on 28/12/14.
 */
public class LocalStatistics {

    private static final String SHARED = "LocalStatistics_Preferences";

    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor mEditor;

    private static final String APP_USAGE_NUMBER = "AppUsageNumber";
    private int mAppUsagesNumber = 0;

    LocalStatistics(Context context) {
        mSharedPref = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();
        ReStoreData();
    }

    private static LocalStatistics mInstance = null;

    public static synchronized LocalStatistics getInstance(Activity activity) {
        if (mInstance == null) {
            mInstance = new LocalStatistics(activity);
        }
        return mInstance;
    }

    public void IncrementAppUsagesNumber() {
        mAppUsagesNumber++;
        Log.v(SHARED, "IncrementAppUsagesNumber: mAppUsagesNumber = " + mAppUsagesNumber);
        StoreData();
    }

    public void StoreData() {
        mEditor.putInt(APP_USAGE_NUMBER, mAppUsagesNumber);
        mEditor.commit();
    }
    public void ReStoreData() {
        mAppUsagesNumber = mSharedPref.getInt(APP_USAGE_NUMBER, 0);
        Log.v(SHARED, "mAppUsagesNumber: mAppUsagesNumber = " + mAppUsagesNumber);
    }
    public void ResetData() {
        mAppUsagesNumber = 0;
    }

    public int getAppUsagesNumber() {
        return mAppUsagesNumber;
    }
}
