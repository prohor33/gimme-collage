package crystal.tech.gimmecollage.lenta_api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.List;


/**
 * Created by prohor on 14/01/15.
 */

class Parser {

    private static final String TAG = "Parser";

    public Parser() {}

    public void parsePostsList(List<Storage.PostInfo> postInfos, String strAnswer) throws JSONException {
        // Clear follows list and load it from snatch.
        postInfos.clear();

        JSONObject jsonObject = (JSONObject) new JSONTokener(strAnswer).nextValue();
        int version = jsonObject.getInt("version");
        Log.v(TAG, "json version = " + version);
        JSONArray jsonArray = (JSONArray) jsonObject.getJSONArray("data");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonUser = jsonArray.getJSONObject(i);
            postInfos.add(parseJSONObjectToPostInfo(jsonUser));
        }
    }

    private Storage.PostInfo parseJSONObjectToPostInfo(JSONObject jsonObject) throws JSONException {
        Storage.PostInfo postInfo = new Storage.PostInfo();

        postInfo.id = jsonObject.getString("id");
        postInfo.nickname = jsonObject.getString("nickname");
        postInfo.image.url = jsonObject.getString("image");
        postInfo.image.width = jsonObject.getInt("image_w");
        postInfo.image.height = jsonObject.getInt("image_h");
        postInfo.image_preview.url = jsonObject.getString("image_preview");
        postInfo.image_preview.width = jsonObject.getInt("image_preview_w");
        postInfo.image_preview.height = jsonObject.getInt("image_preview_h");
        postInfo.text = jsonObject.getString("text");

        return postInfo;
    }
}
