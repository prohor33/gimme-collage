package crystal.tech.gimmecollage.collagemaker;

import android.util.Log;

/**
 * Created by prohor on 26/01/15.
 */
public class CollageUtils {

    private final String TAG = "CollageUtils";

    private static CollageUtils instance;
    private boolean fabCollapsed = true;

    public static synchronized CollageUtils getInstance() {
        if (instance == null) {
            instance = new CollageUtils();
        }
        return instance;
    }

    public void saveCollageOnDisk() {
        Log.w(TAG, "saveCollageOnDisk() not implemented yet");
    }

    public void shareCollage() {
        Log.w(TAG, "shareCollage() not implemented yet");
    }

    public static void putFabCollapsed(boolean x) {
        getInstance().fabCollapsed = x;
    }
    public static boolean getFabCollapsed() {
        return  getInstance().fabCollapsed;
    }
}
