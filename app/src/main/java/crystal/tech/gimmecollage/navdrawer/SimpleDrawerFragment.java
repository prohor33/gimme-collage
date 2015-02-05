package crystal.tech.gimmecollage.navdrawer;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import crystal.tech.gimmecollage.app.R;
import crystal.tech.gimmecollage.collagemaker.ImageData;
import crystal.tech.gimmecollage.collagemaker.ImageStorage;

/**
 * Created by poliveira on 24/10/2014.
 */
public class SimpleDrawerFragment extends Fragment implements SimpleDrawerCallbacks {
    private static final String PREF_USER_LEARNED_DRAWER = "Simple_drawer_learned";
    private static final String STATE_SELECTED_POSITION = "selected_Simple_drawer_position";
    private static final String PREFERENCES_FILE = "my_app_settings"; //TODO: change this to your file
    private SimpleDrawerCallbacks mCallbacks;
    private RecyclerView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;
    private int mCurrentSelectedPosition;
    private SimpleDrawerAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_right_nav_drawer, container, false);
        mDrawerList = (RecyclerView) view.findViewById(R.id.drawerList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mDrawerList.setLayoutManager(layoutManager);
        mDrawerList.setHasFixedSize(true);

        ImageView addImageView = (ImageView) view.findViewById(R.id.drawerAddIimageView);
        addImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallbacks.onSimpleDrawerAddImage();
            }
        });

        mAdapter = new SimpleDrawerAdapter();
        mAdapter.setSimpleDrawerCallbacks(this);
        mDrawerList.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserLearnedDrawer = Boolean.valueOf(readSharedSetting(getActivity(), PREF_USER_LEARNED_DRAWER, "false"));
        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (SimpleDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement SimpleDrawerCallbacks.");
        }
    }

    public SimpleDrawerAdapter getAdapter() {
        return mAdapter;
    }

    public void setup(DrawerLayout drawerLayout) {
        mDrawerLayout = drawerLayout;
        mDrawerLayout.setStatusBarBackgroundColor(
                getResources().getColor(R.color.myPrimaryDarkColor));

        /*
        if (!mUserLearnedDrawer && !mFromSavedInstanceState)
            mDrawerLayout.openDrawer(this.getView());
            */
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(this.getView());
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(this.getView());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    void selectItem(int position) {
        mCurrentSelectedPosition = position;
//        if (mDrawerLayout != null) {
//            mDrawerLayout.closeDrawer(this.getView());
//        }
        if (mCallbacks != null) {
            mCallbacks.onSimpleDrawerItemSelected(position);
        }
        ((SimpleDrawerAdapter) mDrawerList.getAdapter()).selectPosition(position);
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(this.getView());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onSimpleDrawerItemSelected(int position) {
        selectItem(position);
    }

    @Override
    public void onSimpleDrawerAddImage() {
        if (mCallbacks != null) {
            mCallbacks.onSimpleDrawerAddImage();
        }
    }

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    public void setDrawerLayout(DrawerLayout drawerLayout) {
        mDrawerLayout = drawerLayout;
    }

    public static void saveSharedSetting(Context ctx, String settingName, String settingValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(settingName, settingValue);
        editor.apply();
    }

    public static String readSharedSetting(Context ctx, String settingName, String defaultValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(settingName, defaultValue);
    }
}
