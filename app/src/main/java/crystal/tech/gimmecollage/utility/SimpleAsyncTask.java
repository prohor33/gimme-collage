package crystal.tech.gimmecollage.utility;

import android.os.AsyncTask;

/**
 * Created by Dmitry on 10.02.2015.
 */
public class SimpleAsyncTask extends AsyncTask<Void, Void, Void> {
    public interface Listener {
        public abstract void onSuccess();
        public abstract void onFail();
        public abstract void doInBackground();
    }

    private Listener mListener;
    private boolean mError;

    public SimpleAsyncTask(Listener listener) {
        super();
        mListener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            mListener.doInBackground();
        } catch (Exception e) {
            mError = true;
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mError = false;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(!mError) {
            mListener.onSuccess();
        } else {
            mListener.onFail();
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
