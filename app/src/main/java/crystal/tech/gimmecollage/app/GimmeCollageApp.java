package crystal.tech.gimmecollage.app;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

/**
 * Created by prohor on 28/12/14.
 */
public class GimmeCollageApp extends Application {

    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     *
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    private static HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public static synchronized Tracker getAppTracker(Activity activity) {
        if (!mTrackers.containsKey(TrackerName.APP_TRACKER)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(activity);
            Tracker t = analytics.newTracker(R.xml.app_tracker);
            t.enableAutoActivityTracking(true);
            mTrackers.put(TrackerName.APP_TRACKER, t);

            analytics.enableAutoActivityReports(activity.getApplication());
        }
        return mTrackers.get(TrackerName.APP_TRACKER);
    }
}
