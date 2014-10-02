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

import java.util.List;

import crystal.games.gimmecollage.instagram_api.InstagramApp;
import crystal.games.gimmecollage.instagram_api.InstagramSession;

/**
 * Created by prohor on 28/09/14.
 */

public class FriendPicker extends Activity {

    private static final String TAG = "FriendPicker";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<InstagramSession.UserInfo> userInfos = InstagramApp.getInstance().getSession().getSelfFollows();
        String[] friends_names_array = new String[userInfos.size()];
        for(int i = 0;i < userInfos.size();i++) {
            friends_names_array[i] = userInfos.get(i).full_name;
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
        List<InstagramSession.UserInfo> userInfos = InstagramApp.getInstance().getSession().getSelfFollows();
        if (pos < 0 || pos >= userInfos.size()) {
            Log.v(TAG, "Error pickFriend(): index is out of range");
            return;
        }
        Log.v(TAG, "pick friend:" + userInfos.get(pos).username);

        InstagramApp.getInstance().updateImageInfo(userInfos.get(pos).id, images_list_load_listener);
    }

    InstagramApp.APIRequestListener images_list_load_listener = new InstagramApp.APIRequestListener() {

        @Override
        public void onSuccess() {
            Log.v(TAG, "Friend media list successfully loaded!");
            Intent intent = new Intent(FriendPicker.this, ImageProcessor.class);
            Bundle bundle = new Bundle();

            List<InstagramSession.ImageInfo> imageInfos =
                    InstagramApp.getInstance().getSession().getImageInfos();
            String[] images_array = new String[imageInfos.size()];
            int[] image_like_count_array = new int[imageInfos.size()];

            for (int i = 0;i < imageInfos.size();i++) {
                images_array[i] = imageInfos.get(i).standard_resolution.url;
                image_like_count_array[i] = imageInfos.get(i).likes_count;
            }

            bundle.putStringArray("images_array", images_array);
            bundle.putIntArray("image_like_count_array", image_like_count_array);

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