package crystal.tech.gimmecollage.analytics;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

import crystal.tech.gimmecollage.app.GimmeCollageApp;
import crystal.tech.gimmecollage.app.R;

/**
 * Created by prohor on 27/12/14.
 */


public class GoogleAnalyticsUtils {

    static String TAG = "GoogleAnalyticsUtils";

    public static void SendScreenView(Activity activity) {
        // Get tracker.
        Tracker t = ((GimmeCollageApp)activity.getApplication()).getAppTracker(activity);

        // Set screen name.
        // Where path is a String representing the screen name.
        t.setScreenName("GimmeCollage ScreenView");

        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());
        Log.v(TAG, "SendScreenView to GoogleAnalytics");
    }

    public static void SendEvent(Activity activity, final int categoryId, final int actionId,
                                 final int labelId) {

        // Get tracker.
        Tracker t = ((GimmeCollageApp)activity.getApplication()).getAppTracker(activity);
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory(activity.getResources().getString(categoryId))
                .setAction(activity.getResources().getString(actionId))
                .setLabel(activity.getResources().getString(labelId))
                .build());

    }

    public static void SendEventWithValue(Activity activity, final int categoryId, final int actionId,
                                          final int labelId, final long value) {

        // Get tracker.
        Tracker t = ((GimmeCollageApp)activity.getApplication()).getAppTracker(activity);
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory(activity.getResources().getString(categoryId))
                .setAction(activity.getResources().getString(actionId))
                .setLabel(activity.getResources().getString(labelId))
                .setValue(value)
                .build());

    }
}
