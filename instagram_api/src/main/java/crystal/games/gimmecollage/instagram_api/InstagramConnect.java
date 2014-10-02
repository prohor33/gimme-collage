package crystal.games.gimmecollage.instagram_api;

import android.media.Image;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import crystal.games.gimmecollage.instagram_api.InstagramSession.*;

/**
 * Created by Noitorra on 01.10.2014.
 */
public class InstagramConnect {
    private final String DEBUG_TAG = "InstagramAPI.InstagramConnect";
    private final int READ_TIMEOUT = 10000;
    private final int CONNECT_TIMEOUT = 15000;

    private String mHost = "";

    InstagramConnect(final String host) {
        mHost = host;
    }

    public UserInfo fetchUserInfo(final String strUserID, final String accessToken)
            throws Exception {
        String strAnswer = fetchData(mHost + "users/" + strUserID
                + "?access_token=" + accessToken, "GET");

        JSONObject jsonObj = (JSONObject) new JSONTokener(strAnswer).nextValue();
        return parseJSONObjectToUserInfo(jsonObj.getJSONObject("data"));
    }

    public void fetchFollows(List<UserInfo> userInfoList, final String userId,
                             final String accessToken) throws Exception {

        String strAnswer = fetchData(mHost + "users/" + userId + "/follows"
                + "?access_token=" + accessToken, "GET");

        // Clear follows list and load it from snatch.
        userInfoList.clear();

        JSONObject jsonObj = (JSONObject) new JSONTokener(strAnswer).nextValue();
        JSONArray jsonArray = jsonObj.getJSONArray("data");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonUser = jsonArray.getJSONObject(i);
            userInfoList.add(parseJSONObjectToUserInfo(jsonUser));
        }
    }

    public void fetchImageInfo(List<ImageInfo> imageInfos, final String userId,
                               final String accessToken) throws Exception {

        // https://api.instagram.com/v1/users/1510317720/media/recent
        String strAnswer = fetchData(mHost + "users/" + userId + "/media/recent"
                + "?access_token=" + accessToken, "GET");

        // Clear Images and set them again.
        imageInfos.clear();

        JSONObject jsonObj = (JSONObject) new JSONTokener(strAnswer).nextValue();
        JSONArray jsonArray = jsonObj.getJSONArray("data");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonMedia = jsonArray.getJSONObject(i);
            imageInfos.add(parseJSONObjectToImageInfo(jsonMedia));
        }
    }

    /* Private section */

    private String fetchData(final String strURL, final String requestMethod) throws Exception {
        Log.d(DEBUG_TAG, "Request: " + strURL);
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
        Log.d(DEBUG_TAG, "The response is: " + response);
        // Convert the InputStream into a string
        contentAsString = streamToString(urlConnection.getInputStream());

        Log.d(DEBUG_TAG, "Answer: " + contentAsString);
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

    private UserInfo parseJSONObjectToUserInfo(JSONObject jsonObject) throws JSONException {
        UserInfo userInfo = new UserInfo();

        userInfo.username = jsonObject.getString("username");
        userInfo.bio = jsonObject.getString("bio");
        userInfo.website = jsonObject.getString("website");
        userInfo.profile_picture = jsonObject.getString("profile_picture");
        userInfo.full_name = jsonObject.getString("full_name");
        userInfo.id = jsonObject.getString("id");

        return userInfo;
    }

    private ImageInfo parseJSONObjectToImageInfo(JSONObject jsonObject) throws JSONException {
        ImageInfo imageInfo = new ImageInfo();

        imageInfo.likes_count = jsonObject.getJSONObject("likes").getInt("count");

        JSONObject jsonImages = jsonObject.getJSONObject("images");

        if(!jsonImages.isNull("low_resolution")) {
            JSONObject jsonLowRes = jsonImages.getJSONObject("low_resolution");
            imageInfo.low_resolution.url = jsonLowRes.getString("url");
            imageInfo.low_resolution.width = jsonLowRes.getInt("width");
            imageInfo.low_resolution.height = jsonLowRes.getInt("height");
        }

        if(!jsonImages.isNull("thumbnail")) {
            JSONObject jsonThumbnail = jsonImages.getJSONObject("thumbnail");
            imageInfo.thumbnail.url = jsonThumbnail.getString("url");
            imageInfo.thumbnail.width = jsonThumbnail.getInt("width");
            imageInfo.thumbnail.height = jsonThumbnail.getInt("height");
        }

        if(!jsonImages.isNull("standard_resolution")) {
            JSONObject jsonStndRes = jsonImages.getJSONObject("standard_resolution");
            imageInfo.standard_resolution.url = jsonStndRes.getString("url");
            imageInfo.standard_resolution.width = jsonStndRes.getInt("width");
            imageInfo.standard_resolution.height = jsonStndRes.getInt("height");
        }

        return imageInfo;
    }
}
