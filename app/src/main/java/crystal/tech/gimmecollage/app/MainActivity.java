package crystal.tech.gimmecollage.app;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import crystal.tech.gimmecollage.collagemaker.CollageMaker;
import crystal.tech.gimmecollage.collagemaker.ImageStorage;
import crystal.tech.gimmecollage.navdrawer.NavigationDrawerCallbacks;
import crystal.tech.gimmecollage.navdrawer.NavigationDrawerFragment;
import crystal.tech.gimmecollage.navdrawer.NavigationItem;
import crystal.tech.gimmecollage.navdrawer.SimpleDrawerCallbacks;
import crystal.tech.gimmecollage.navdrawer.SimpleDrawerFragment;

public class MainActivity extends ActionBarActivity implements
        NavigationDrawerCallbacks,
        SimpleDrawerCallbacks,
        SettingsFragment.OnSettingsFragmentInteractionListener,
        ReportFragment.OnReportFragmentInteractionListener {

    private static final String TAG = "MainActivity";

    private Toolbar mToolbar;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private SimpleDrawerFragment mSimpleDrawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        List<NavigationItem> navigationItems = new ArrayList<NavigationItem>();
        navigationItems.add(new NavigationItem(getResources().getString(R.string.drawer_item_collage),
                getResources().getDrawable(R.drawable.ic_nav_drawer_collage)));
        navigationItems.add(new NavigationItem(getResources().getString(R.string.drawer_item_settings),
                getResources().getDrawable(R.drawable.ic_nav_drawer_settings)));
        navigationItems.add(new NavigationItem(getResources().getString(R.string.drawer_item_report),
                getResources().getDrawable(R.drawable.ic_nav_drawer_report)));

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_drawer_left);
        mNavigationDrawerFragment.loadList(navigationItems);
        mNavigationDrawerFragment.setup(R.id.fragment_drawer_left,
                (DrawerLayout) findViewById(R.id.drawer), mToolbar);

        mSimpleDrawerFragment = (SimpleDrawerFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_drawer_right);
        mSimpleDrawerFragment.setup((DrawerLayout) findViewById(R.id.drawer));

        Application.startMainActivity(MainActivity.this);
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
            case R.id.action_share:
                CollageMaker.deselectAllViews();
                CollageMaker.shareCollage();
                break;
            case R.id.action_save:
                CollageMaker.deselectAllViews();
                CollageMaker.saveCollageOnDisk();
                break;
            case R.id.action_trash:
                CollageMaker.deselectAllViews();
                ImageStorage.ClearAll();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(TAG, requestCode + " " + resultCode);

        if (requestCode == Utils.ADD_PICTURES_REQUEST) {
//            if (resultCode == RESULT_OK) {
            // ??? if uncomment => go instagram -> back -> gallery -> select photos -> ok -> resultCode == 0 !!!
                ImageStorage.moveAllImagesFromPullToCollage();
                CollageMaker.updateImageData();
                mSimpleDrawerFragment.getAdapter().notifyDataSetChanged();
                if (ImageStorage.getPullImageCount() == 0)
                    mSimpleDrawerFragment.closeDrawer();
//            }
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
                startFragment(new SettingsFragment().newInstance("", ""));
                break;
            case 2:
                startFragment(new ReportFragment().newInstance("", ""));
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
        Utils.spawnAddImagesActivity(MainActivity.this);
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

    public void onSettingsInteraction(Uri uri) {
    }

    public void onReportInteraction(Uri uri) {
    }
}