package crystal.tech.gimmecollage.lenta_api;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by prohor on 14/01/15.
 */
public class LentaAPI {

    /**
     * Interface for catching result of operations.
     */
    public interface Listener {
        public abstract void onSuccess();
        public abstract void onFail(String error);
    }

    private static final String TAG = "LentaAPI";
    private static final String API_URL = "https://just-learning.herokuapp.com/gc_posts/";

    private static final int WHAT_ERROR = -1;
    private static final int WHAT_FINALIZE = 0;

    private static LentaAPI mSingleton;
    private Listener mListener;
    private Storage mStorage;
    private Loader mLoader;
    private Context mContext;
    private Parser mParser;

    // Public static methods.
    public static void init(Context context) {
        mSingleton = new LentaAPI(context);
    }

    public static boolean initialized() {
        if (mSingleton == null) {
            Log.e(TAG, "To use LentaAPI, must call init() method at least once.");
            return false;
        }
        return true;
    }

    public static LentaAPI with(Listener listener) {
        mSingleton.attachListener(listener);
        return mSingleton;
    }

    public static LentaAPI with() {
        return with(null);
    }

    // Methods for updating data.
    public void updatePosts() {
        Log.d(TAG, "Updating posts ...");
        new Thread() {
            @Override
            public void run() {
                int what = WHAT_FINALIZE;
                try {
                    String answer = mLoader.fetchPosts();
                    mParser.parsePostsList(mStorage.postsInfo, answer);
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
                listener.onFail("LentaAPI error: " + msg.toString());
            } else {
                listener.onSuccess();
            }
        }
    };


    private LentaAPI(Context context) {
        mContext = context;

        mListener = mDefaultListener;

        mStorage = new Storage(mContext);

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
