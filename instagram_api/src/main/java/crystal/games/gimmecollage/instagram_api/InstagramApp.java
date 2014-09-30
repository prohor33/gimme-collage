package crystal.games.gimmecollage.instagram_api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import crystal.games.gimmecollage.instagram_api.InstagramDialog.OAuthDialogListener;

public class InstagramApp {

    // Singletone pattern
    private static InstagramApp m_pInstance;
    public static synchronized InstagramApp getInstance() {
        if (m_pInstance == null) {
            m_pInstance = new InstagramApp();
        }
        return m_pInstance;
    }
    protected InstagramApp() {};

    private InstagramSession mSession;
    private InstagramDialog mDialog;
    private APIRequestListener mListener;
    private ProgressDialog mProgress;
    private String mAuthUrl;
    private String mTokenUrl;
    private String mAccessToken;
    private Context mCtx;

    private String mClientId;
    private String mClientSecret;

    private static int WHAT_FINALIZE = 0;
    private static int WHAT_ERROR = -1;
    private static int WHAT_FETCH_INFO = 1;
    private static int WHAT_FETCH_USER = 2;
    private static int WHAT_FETCHING_FRIENDS = 3;
    private static int WHAT_FETCHING_FRIEND_IMAGES = 4;

    private List<String> m_vFriendList = new ArrayList<String>();
    private List<String> m_vFriendsProfilePics = new ArrayList<String>();
    private List<String> m_vFriendsIds = new ArrayList<String>();

    private List<String> m_vFriendImages = new ArrayList<String>();
    private List<Integer> m_vImageLikeCount = new ArrayList<Integer>();

    /**
     * Callback url, as set in 'Manage OAuth Costumers' page
     * (https://developer.github.com/)
     */

    public static String mCallbackUrl = "";
    private static final String AUTH_URL = "https://api.instagram.com/oauth/authorize/";
    private static final String TOKEN_URL = "https://api.instagram.com/oauth/access_token";
    private static final String API_URL = "https://api.instagram.com/v1";

    private static final String TAG = "InstagramAPI";

    public void Init(Context context, String clientId, String clientSecret,
                        String callbackUrl) {

        mClientId = clientId;
        mClientSecret = clientSecret;
        mCtx = context;
        mSession = new InstagramSession(context);
        mAccessToken = mSession.getAccessToken();
        mCallbackUrl = callbackUrl;
        mTokenUrl = TOKEN_URL + "?client_id=" + clientId + "&client_secret="
                + clientSecret + "&redirect_uri=" + mCallbackUrl + "&grant_type=authorization_code";
        mAuthUrl = AUTH_URL + "?client_id=" + clientId + "&redirect_uri="
                + mCallbackUrl + "&response_type=code&display=touch&scope=likes+comments+relationships";

        OAuthDialogListener listener = new OAuthDialogListener() {
            @Override
            public void onComplete(String code) {
                getAccessToken(code);
            }

            @Override
            public void onError(String error) {
                mListener.onFail("Authorization failed");
            }
        };

        mDialog = new InstagramDialog(context, mAuthUrl, listener);
        mProgress = new ProgressDialog(context);
        mProgress.setCancelable(false);
    }

    private void getAccessToken(final String code) {
        mProgress.setMessage("Getting access token ...");
        mProgress.show();

        new Thread() {
            @Override
            public void run() {
                Log.i(TAG, "Getting access token");
                int what = WHAT_FETCH_INFO;
                InputStream is = null;
                try {
                    URL url = new URL(TOKEN_URL);
                    //URL url = new URL(mTokenUrl + "&code=" + code);
                    Log.i(TAG, "Opening Token URL " + url.toString());
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);
                    //urlConnection.connect();
                    OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                    writer.write("client_id="+mClientId+
                            "&client_secret="+mClientSecret+
                            "&grant_type=authorization_code" +
                            "&redirect_uri="+mCallbackUrl+
                            "&code=" + code);
                    writer.flush();
                    int response = urlConnection.getResponseCode();
                    Log.d(TAG, "The response is: " + response);
                    is = urlConnection.getInputStream();

                    // Convert the InputStream into a string
                    String contentAsString = streamToString(is);
                    Log.i(TAG, "response " + contentAsString);
                    JSONObject jsonObj = (JSONObject) new JSONTokener(contentAsString).nextValue();

                    mAccessToken = jsonObj.getString("access_token");
                    Log.i(TAG, "Got access token: " + mAccessToken);

                    String id = jsonObj.getJSONObject("user").getString("id");
                    String user = jsonObj.getJSONObject("user").getString("username");
                    String name = jsonObj.getJSONObject("user").getString("full_name");

                    mSession.storeAccessToken(mAccessToken, id, user, name);

                } catch (Exception ex) {
                    what = WHAT_ERROR;
                    ex.printStackTrace();
                }

                mHandler.sendMessage(mHandler.obtainMessage(what, 1, 0));
            }
        }.start();
    }

    private void fetchUserName() {
        mProgress.setMessage("Fetching user info...");

        new Thread() {
            @Override
            public void run() {
                Log.i(TAG, "Fetching user info");
                int what = WHAT_FETCH_USER;
                InputStream is = null;
                try {
                    URL url = new URL(API_URL + "/users/" + mSession.getId() + "/?access_token=" + mAccessToken);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    // Starts the query
                    conn.connect();
                    int response = conn.getResponseCode();
                    Log.d(TAG, "The response is: " + response);
                    is = conn.getInputStream();

                    // Convert the InputStream into a string
                    String contentAsString = streamToString(is);
                    //return contentAsString;
                    System.out.println(contentAsString);
                    JSONObject jsonObj = (JSONObject) new JSONTokener(contentAsString).nextValue();
                    String name = jsonObj.getJSONObject("data").getString("full_name");
                    String bio = jsonObj.getJSONObject("data").getString("bio");
                    Log.i(TAG, "Got name: " + name + ", bio [" + bio + "]");
                } catch (Exception ex) {
                    what = WHAT_ERROR;
                    ex.printStackTrace();
                }

                mHandler.sendMessage(mHandler.obtainMessage(what, 2, 0));
            }
        }.start();

    }

    public void fetchFriends() {
        mProgress.setMessage("Fetching friends...");
        mProgress.show();

        new Thread() {
            @Override
            public void run() {
                Log.i(TAG, "Fetching friends");
                int what = WHAT_FETCHING_FRIENDS;
                InputStream is = null;
                try {
                    URL url = new URL(API_URL + "/users/" + mSession.getId() + "/follows" + "/?access_token=" + mAccessToken);
                    Log.d(TAG, "Opening URL " + url.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    // Starts the query
                    conn.connect();
                    int response = conn.getResponseCode();
                    Log.d(TAG, "The response is: " + response);
                    is = conn.getInputStream();

                    // Convert the InputStream into a string
                    String contentAsString = streamToString(is);
                    //return contentAsString;
                    System.out.println(contentAsString);
                    JSONObject jsonObj = (JSONObject) new JSONTokener(contentAsString).nextValue();
                    m_vFriendList.clear();
                    m_vFriendsProfilePics.clear();
                    m_vFriendsIds.clear();
                    JSONArray friends_data = jsonObj.getJSONArray("data");
                    for (int i = 0; i < friends_data.length(); i++) {
                        JSONObject jsonUser = friends_data.getJSONObject(i);
                        String strFullName = jsonUser.getString("full_name");
                        if (strFullName.isEmpty())
                            strFullName = jsonUser.getString("username");
                        String strProfilePic = jsonUser.getString("profile_picture");
                        String strId = jsonUser.getString("id");
                        m_vFriendList.add(strFullName);
                        m_vFriendsProfilePics.add(strProfilePic);
                        m_vFriendsIds.add(strId);
                        Log.v(TAG, "Follow: " + strFullName + ", ProfilePic [" + strProfilePic + "]");
                    }
                } catch (Exception ex) {
                    what = WHAT_ERROR;
                    ex.printStackTrace();
                }

                mHandler.sendMessage(mHandler.obtainMessage(what, 3, 0));
            }
        }.start();

    }

    public void fetchUserMedia(final String strUserId) {
        mProgress.setMessage("Fetching userid=" + strUserId + " media...");
        mProgress.show();

        new Thread() {
            @Override
            public void run() {
                Log.i(TAG, "Fetching userid=" + strUserId + " media...");
                int what = WHAT_FETCHING_FRIEND_IMAGES;
                InputStream is = null;
                try {
                    int media_count_to_load = 100;
                    URL url = new URL(API_URL + "/users/" + strUserId + "/media/recent?count=" +
                            media_count_to_load + "&access_token=" + mAccessToken);
                    Log.d(TAG, "Opening URL " + url.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    // Starts the query
                    conn.connect();
                    int response = conn.getResponseCode();
                    Log.d(TAG, "The response is: " + response);
                    is = conn.getInputStream();

                    // Convert the InputStream into a string
                    String contentAsString = streamToString(is);
                    System.out.println(contentAsString);
                    JSONObject jsonObj = (JSONObject) new JSONTokener(contentAsString).nextValue();
                    m_vFriendImages.clear();
                    m_vImageLikeCount.clear();
                    JSONArray images_data = jsonObj.getJSONArray("data");
                    for (int i = 0; i < images_data.length(); i++) {
                        JSONObject jsonMedia = images_data.getJSONObject(i);
                        // fetching images url
                        JSONObject jsonImages = jsonMedia.getJSONObject("images");
                        if (jsonImages.isNull("standard_resolution"))
                            continue;
                        JSONObject jsonStandartRes = jsonImages.getJSONObject("standard_resolution");
                        String strImage = jsonStandartRes.getString("url");

                        // get likes count
                        JSONObject jsonLikes = jsonMedia.getJSONObject("likes");
                        int iLikesCount = jsonLikes.getInt("count");
                        Log.v(TAG, "Image: " + strImage + ", Likes = " + iLikesCount);

                        m_vFriendImages.add(strImage);
                        m_vImageLikeCount.add(iLikesCount);
                    }
                } catch (Exception ex) {
                    what = WHAT_ERROR;
                    ex.printStackTrace();
                }

                mHandler.sendMessage(mHandler.obtainMessage(what, 4, 0));
            }
        }.start();

    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_ERROR) {
                mProgress.dismiss();
                if(msg.arg1 == 1) {
                    mListener.onFail("Failed to get access token");
                } else if(msg.arg1 == 2) {
                    mListener.onFail("Failed to get user information");
                } else if(msg.arg1 == 3) {
                    mListener.onFail("Failed to get friends");
                } else if(msg.arg1 == 4) {
                    mListener.onFail("Failed to get friend image");
                }
            } else if(msg.what == WHAT_FETCH_INFO) {
                fetchUserName();
            } else if (msg.what == WHAT_FETCH_USER) {
                // finalize
                mHandler.sendMessage(mHandler.obtainMessage(WHAT_FINALIZE, 0, 0));
            } else if (msg.what == WHAT_FETCHING_FRIENDS) {
                // finalize
                mHandler.sendMessage(mHandler.obtainMessage(WHAT_FINALIZE, 0, 0));
            } else if (msg.what == WHAT_FETCHING_FRIEND_IMAGES) {
                // finalize
                mHandler.sendMessage(mHandler.obtainMessage(WHAT_FINALIZE, 0, 0));
            } else {
                mProgress.dismiss();
                mListener.onSuccess();
            }
        }
    };

    public boolean hasAccessToken() {
        return (mAccessToken == null) ? false : true;
    }

    public void setListener(APIRequestListener listener) {
        mListener = listener;
    }

    public String getUserName() {
        return mSession.getUsername();
    }

    public String getId() {
        return mSession.getId();
    }

    public String getName() {
        return mSession.getName();
    }

    public void authorize() {
//		Intent webAuthIntent = new Intent(Intent.ACTION_VIEW);
//        webAuthIntent.setData(Uri.parse(AUTH_URL));
//        mCtx.startActivity(webAuthIntent);
        mDialog.show();
    }

    private String streamToString(InputStream is) throws IOException {
        String str = "";

        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                reader.close();
            } finally {
                is.close();
            }

            str = sb.toString();
        }

        return str;
    }

    public void resetAccessToken() {
        if (mAccessToken != null) {
            mSession.resetAccessToken();
            mAccessToken = null;
        }
    }

    public String[] getFriendsNames() {
        String[] str_arr = new String[m_vFriendList.size()];
        int i = 0;
        for (String item : m_vFriendList) {
            str_arr[i] = item;
            i++;
        }
        Log.v(TAG, "getFriendsNames, size = " + str_arr.length);
        return str_arr;
    }

    public String[] getFriendsIds() {
        String[] str_arr = new String[m_vFriendsIds.size()];
        int i = 0;
        for (String item : m_vFriendsIds) {
            str_arr[i] = item;
            i++;
        }
        Log.v(TAG, "getFriendsIds, size = " + str_arr.length);
        return str_arr;
    }

    public String[] getFriendImages() {
        String[] str_arr = new String[m_vFriendImages.size()];
        int i = 0;
        for (String item : m_vFriendImages) {
            str_arr[i] = item;
            i++;
        }
        Log.v(TAG, "getFriendImages, size = " + str_arr.length);
        return str_arr;
    }

    public int[] getImagesLikeCount() {
        int[] arr = new int[m_vImageLikeCount.size()];
        int i = 0;
        for (int item : m_vImageLikeCount) {
            arr[i] = item;
            i++;
        }
        Log.v(TAG, "getImagesLikeCount, size = " + arr.length);
        return arr;
    }

    public interface APIRequestListener {
        public abstract void onSuccess();

        public abstract void onFail(String error);
    }
}