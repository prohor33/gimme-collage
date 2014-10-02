package crystal.games.gimmecollage.instagram_api;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class InstagramSession {

    /**
     * Data classes
     */
    public static class UserInfo {
        public String username = null;
        public String bio = null;
        public String website = null;
        public String profile_picture = null;
        public String full_name = null;
        public String id = null;


    }

    public static class ImageInfo {
        ImageInfo() {
            low_resolution = new ImageResolution();
            thumbnail = new ImageResolution();
            standard_resolution = new ImageResolution();
        }

        public static class ImageResolution {
            public String url = null;
            public int width = 0;
            public int height = 0;
        }

        public int likes_count = 0;
        public ImageResolution low_resolution;
        public ImageResolution thumbnail;
        public ImageResolution standard_resolution;
    }

    /**
     * Variables
     */
    private String mAccessToken = null;
    private UserInfo mSelfUserInfo;
    private ArrayList<UserInfo> mSelfFollows;
    private ArrayList<ImageInfo> mImageInfos;

    /**
     * Utility
     */
    private static final String DEBUG_TAG = "InstagramSession";
    private static final String SHARED = "Instagram_Preferences";
    private static final String API_ACCESS_TOKEN = "access_token";
    private static final String API_USERNAME = "username";


    private SharedPreferences sharedPref;
    private Editor editor;

    public InstagramSession(Context context) {
        sharedPref = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        mSelfUserInfo = new UserInfo();
        mSelfFollows = new ArrayList<UserInfo>();
        mImageInfos = new ArrayList<ImageInfo>();
    }

    /**
     * Setters Getters
     */
    // Access token
    public String getAccessToken() {
        return mAccessToken;
    }

    public void setAccessToken(String accessToken) {
        this.mAccessToken = accessToken;
    }

    // SelfUserInfo
    public UserInfo getSelfUserInfo() {
        return mSelfUserInfo;
    }

    public void setSelfUserInfo(UserInfo selfUserInfo) {
        this.mSelfUserInfo = selfUserInfo;
    }

    // SelfFollows
    public List<UserInfo> getSelfFollows() {
        return mSelfFollows;
    }

    // ImageInfoMap
    public List<ImageInfo> getImageInfos() {
        return mImageInfos;
    }

    /**
     * Methods for saving access token between launches ...
     */
    public boolean hasAccessToken() {
        return (mAccessToken != null);
    }

    public void storeAccessToken() {
        editor.putString(API_ACCESS_TOKEN, mAccessToken);
        editor.putString(API_USERNAME, mSelfUserInfo.username);
        editor.commit();
    }

    public void restoreAccessToken() {
        mAccessToken = sharedPref.getString(API_ACCESS_TOKEN, null);
        mSelfUserInfo.username = sharedPref.getString(API_USERNAME, null);
    }

    public void resetAccessToken() {
        mAccessToken = null;
        mSelfUserInfo.username = null;
        storeAccessToken();
    }

}