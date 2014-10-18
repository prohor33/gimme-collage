package crystal.games.gimmecollage.instagram_api;

import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by kisame on 15.10.2014.
 */
class Authenticator {
    private static final String TAG = "Authenticator";

    /**
     * Interface for catching result of operations.
     */
    public interface Listener {
        public abstract void onComplete(String result);
        public abstract void onError(String error);
    }

    private String mAuthUrl;
    private String mRedirectUrl;
    private Listener mListener;

    public Authenticator(String authUrl, String clientId, String redirectUrl) {
        mAuthUrl = authUrl + "?client_id=" + clientId + "&redirect_uri=" + redirectUrl + "&response_type=token";
        mRedirectUrl = redirectUrl;
    }

    public void loadPage(WebView webView, Listener listener) {
        mListener = listener;
        webView.setWebViewClient(new OAuthWebViewClient());
        webView.loadUrl(mAuthUrl);
    }

    private class OAuthWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(InstagramAPI.getTag() + "." + TAG, "Redirecting URL " + url);

            if (url.startsWith(mRedirectUrl)) {
                String urls[] = url.split("=");
                mListener.onComplete(urls[1]);
                return true;
            }
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            Log.d(InstagramAPI.getTag() + "." + TAG, "Page error: " + description);

            super.onReceivedError(view, errorCode, description, failingUrl);
            mListener.onError(description);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(InstagramAPI.getTag() + "." + TAG, "Loading URL: " + url);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d(InstagramAPI.getTag() + "." + TAG, "onPageFinished URL: " + url);
        }

    }

}
