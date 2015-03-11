package crystal.tech.gimmecollage.instagram_api;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kisame on 15.10.2014.
 */
public class Storage {

    private static final String TAG = "Storage";

    public static class UserInfo {
        public String username;
        public String profile_picture;
        public String full_name;
        public String id;

        public String getName() {
            if(full_name.isEmpty())
                return username;
            else
                return full_name;
        }
    }

    public static class ImageInfo {
        ImageInfo() {
            low_resolution = new ImageResolution();
            thumbnail = new ImageResolution();
            standard_resolution = new ImageResolution();
        }

        public static class ImageResolution {
            public String url;
            public int width;
            public int height;
        }

        public int likes_count;
        public ImageResolution low_resolution;
        public ImageResolution thumbnail;
        public ImageResolution standard_resolution;
    }

    // Data stack.
    public String accessToken;
    public UserInfo selfUserInfo;
    public ArrayList<UserInfo> selfFollows;
    public ArrayList<ImageInfo> imageInfos;

    private static final String SHARED = "Instagram_Preferences";
    private static final String API_ACCESS_TOKEN = "access_token";
    private static final String API_USERNAME = "username";

    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor mEditor;

    Storage(Context context) {
        mSharedPref = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();

        selfUserInfo = new UserInfo();
        selfFollows = new ArrayList<UserInfo>();
        imageInfos = new ArrayList<ImageInfo>();
    }

    public boolean hasAccessToken() {
        return !(accessToken == null);
    }

    public void storeAccessToken() {
        mEditor.putString(API_ACCESS_TOKEN, accessToken);
        mEditor.putString(API_USERNAME, selfUserInfo.username);
        mEditor.commit();
    }

    public void restoreAccessToken() {
        accessToken = mSharedPref.getString(API_ACCESS_TOKEN, null);
        selfUserInfo.username = mSharedPref.getString(API_USERNAME, null);
    }

    public void resetAccessToken() {
        accessToken = null;
        selfUserInfo.username = null;
        storeAccessToken();
    }
}
