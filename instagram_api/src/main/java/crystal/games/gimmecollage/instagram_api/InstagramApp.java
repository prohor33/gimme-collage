package crystal.games.gimmecollage.instagram_api;

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
    private InstagramConnect mConnect;

    private String mAuthUrl;

    private Context mContext;
    private Context mDefaultContext;
    private String mClientId;
    private String mClientSecret;
    public String mCallbackUrl;

    private static int WHAT_ERROR = -1;
    private static int WHAT_FINALIZE = 0;

    /**
     * .....
     */
    private static final String DEBUG_TAG = "InstagramApp";
    private static final String AUTH_URL = "https://instagram.com/oauth/authorize/";
    private static final String API_URL = "https://api.instagram.com/v1/";

    public void Init(Context defaultContext, String clientId, String clientSecret,
                          String callbackUrl) {

        mDefaultContext = defaultContext;
        mClientId = clientId;
        mClientSecret = clientSecret;
        mCallbackUrl = callbackUrl;

        mAuthUrl = AUTH_URL + "?client_id=" + clientId +
                "&redirect_uri=" + mCallbackUrl + "&response_type=token";
        // + "&scope=likes+comments+relationships"

        mContext = null;

        mSession = new InstagramSession(mDefaultContext);
        mSession.restoreAccessToken();

        mConnect = new InstagramConnect(API_URL);
    }

    public void setCurrentContext(Context currentContext) {
        mContext = currentContext;
    }

    public InstagramSession getSession() {
        return mSession;
    }

    public Context getContext() {
        return ((mContext != null) ? mContext : mDefaultContext);
    }

    public void updateSelfUserInfo(final APIRequestListener listener) {
        Log.d(DEBUG_TAG, "Updating self user info ...");
        new Thread() {
            @Override
            public void run() {
                int what = WHAT_FINALIZE;
                try {
                    mSession.setSelfUserInfo(mConnect.fetchUserInfo("self",
                            mSession.getAccessToken()));
                } catch (Exception ex) {
                    what = WHAT_ERROR;
                    ex.printStackTrace();
                }
                mHandler.sendMessage(mHandler.obtainMessage(what, 0, 0, listener));
            }
        }.start();
        Log.d(DEBUG_TAG, "Updating self user info ...");
    }

    public void updateSelfFollows(final APIRequestListener listener) {
        Log.d(DEBUG_TAG, "Updating self follows list ...");
        new Thread() {
            @Override
            public void run() {
                int what = WHAT_FINALIZE;
                try {
                    mConnect.fetchFollows(mSession.getSelfFollows(), "self",
                            mSession.getAccessToken());
                } catch (Exception ex) {
                    what = WHAT_ERROR;
                    ex.printStackTrace();
                }
                mHandler.sendMessage(mHandler.obtainMessage(what, 0, 0, listener));
            }
        }.start();
    }

    public void updateImageInfo(final String userId, final APIRequestListener listener) {
        Log.d(DEBUG_TAG, "Updating userId = " + userId + " images info ...");
        new Thread() {
            @Override
            public void run() {
                int what = WHAT_FINALIZE;
                try {
                    mConnect.fetchImageInfo(mSession.getImageInfos(), userId,
                            mSession.getAccessToken());
                } catch (Exception ex) {
                    what = WHAT_ERROR;
                    ex.printStackTrace();
                }
                mHandler.sendMessage(mHandler.obtainMessage(what, 0, 0, listener));
            }
        }.start();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            APIRequestListener listener = (APIRequestListener) msg.obj;
            if (msg.what == WHAT_ERROR) {
                listener.onFail("Error (TODO: SPECIFY)");
            } else {
                listener.onSuccess();
            }
        }
    };

    public void authorize(final APIRequestListener listener) {
        final APIRequestListener userInfoListener = new APIRequestListener() {
            @Override
            public void onSuccess() {
                mSession.storeAccessToken();
                listener.onSuccess();
            }

            @Override
            public void onFail(String error) {
                listener.onFail(error);
            }
        };

        OAuthDialogListener dialogListener = new OAuthDialogListener() {
            @Override
            public void onComplete(String accessToken) {
                mSession.setAccessToken(accessToken);
                updateSelfUserInfo(userInfoListener);
            }

            @Override
            public void onError(String error) {
                listener.onFail(error);
            }
        };

        new InstagramDialog(getContext(), mAuthUrl, dialogListener).show();
    }

    public interface APIRequestListener {
        public abstract void onSuccess();
        public abstract void onFail(String error);
    }
}