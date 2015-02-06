package crystal.tech.gimmecollage.app;

import com.google.android.gms.analytics.GoogleAnalytics;

import crystal.tech.gimmecollage.ads.Ads;
import crystal.tech.gimmecollage.analytics.LocalStatistics;
import crystal.tech.gimmecollage.collagemaker.CollageMaker;
import crystal.tech.gimmecollage.instagram_api.InstagramAPI;
import crystal.tech.gimmecollage.navdrawer.SimpleDrawerFragment;

/**
 * Created by prohor on 06/02/15.
 */
public class Application {

    private final String TAG = "Application";

    private static Application instance;
    private MainActivity mainActivity = null;
    private boolean isStarted = false;
    private LocalStatistics localStatistics = null;

    public static synchronized Application getInstance() {
        if (instance == null) {
            instance = new Application();
        }
        return instance;
    }

    public static void startMainActivity(MainActivity main_activity) {
        getInstance().startMainActivityImpl(main_activity);
    }
    private void startMainActivityImpl(MainActivity main_activity) {
        mainActivity = main_activity;

        if (!isStarted)
            startApplication();

        // init Instagram
        if (!InstagramAPI.initialized()) {
            InstagramAPI.init(mainActivity, ApplicationData.CLIENT_ID,
                    ApplicationData.CLIENT_SECRET, ApplicationData.CALLBACK_URL);
        } else {
            InstagramAPI.putContext(mainActivity);
        }

        // init CollageMaker
        CollageMaker.getInstance().putMainActivity(mainActivity);

        if (Settings.showAds) {
            if (localStatistics.getAppUsagesNumber() > 2 && Math.random() < 0.5) {
                Ads.LoadInterstitial(mainActivity);
            }
        }

        // TODO: use Google Tag Manager
        {
//        GoogleTagManager.LoadContainer(this);

//        if (ContainerHolderSingleton.getContainerHolder() != null)
//            ContainerHolderSingleton.getContainerHolder().refresh();

//        DataLayer dataLayer = TagManager.getInstance(this).getDataLayer();
//        dataLayer.push("AppUsageNumber", LocalStatistics.getInstance(CollageActivity.this).getAppUsagesNumber());
        }

        if (!Settings.collectStatistics) {
            // When dry run is set, hits will not be dispatched, but will still be logged as
            // though they were dispatched.
            GoogleAnalytics.getInstance(mainActivity).setDryRun(true);
        }
    }

    // called ones per start
    private void startApplication() {
        isStarted = true;

        // Load and update LocalStatistic
        localStatistics = new LocalStatistics(mainActivity);
        localStatistics.IncrementAppUsagesNumber();
    }

    public static void moveRightDrawer(boolean open) {
        getInstance().moveRightDrawerImpl(open);
    }
    private void moveRightDrawerImpl(boolean open) {
        SimpleDrawerFragment rightFragment = mainActivity.getRightDrawer();
        if (open) {
            rightFragment.openDrawer();
        } else {
            rightFragment.closeDrawer();
        }
    }
}
