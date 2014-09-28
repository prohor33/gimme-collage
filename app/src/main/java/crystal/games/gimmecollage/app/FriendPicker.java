package crystal.games.gimmecollage.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import crystal.games.gimmecollage.instagram_api.InstagramApp;
import crystal.games.gimmecollage.app.MainActivity;

/**
 * Created by prohor on 28/09/14.
 */
public class FriendPicker extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getIntent().getExtras();
        String[] friends_arrays = bundle.getStringArray("friends_array");

        ListView list = new ListView(this);
        list.setAdapter(new MyAdapter(this, friends_arrays));

        setContentView(list);
    }

    private class MyAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public MyAdapter(Context context, String[] strings) {
            super(context, -1, strings);
            for (int i = 0; i < strings.length; ++i) {
                mIdMap.put(strings[i], i);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LinearLayout listLayout = new LinearLayout(FriendPicker.this);
            listLayout.setLayoutParams(new AbsListView.LayoutParams(
                    AbsListView.LayoutParams.WRAP_CONTENT,
                    AbsListView.LayoutParams.WRAP_CONTENT));
            listLayout.setId(5000);

            TextView listText = new TextView(FriendPicker.this);
            listText.setId(5001);

            listLayout.addView(listText);

            listText.setText(super.getItem(position));

            return listLayout;
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}