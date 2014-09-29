package crystal.games.gimmecollage.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import crystal.games.gimmecollage.instagram_api.InstagramApp;

/**
 * Created by prohor on 28/09/14.
 */
public class FriendPicker extends Activity {

    private static final String TAG = "FriendPicker";

    private List<FriendData> m_lFriendsData = new ArrayList<FriendData>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getIntent().getExtras();
        String[] friends_names_array = bundle.getStringArray("friends_names_array");
        String[] friends_ids_array = bundle.getStringArray("friends_ids_array");
        if (friends_names_array.length != friends_ids_array.length) {
            Log.v(TAG, "Error onCreate: different arrays length");
            return;
        }
        m_lFriendsData.clear();
        for (int i = 0; i < friends_names_array.length; ++i) {
            m_lFriendsData.add(new FriendData(friends_names_array[i], friends_ids_array[i]));
        }

        ListView list = new ListView(this);
        list.setAdapter(new MyAdapter(this, friends_names_array));
        list.setClickable(true);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View view,
                                    int position, long id) {

                Log.v(TAG, "click " + position);
                pickFriend(position);
            }
        });

        setContentView(list);
    }

    private class MyAdapter extends ArrayAdapter<String> {

        public MyAdapter(Context context, String[] strings) {
            super(context, -1, strings);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LinearLayout listLayout = new LinearLayout(FriendPicker.this);
            listLayout.setLayoutParams(new AbsListView.LayoutParams(
                    AbsListView.LayoutParams.MATCH_PARENT,
                    AbsListView.LayoutParams.WRAP_CONTENT));
            listLayout.setId(5000);

            TextView listText = new TextView(FriendPicker.this);
            listText.setId(5001);
            listText.setTextSize(24);

            listLayout.addView(listText);

            listText.setText(super.getItem(position));

            return listLayout;
        }
    }

    private void pickFriend(int pos) {
        if (pos < 0 || pos >= m_lFriendsData.size()) {
            Log.v(TAG, "Error pickFriend(): index is out of range");
            return;
        }
        Log.v(TAG, "pick friend:" + m_lFriendsData.get(pos).m_strUsername);

        InstagramApp.getInstance().setListener(images_list_load_listener);
        InstagramApp.getInstance().fetchUserMedia(m_lFriendsData.get(pos).m_strId);
    }

    InstagramApp.APIRequestListener images_list_load_listener = new InstagramApp.APIRequestListener() {

        @Override
        public void onSuccess() {
            Log.v(TAG, "Friend media list successfully loaded!");
            Intent intent = new Intent(FriendPicker.this, ImageProcessor.class);
            Bundle bundle = new Bundle();
            bundle.putStringArray("images_array", InstagramApp.getInstance().getFriendImages());
            bundle.putIntArray("image_like_count_array", InstagramApp.getInstance().getImagesLikeCount());
            intent.putExtras(bundle);

            startActivity(intent);
            Log.v(TAG, "Start ImageProcessor activity");
        }

        @Override
        public void onFail(String error) {
            Log.v(TAG, "Failed to load friend media list");
            Toast.makeText(FriendPicker.this, error, Toast.LENGTH_SHORT).show();
        }
    };
}

class FriendData {
    FriendData(String username, String id) {
        m_strUsername = username;
        m_strId = id;
    }
    String m_strUsername;
    String m_strProfilePicture;
    String m_strId;
}