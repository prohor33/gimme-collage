package crystal.tech.gimmecollage.app;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;

import crystal.tech.gimmecollage.ads.Ads;
import crystal.tech.gimmecollage.analytics.LocalStatistics;
import crystal.tech.gimmecollage.navdrawer.NavigationDrawerCallbacks;
import crystal.tech.gimmecollage.navdrawer.NavigationDrawerFragment;

public class MainActivity extends ActionBarActivity implements NavigationDrawerCallbacks {

    private static final String TAG = "MainActivity";

    private Toolbar mToolbar;
    private NavigationDrawerFragment mNavigationDrawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        //Fragment fragment = new CollageActivity().newInstance("", "");
        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_drawer);
        mNavigationDrawerFragment.setup(R.id.fragment_drawer,
                (DrawerLayout) findViewById(R.id.drawer), mToolbar);

        startApp();
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
    public void onNavigationDrawerItemSelected(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new CollageActivity().newInstance("", "");
                break;
            default:
                Log.e(TAG, "Wrong position of navigation drawer!");
                break;
        }
        if(fragment != null) {
            getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        }

        Toast.makeText(this, "Menu item selected -> " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen())
            mNavigationDrawerFragment.closeDrawer();
        else
            super.onBackPressed();
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