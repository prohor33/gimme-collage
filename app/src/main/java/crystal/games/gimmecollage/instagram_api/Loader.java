package crystal.games.gimmecollage.instagram_api;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by kisame on 15.10.2014.
 */
class Loader {

    public static final String REQUEST_GET = "GET";
    public static final String REQUEST_POST = "POST";

    private final String TAG = "Loader";
    private final int READ_TIMEOUT = 10000;
    private final int CONNECT_TIMEOUT = 15000;

    private String mHost;

    public Loader(String host) {
        mHost = host;
    }

    public String fetchUserInfo(String accessToken, String userId) throws IOException {
        return fetchSomeData(mHost + "users/" + userId + "?access_token=" + accessToken, REQUEST_GET);
    }

    public String fetchFollows(String accessToken, String userId) throws IOException {
        return fetchSomeData(mHost + "users/" + userId + "/follows" + "?access_token=" + accessToken, REQUEST_GET);
    }

    public String fetchImages(String accessToken, String userId) throws IOException {
        return fetchSomeData(mHost + "users/" + userId + "/media/recent" + "?access_token=" + accessToken, REQUEST_GET);
    }

    private String fetchSomeData(String strURL, String requestMethod) throws IOException {
        Log.d(InstagramAPI.getTag() + "." + TAG, "Request: " + strURL);
        String contentAsString;

        URL url = new URL(strURL);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setReadTimeout(READ_TIMEOUT /* milliseconds */);
        urlConnection.setConnectTimeout(CONNECT_TIMEOUT /* milliseconds */);
        urlConnection.setRequestMethod(requestMethod);
        urlConnection.setDoInput(true);
        // Starts the query
        urlConnection.connect();
        int response = urlConnection.getResponseCode();
        Log.d(InstagramAPI.getTag() + "." + TAG, "The response is: " + response);
        // Convert the InputStream into a string
        contentAsString = streamToString(urlConnection.getInputStream());

        Log.d(InstagramAPI.getTag() + "." + TAG, "Answer: " + contentAsString);
        return contentAsString;
    }

    private String streamToString(InputStream is) throws IOException {
        String str = "";
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
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
}
