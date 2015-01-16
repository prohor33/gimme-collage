package crystal.tech.gimmecollage.lenta_api;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

/**
 * Created by prohor on 14/01/15.
 */
public class Storage {

    private static final String TAG = "LentaStorage";

    public static class ImageInfo {
        public String url;
        public int width;
        public int height;
    }
    public static class PostInfo {
        PostInfo() {
            image = new ImageInfo();
            image_preview = new ImageInfo();
        }
        public String id;
        public String nickname;
        public ImageInfo image;
        public ImageInfo image_preview;
        public String text;
    }

    // Data stack.
    public ArrayList<PostInfo> postsInfo = new ArrayList<>(10);

    private static final String SHARED = "LentaPreferences";

    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor mEditor;

    Storage(Context context) {
        mSharedPref = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();
    }
}
