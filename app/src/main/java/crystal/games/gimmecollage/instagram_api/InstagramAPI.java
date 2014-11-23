package crystal.games.gimmecollage.instagram_api;

/**
 * Created by Kisame on 14.10.2014.
 */
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;

import java.util.ArrayList;

/**
 * This class designed to solve those problems:
 * authentication by loading webpage on a webview and then catch redirection
 * gathering data by simple update, storing this data within class instance
 */

public class InstagramAPI {

    /**
     * Interface for catching result of operations.
     */
    public interface Listener {
        public abstract void onSuccess();
        public abstract void onFail(String error);
    }

    // Const
    private static final String TAG = "InstagramAPI";
    private static final String API_URL = "https://api.instagram.com/v1/";
    private static final String AUTH_URL = "https://instagram.com/oauth/authorize/";

    private static final int WHAT_ERROR = -1;
    private static final int WHAT_FINALIZE = 0;

    // Support classes
    private static InstagramAPI mSingleton;
    private Listener mListener;
    private Authenticator mAuthenticator;
    private Storage mStorage;
    private Loader mLoader;
    private Parser mParser;

    // External data
    private Context mContext;
    private String mClientId;
    private String mClientSecret;
    private String mRedirectUrl;

    // Public static methods.
    public static void init(Context context, String clientId, String clientSecret, String redirectUrl) {
        mSingleton = new InstagramAPI(context, clientId, clientSecret, redirectUrl);
    }

    public static boolean initialized() {
        if (mSingleton == null) {
            Log.e(TAG, "To use InstagramAPI, must call init() method at least once.");
            return false;
        }
        return true;
    }

    public static InstagramAPI with(Listener listener) {
        mSingleton.attachListener(listener);
        return mSingleton;
    }

    public static InstagramAPI with() {
        return with(null);
    }

    public static String getTag() {
        return TAG;
    }

    // Check if there is auth.
    public static boolean isAuthenticated() {
        return mSingleton.mStorage.hasAccessToken();
    }

    public static void resetAuthentication() {
        mSingleton.mStorage.resetAccessToken();
        mSingleton.mStorage.selfFollows.clear();
    }

    // Methods for getting data.
    public static Storage.UserInfo getSelf() {
        return mSingleton.mStorage.selfUserInfo;
    }

    public static ArrayList<Storage.UserInfo> getFollows() {
        return mSingleton.mStorage.selfFollows;
    }

    public static ArrayList<Storage.ImageInfo> getImages() {
        return mSingleton.mStorage.imageInfos;
    }

    public static void updateFollows() {
        mSingleton.updateFollowsImpl();
    }

    // Public methods.
    // Need method that will handle webview and capture redirect.
    public void startAuthentication(WebView webView) {
        Authenticator.Listener authListener = new Authenticator.Listener() {
            @Override
            public void onComplete(String result) {
                mStorage.accessToken = result;
                updateSelf();
                mStorage.storeAccessToken();
                mListener.onSuccess();
            }

            @Override
            public void onError(String error) {
                mListener.onFail(error);
            }
        };
        mAuthenticator.loadPage(webView, authListener);
    }

    // Methods for updating data.
    public void updateSelf() {
        Log.d(TAG, "Updating self user info ...");
        new Thread() {
            @Override
            public void run() {
                int what = WHAT_FINALIZE;
                try {
                    String answer = mLoader.fetchUserInfo(mStorage.accessToken, "self");
                    mStorage.selfUserInfo = mParser.parseUserInfo(answer);
                } catch (Exception ex) {
                    what = WHAT_ERROR;
                    ex.printStackTrace();
                }
                mHandler.sendMessage(mHandler.obtainMessage(what, 0, 0, mListener));
            }
        }.start();
    }

    public void updateFollowsImpl() {
        Log.d(TAG, "Updating self follows list ...");
        new Thread() {
            @Override
            public void run() {
                int what = WHAT_FINALIZE;
                try {
                    String answer = mLoader.fetchFollows(mStorage.accessToken, "self");
                    mParser.parseUserInfoList(mStorage.selfFollows, answer);
                } catch (Exception ex) {
                    what = WHAT_ERROR;
                    ex.printStackTrace();
                }
                mHandler.sendMessage(mHandler.obtainMessage(what, 0, 0, mListener));
            }
        }.start();
    }

    public void updateImages(final String userId) {
        Log.d(TAG, "Updating userId = " + userId + " images info ...");
        new Thread() {
            @Override
            public void run() {
                int what = WHAT_FINALIZE;
                try {
                    String answer = mLoader.fetchImages(mStorage.accessToken, userId);
                    mParser.parseImageInfoList(mStorage.imageInfos, answer);
                } catch (Exception ex) {
                    what = WHAT_ERROR;
                    ex.printStackTrace();
                }
                mHandler.sendMessage(mHandler.obtainMessage(what, 0, 0, mListener));
            }
        }.start();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Listener listener = (Listener) msg.obj;
            if (msg.what == WHAT_ERROR) {
                listener.onFail("InstagramAPI error: " + msg.toString());
            } else {
                listener.onSuccess();
            }
        }
    };
    /**
     *
     */


    // Private section.
    private InstagramAPI(Context context, String clientId, String clientSecret, String redirectUrl) {
        mContext = context;
        mClientId = clientId;
        mClientSecret = clientSecret;
        mRedirectUrl = redirectUrl;

        mListener = mDefaultListener;

        mAuthenticator = new Authenticator(AUTH_URL, mClientId, mRedirectUrl);

        mStorage = new Storage(mContext);
        mStorage.restoreAccessToken();

        mLoader = new Loader(API_URL);

        mParser = new Parser();
    }

    // This func must control that mListener is not null.
    private void attachListener(Listener listener) {
        if (listener == null) {
            mListener = mDefaultListener;
        } else {
            mListener = listener;
        }
    }

    // Default Listener
    private Listener mDefaultListener = new Listener() {
        @Override
        public void onSuccess() {}

        @Override
        public void onFail(String error) {}
    };

}
