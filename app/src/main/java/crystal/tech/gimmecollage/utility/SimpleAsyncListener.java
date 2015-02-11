package crystal.tech.gimmecollage.utility;

/**
 * Created by kisame on 10.02.15.
 */
public interface SimpleAsyncListener {
    void onSuccess();
    void onError(String error);
    void doInBackground();
}
