package crystal.tech.gimmecollage.ads;

import android.app.Activity;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import crystal.tech.gimmecollage.app.R;

/**
 * Created by prohor on 30/11/14.
 */
public class Ads {

    private static InterstitialAd mInterstitial = null;

    public static void showBanner(Activity activity) {

        AdView mAdView = new AdView(activity);
        mAdView.setAdUnitId(activity.getResources().getString(R.string.admob_banner_id));
        mAdView.setAdSize(AdSize.BANNER);
//        mAdView.setAdListener(new ToastAdListener(this));
        LinearLayout main_lr = (LinearLayout) activity.findViewById(R.id.layoutMain);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        main_lr.addView(mAdView, params);
        mAdView.loadAd(new AdRequest.Builder().build());
    }

    public static void LoadInterstitial(Activity activity) {
        mInterstitial = new InterstitialAd(activity);
//        mInterstitial.setAdUnitId(activity.getResources().getString(R.string.admob_interstitial_graphic_id));
        mInterstitial.setAdUnitId(activity.getResources().getString(R.string.admob_interstitial_video_id));
        mInterstitial.loadAd(new AdRequest.Builder().build());
//        mInterstitial.setAdListener(new AdListener() {
//            @Override
//            public void onAdLoaded() {
//                super.onAdLoaded();
//                mInterstitial.show();
//            }
//        });
    }

    public static boolean ShowInterstitial() {
        if (mInterstitial != null && mInterstitial.isLoaded()) {
            mInterstitial.show();
            return true;
        }
        return false;
    }
}
