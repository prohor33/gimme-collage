package crystal.tech.gimmecollage.analytics;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import crystal.tech.gimmecollage.app.GimmeCollageApp;
import crystal.tech.gimmecollage.app.R;

/**
 * Created by prohor on 27/12/14.
 */


public class GoogleAnalyticsUtils {

    static String TAG = "GoogleAnalyticsUtils";

    // have already sent automatically?
    // it's just information that user is viewing the screen, not the screen shot
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

    // Tracking events =============================================================================
    public static void trackShowInterstitialWhenBackToMainActivity(Activity activity) {
        SendEvent(activity,
                R.string.ga_event_category_see_interstitial_back_to_main_activity,
                R.string.ga_event_action_see_interstitial_back_to_main_activity,
                R.string.ga_event_label_see_interstitial_back_to_main_activity);
    }
    public static void trackSaveTheResultsViaFab(Activity activity) {
        SendEvent(activity,
                R.string.ga_event_category_save_via_fab,
                R.string.ga_event_action_save_via_fab,
                R.string.ga_event_label_save_via_fab);
    }
    public static void trackShareTheResultsViaFab(Activity activity) {
        SendEvent(activity,
                R.string.ga_event_category_share_via_fab,
                R.string.ga_event_action_share_via_fab,
                R.string.ga_event_label_share_via_fab);
    }
    public static void trackSaveTheResultsViaActionBar(Activity activity) {
        SendEvent(activity,
                R.string.ga_event_category_save_via_action_bar,
                R.string.ga_event_action_save_via_action_bar,
                R.string.ga_event_label_save_via_action_bar);
    }
    public static void trackShareTheResultsViaActionBar(Activity activity) {
        SendEvent(activity,
                R.string.ga_event_category_share_via_action_bar,
                R.string.ga_event_action_share_via_action_bar,
                R.string.ga_event_label_share_via_action_bar);
    }
    public static void trackOpenPullViaCollageTouch(Activity activity) {
        SendEvent(activity,
                R.string.ga_event_category_open_pull_via_touch_collage,
                R.string.ga_event_action_open_pull_via_touch_collage,
                R.string.ga_event_label_open_pull_via_touch_collage);
    }
    public static void trackSelectImageSourceGallery(Activity activity) {
        SendEvent(activity,
                R.string.ga_event_category_select_img_src_gallery,
                R.string.ga_event_action_select_img_src_gallery,
                R.string.ga_event_label_select_img_src_gallery);
    }
    public static void trackSelectImageSourceInstagram(Activity activity) {
        SendEvent(activity,
                R.string.ga_event_category_select_img_src_instagram,
                R.string.ga_event_action_select_img_src_instagram,
                R.string.ga_event_label_select_img_src_instagram);
    }
    public static void trackInstagramAuthSuccess(Activity activity) {
        SendEvent(activity,
                R.string.ga_event_category_instagram_auth_success,
                R.string.ga_event_action_instagram_auth_success,
                R.string.ga_event_label_instagram_auth_success);
    }
    public static void trackInstagramAuthFailed(Activity activity) {
        SendEvent(activity,
                R.string.ga_event_category_instagram_auth_failed,
                R.string.ga_event_action_instagram_auth_failed,
                R.string.ga_event_label_instagram_auth_failed);
    }
    public static void trackOpenBackgroundColorPickerViaActionButton(Activity activity) {
        SendEvent(activity,
                R.string.ga_event_category_open_back_clr_picker_via_action_btn,
                R.string.ga_event_action_open_back_clr_picker_via_action_btn,
                R.string.ga_event_label_open_back_clr_picker_via_action_btn);
    }
    public static void trackOpenBackgroundColorPickerViaActionBar(Activity activity) {
        SendEvent(activity,
                R.string.ga_event_category_open_back_clr_picker_via_action_bar,
                R.string.ga_event_action_open_back_clr_picker_via_action_bar,
                R.string.ga_event_label_open_back_clr_picker_via_action_bar);
    }
    public static void trackTrashViaFab(Activity activity) {
        SendEvent(activity,
                R.string.ga_event_category_trash_via_fab,
                R.string.ga_event_action_trash_via_fab,
                R.string.ga_event_label_trash_via_fab);
    }
    public static void trackTrashViaActionBar(Activity activity) {
        SendEvent(activity,
                R.string.ga_event_category_trash_via_action_bar,
                R.string.ga_event_action_trash_via_action_bar,
                R.string.ga_event_label_trash_via_action_bar);
    }
    public static void trackImageActionButtonRotateLeft(Activity activity) {
        SendEvent(activity,
                R.string.ga_event_category_img_action_btn_rotate_left,
                R.string.ga_event_action_img_action_btn_rotate_left,
                R.string.ga_event_label_img_action_btn_rotate_left);
    }
    public static void trackImageActionButtonRotateRight(Activity activity) {
        SendEvent(activity,
                R.string.ga_event_category_img_action_btn_rotate_right,
                R.string.ga_event_action_img_action_btn_rotate_right,
                R.string.ga_event_label_img_action_btn_rotate_right);
    }
    public static void trackApplyGalleryImagesInActionBar(Activity activity) {
        SendEvent(activity,
                R.string.ga_event_category_apply_gallery_images,
                R.string.ga_event_action_apply_gallery_images,
                R.string.ga_event_label_apply_gallery_images);
    }
    public static void trackSwapCollageImages(Activity activity) {
        SendEvent(activity,
                R.string.ga_event_category_swap_collage_images,
                R.string.ga_event_action_swap_collage_images,
                R.string.ga_event_label_swap_collage_images);
    }
    public static void trackSwapImageFromPull(Activity activity) {
        SendEvent(activity,
                R.string.ga_event_category_swap_image_from_pull,
                R.string.ga_event_action_swap_image_from_pull,
                R.string.ga_event_label_swap_image_from_pull);
    }
    public static void trackNavigationDrawerSelectItem(Activity activity) {
        SendEvent(activity,
                R.string.ga_event_category_item_selected_nav_drawer,
                R.string.ga_event_action_item_selected_nav_drawer,
                R.string.ga_event_label_item_selected_nav_drawer);
    }
}
