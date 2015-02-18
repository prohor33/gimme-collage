package crystal.tech.gimmecollage.app;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import crystal.tech.gimmecollage.ads.Ads;
import crystal.tech.gimmecollage.analytics.GoogleAnalyticsUtils;
import crystal.tech.gimmecollage.collagemaker.CollageUtils;

import java.io.File;

import crystal.tech.gimmecollage.collagemaker.CollageMaker;


public class CollageFragment extends Fragment {

    private static final String TAG = "CollageActivity";
    private static final int INSTAGRAM_FRIEND_REQUEST = 1;
    private static final int GALLERY_REQUEST = 2;
    private ProgressDialog m_dialogProgress = null;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NewsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CollageFragment newInstance(String param1, String param2) {
        CollageFragment fragment = new CollageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public CollageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_collage,
                container, false);

        CollageMaker.init(getActivity(), rootView);
        CollageUtils.addFloatingActionButtons(rootView);
        CollageUtils.addCollageTypeSelectorLayout(rootView);
        CollageMaker.initImageViews(rootView);

        // reading settings
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean disable_image_action_buttons =
                sharedPref.getBoolean("pref_key_disable_image_action_buttons", false);
        CollageUtils.getImageActionButtons().putDisabled(disable_image_action_buttons);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CollageMaker.deselectAllViews();
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (Settings.showAds) {
            if (Ads.ShowInterstitial()) {
                GoogleAnalyticsUtils.SendEvent(getActivity(),
                        R.string.ga_event_category_see_interstitial_back_to_main_activity,
                        R.string.ga_event_action_see_interstitial_back_to_main_activity,
                        R.string.ga_event_label_see_interstitial_back_to_main_activity);
            }
        }

        switch (requestCode) {
            case INSTAGRAM_FRIEND_REQUEST:
                break;
            case GALLERY_REQUEST:
                break;
            default:
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_share:

                GoogleAnalyticsUtils.SendEvent(getActivity(),
                        R.string.ga_event_category_share_via_action_bar,
                        R.string.ga_event_action_share_via_action_bar,
                        R.string.ga_event_label_share_via_action_bar);

                break;
            case R.id.action_save:

                GoogleAnalyticsUtils.SendEvent(getActivity(),
                        R.string.ga_event_category_save_via_action_bar,
                        R.string.ga_event_action_save_via_action_bar,
                        R.string.ga_event_label_save_via_action_bar);

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public interface FileSaveCallback {
        void onSuccess(File file_result);

        void onError();

        public static class EmptyFileSaveCallback implements FileSaveCallback {

            @Override public void onSuccess(File file_result) {
            }

            @Override public void onError() {
            }
        }
    }
}
