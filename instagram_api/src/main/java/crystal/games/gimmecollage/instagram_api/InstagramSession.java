package crystal.games.gimmecollage.instagram_api;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.Context;
import android.util.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manage access token and user name. Uses shared preferences to store access
 * token and user name.
 *
 * @author Thiago Locatelli <thiago.locatelli@gmail.com>
 * @author Lorensius W. L T <lorenz@londatiga.net>
 *
 */
public class InstagramSession {

    private SharedPreferences sharedPref;
    private Editor editor;

    private static final String TAG = "InstagramSession";

    private static final String SHARED = "Instagram_Preferences";
    private static final String API_USERNAME = "username";
    private static final String API_ID = "id";
    private static final String API_NAME = "name";
    private static final String API_ACCESS_TOKEN = "access_token";
    private static final String API_FRIENDS_SET = "friends_set";

    public InstagramSession(Context context) {
        sharedPref = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    /**
     *
     * @param accessToken
     * @param expireToken
     * @param expiresIn
     * @param username
     */
    public void storeAccessToken(String accessToken, String id, String username, String name) {
        editor.putString(API_ID, id);
        editor.putString(API_NAME, name);
        editor.putString(API_ACCESS_TOKEN, accessToken);
        editor.putString(API_USERNAME, username);
        editor.commit();
    }

    public void storeAccessToken(String accessToken) {
        editor.putString(API_ACCESS_TOKEN, accessToken);
        editor.commit();
    }

    public void storeFriendsList(List<String> friendsList) {
        Set<String> str_set = new HashSet();
        for (String it : friendsList) {
            str_set.add(it);
        }
        // TODO: putStringSet() is only under 11 SDK version!
//        editor.putStringSet(API_FRIENDS_SET, str_set);
        editor.commit();
    }

    /**
     * Reset access token and user name
     */
    public void resetAccessToken() {
        editor.putString(API_ID, null);
        editor.putString(API_NAME, null);
        editor.putString(API_ACCESS_TOKEN, null);
        editor.putString(API_USERNAME, null);
        editor.commit();
    }

    /**
     * Get user name
     *
     * @return User name
     */
    public String getUsername() {
        return sharedPref.getString(API_USERNAME, null);
    }

    /**
     *
     * @return
     */
    public String getId() {
        return sharedPref.getString(API_ID, null);
    }

    /**
     *
     * @return
     */
    public String getName() {
        return sharedPref.getString(API_NAME, null);
    }

    /**
     * Get access token
     *
     * @return Access token
     */
    public String getAccessToken() {
        return sharedPref.getString(API_ACCESS_TOKEN, null);
    }

    public String[] getFriendsList() {
        Set<String> str_set = new HashSet();
        // TODO: putStringSet() is only under 11 SDK version!
//        sharedPref.getStringSet(API_FRIENDS_SET, str_set);
        String[] str_arr = new String[str_set.size()];
        int i = 0;
        for (String item : str_set) {
            str_arr[i] = item;
            i++;
        }
        Log.v(TAG, "getFriendsList, size = " + str_arr.length);
        return str_arr;
    }
}