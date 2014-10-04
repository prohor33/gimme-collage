package crystal.games.gimmecollage.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import crystal.games.gimmecollage.instagram_api.InstagramApp;
import crystal.games.gimmecollage.instagram_api.InstagramSession;

import com.squareup.picasso.Picasso;

/**
 * Created by prohor on 28/09/14.
 */

public class FriendPicker extends ActionBarActivity {

    private static final String DEBUG_TAG = "FriendPicker";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_picker);

        GridView friendGridView = (GridView) findViewById(R.id.friendGridView);

        // Here we must load mImageUrls
        mUserInfos = InstagramApp.getInstance().getSession().getSelfFollows();

        friendGridView.setAdapter(new FriendPickerAdapter(this));

        friendGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                pickFriend(position);
                // Toast.makeText(FriendPicker.this, "" + position, Toast.LENGTH_SHORT).show();
            }
        });
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

            InstagramSession.UserInfo userInfo = mUserInfos.get(position);
            Picasso.with(mContext).load(userInfo.profile_picture).into(imageView);
            String name = userInfo.full_name.isEmpty() ? userInfo.username : userInfo.full_name;
            textView.setText(name);
            Log.d(DEBUG_TAG, "For user - " + name + " loaded " + userInfo.profile_picture);

            return view;
        }

    }

    private List<InstagramSession.UserInfo> mUserInfos;

    private void pickFriend(int pos) {
        List<InstagramSession.UserInfo> userInfos = InstagramApp.getInstance().getSession().getSelfFollows();
        if (pos < 0 || pos >= userInfos.size()) {
            Log.v(DEBUG_TAG, "Error pickFriend(): index is out of range");
            return;
        }
        Log.v(DEBUG_TAG, "pick friend:" + userInfos.get(pos).username);

        InstagramApp.getInstance().updateImageInfo(userInfos.get(pos).id, images_list_load_listener);
    }

    InstagramApp.APIRequestListener images_list_load_listener = new InstagramApp.APIRequestListener() {

        @Override
        public void onSuccess() {
            Log.v(DEBUG_TAG, "Friend media list successfully loaded!");
            Intent intent = new Intent(FriendPicker.this, ImageProcessor.class);

            startActivity(intent);
            Log.v(DEBUG_TAG, "Start ImageProcessor activity");
        }

        @Override
        public void onFail(String error) {
            Log.v(DEBUG_TAG, "Failed to load friend media list");
            Toast.makeText(FriendPicker.this, error, Toast.LENGTH_SHORT).show();
        }
    };
}