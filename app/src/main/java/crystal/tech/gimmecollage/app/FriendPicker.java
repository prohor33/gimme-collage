package crystal.tech.gimmecollage.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import crystal.tech.gimmecollage.instagram_api.InstagramAPI;
import crystal.tech.gimmecollage.instagram_api.Storage;

import com.squareup.picasso.Picasso;

/**
 * Created by prohor on 28/09/14.
 */

public class FriendPicker extends ActionBarActivity {

    private static final String DEBUG_TAG = "FriendPicker";
    private static final int INSTAGRAM_AUTH_REQUEST = 1;

    private FriendPickerAdapter friendPickerAdapter = null;
    private ProgressDialog loadingProgress = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_picker);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        GridView friendGridView = (GridView) findViewById(R.id.friendGridView);

        Log.v(DEBUG_TAG, "isAuthenticated() = " + InstagramAPI.isAuthenticated());
        if (InstagramAPI.isAuthenticated()) {
            loadSelfFollows();
        } else {
            Intent intent = new Intent(FriendPicker.this, AuthenticationActivity.class);
            startActivityForResult(intent, INSTAGRAM_AUTH_REQUEST);
        }

        friendPickerAdapter = new FriendPickerAdapter(this);
        friendGridView.setAdapter(friendPickerAdapter);

        friendGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                pickFriend(position);
                // Toast.makeText(FriendPicker.this, "" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == INSTAGRAM_AUTH_REQUEST) {
            if(resultCode == RESULT_OK) {
                if (InstagramAPI.isAuthenticated())
                    loadSelfFollows();
                else
                    Log.v(DEBUG_TAG, "error in onActivityResult(): no auth");
            } else {
                // Errors during AuthActivity or Canceled...
                Log.d(DEBUG_TAG, "AuthActivityResult = " + resultCode);
                FriendPicker.this.finish();
            }
        }
    }

    public class FriendPickerAdapter extends BaseAdapter {
        private Context mContext;

        public FriendPickerAdapter(Context context) { mContext = context; }

        public int getCount() {
            return mUserInfos.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ImageView imageView;
            TextView textView;

            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.layout_friend, parent, false);
                view.setTag(R.id.picture, view.findViewById(R.id.picture));
                view.setTag(R.id.text, view.findViewById(R.id.text));
            }

            imageView = (ImageView) view.getTag(R.id.picture);
            textView = (TextView) view.getTag(R.id.text);

            Storage.UserInfo userInfo = mUserInfos.get(position);
            Picasso.with(mContext).load(userInfo.profile_picture).into(imageView);
            String name = userInfo.full_name.isEmpty() ? userInfo.username : userInfo.full_name;
            textView.setText(name);
            //Log.d(DEBUG_TAG, "For user - " + name + " loaded " + userInfo.profile_picture);

            return view;
        }

    }

    private List<Storage.UserInfo> mUserInfos = new ArrayList<Storage.UserInfo>(0);
    private int mSelectedFriendID = -1;

    private void loadSelfFollows() {
        mUserInfos = InstagramAPI.getFollows();
        if (mUserInfos.size() != 0) {
            Log.v(DEBUG_TAG, "Already have " + mUserInfos.size() + " follows");
            return;
        }

        Log.v(DEBUG_TAG, "No follows, try to load...");
        loadingProgress = new ProgressDialog(this);
        loadingProgress.setTitle("Loading");
        loadingProgress.setMessage("Please, wait...");
        loadingProgress.show();
        InstagramAPI.with(follows_load_listener).updateFollows();
    }

    private void clearSelfFollows() {
        mUserInfos.clear();
        friendPickerAdapter.notifyDataSetChanged();
    }

    private void pickFriend(int pos) {
        if (pos < 0 || pos >= mUserInfos.size()) {
            Log.v(DEBUG_TAG, "Error pickFriend(): index is out of range");
            return;
        }
        Log.v(DEBUG_TAG, "pick friend:" + mUserInfos.get(pos).username);

        loadingProgress = new ProgressDialog(this);
        loadingProgress.setTitle("Loading");
        loadingProgress.setMessage("Please, wait...");
        loadingProgress.show();
        InstagramAPI.with(images_list_load_listener).updateImages(mUserInfos.get(pos).id);
        mSelectedFriendID = Integer.parseInt(mUserInfos.get(pos).id);
    }

    InstagramAPI.Listener images_list_load_listener = new InstagramAPI.Listener() {

        @Override
        public void onSuccess() {
            if (loadingProgress != null)
                loadingProgress.dismiss();
            Log.v(DEBUG_TAG, "Friend media list successfully loaded!");
            Log.v(DEBUG_TAG, "have images: " + InstagramAPI.getImages().size());

            Intent data = new Intent();
            data.putExtra("intSelectedFriendID", mSelectedFriendID);
            if (getParent() == null) {
                setResult(FriendPicker.RESULT_OK, data);
            } else {
                getParent().setResult(FriendPicker.RESULT_OK, data);
            }
            FriendPicker.this.finish();
            Log.v(DEBUG_TAG, "finish activity");
        }

        @Override
        public void onFail(String error) {
            if (loadingProgress != null)
                loadingProgress.dismiss();
            Log.v(DEBUG_TAG, "Failed to load friend media list");
            Toast.makeText(FriendPicker.this, error, Toast.LENGTH_SHORT).show();
        }
    };

    InstagramAPI.Listener follows_load_listener = new InstagramAPI.Listener() {

        @Override
        public void onSuccess() {
            if (loadingProgress != null)
                loadingProgress.dismiss();
            if (friendPickerAdapter != null)
                friendPickerAdapter.notifyDataSetChanged();
        }

        @Override
        public void onFail(String error) {
            if (loadingProgress != null)
                loadingProgress.dismiss();
            Toast.makeText(FriendPicker.this, error, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.friend_picker, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        // Configure the search info and add any event listeners
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            // ClearActivityData
            clearSelfFollows();
            // Reset Auth infos.
            InstagramAPI.resetAuthentication();
            // Change activity to Auth Activity due to prevent from using FriendPickerData
            Intent intent = new Intent(FriendPicker.this, AuthenticationActivity.class);
            startActivityForResult(intent, INSTAGRAM_AUTH_REQUEST);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}