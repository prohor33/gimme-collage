package crystal.tech.gimmecollage.utility;

import android.app.ProgressDialog;
import android.os.AsyncTask;

/**
 * Created by Dmitry on 10.02.2015.
 */
public class SimpleAsyncTask extends AsyncTask<Void, Void, Void> {

    private SimpleAsyncListener mSimpleAsyncListener;
    private String mErrorString;
    private boolean mErrorMet;
    private ProgressDialog mProgressDialog;

    public SimpleAsyncTask(SimpleAsyncListener simpleAsyncListener) {
        super();
        mSimpleAsyncListener = simpleAsyncListener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        mSimpleAsyncListener.doInBackground();
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mErrorMet = false;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(!mErrorMet) {
            mSimpleAsyncListener.onSuccess();
        } else {
            mSimpleAsyncListener.onError(mErrorString);
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
