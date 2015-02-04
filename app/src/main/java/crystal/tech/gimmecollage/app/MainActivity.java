package crystal.tech.gimmecollage.app;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.analytics.GoogleAnalytics;

import java.util.ArrayList;
import java.util.List;

import crystal.tech.gimmecollage.ads.Ads;
import crystal.tech.gimmecollage.analytics.LocalStatistics;
import crystal.tech.gimmecollage.collagemaker.CollageMaker;
import crystal.tech.gimmecollage.collagemaker.ImageStorage;
import crystal.tech.gimmecollage.navdrawer.NavigationDrawerCallbacks;
import crystal.tech.gimmecollage.navdrawer.NavigationDrawerFragment;
import crystal.tech.gimmecollage.navdrawer.NavigationItem;
import crystal.tech.gimmecollage.navdrawer.SimpleDrawerCallbacks;
import crystal.tech.gimmecollage.navdrawer.SimpleDrawerFragment;

public class MainActivity extends ActionBarActivity implements NavigationDrawerCallbacks, SimpleDrawerCallbacks {

    private static final String TAG = "MainActivity";

    private Toolbar mToolbar;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private SimpleDrawerFragment mSimpleDrawerFragment;
    static final int ADD_PICTURES_REQUEST = 1;  // The request code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        List<NavigationItem> navigationItems = new ArrayList<NavigationItem>();
        navigationItems.add(new NavigationItem("item 1", getResources().getDrawable(R.drawable.ic_action_select)));
        navigationItems.add(new NavigationItem("item 2", getResources().getDrawable(R.drawable.ic_action_select)));
        navigationItems.add(new NavigationItem("item 3", getResources().getDrawable(R.drawable.ic_action_select)));

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_drawer_left);
        mNavigationDrawerFragment.loadList(navigationItems);
        mNavigationDrawerFragment.setup(R.id.fragment_drawer_left,
                (DrawerLayout) findViewById(R.id.drawer), mToolbar);

        mSimpleDrawerFragment = (SimpleDrawerFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_drawer_right);
        mSimpleDrawerFragment.setup((DrawerLayout) findViewById(R.id.drawer));

        CollageMaker.getInstance().putMainActivity(this);

        //startApp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
//        if (mDrawerToggle.onOptionsItemSelected(item)) {
//            return true;
//        }
        // Handle action buttons
        switch(item.getItemId()) {
//            case R.id.action_websearch:
//                // create intent to perform web search for this planet
//                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
//                intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
//                // catch event that there's no activity to handle intent
//                if (intent.resolveActivity(getPackageManager()) != null) {
//                    startActivity(intent);
//                } else {
//                    Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
//                }
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ADD_PICTURES_REQUEST) {
            if (resultCode == RESULT_OK) {
                ImageStorage.moveAllImagesFromPullToCollage();
                CollageMaker.updateImageData();
                mSimpleDrawerFragment.getAdapter().notifyDataSetChanged();
                if (ImageStorage.getPullImageCount() == 0)
                    mSimpleDrawerFragment.closeDrawer();
            }
        }
    }

    /* Callback for NavigationDrawerFragments. */
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        switch (position) {
            case 0:
                startFragment(new CollageFragment().newInstance("", ""));
                break;
            case 1:
                startFragment(new NewsFragment().newInstance("", ""));
                break;
            default:
                Log.e(TAG, "Wrong position of navigation drawer!");
                break;
        }

        //Toast.makeText(this, "Menu item -> " + position, Toast.LENGTH_SHORT).show();
    }

    /* Callback for SimpleDrawerFragments. */
    @Override
    public void onSimpleDrawerItemSelected(int position) {
        // TODO: implement
    }

    /* Callback for SimpleDrawerFragments. */
    @Override
    public void onSimpleDrawerAddImage() {
        // Spawn ImageSourceActivity.
        Intent intent = new Intent(MainActivity.this, ImageSourceActivity.class);
        startActivityForResult(intent, ADD_PICTURES_REQUEST);
    }

    /* Replace R.id.container for presented fragment. */
    public void startFragment(Fragment fragment) {
        getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen())
            mNavigationDrawerFragment.closeDrawer();
        else
            super.onBackPressed();
    }

    public SimpleDrawerFragment getRightDrawer() {
        return mSimpleDrawerFragment;
    }

    /* Google staticstics ?.. */
    private void startApp() {
        // Load and update LocalStatistic
        LocalStatistics localStatistics = LocalStatistics.getInstance(MainActivity.this);
        localStatistics.IncrementAppUsagesNumber();

        if (Settings.showAds) {
            if (localStatistics.getAppUsagesNumber() > 2 && Math.random() < 0.5) {
                Ads.LoadInterstitial(MainActivity.this);
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
            GoogleAnalytics.getInstance(MainActivity.this).setDryRun(true);
        }
    }

}