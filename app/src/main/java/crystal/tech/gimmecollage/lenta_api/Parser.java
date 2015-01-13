package crystal.tech.gimmecollage.lenta_api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.List;


/**
 * Created by prohor on 14/01/15.
 */

class Parser {

    public Parser() {}

    public void parsePostsList(List<Storage.PostInfo> postInfos, String strAnswer) throws JSONException {
        // Clear follows list and load it from snatch.
        postInfos.clear();

        JSONArray jsonArray = (JSONArray) new JSONTokener(strAnswer).nextValue();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonUser = jsonArray.getJSONObject(i);
            postInfos.add(parseJSONObjectToPostInfo(jsonUser));
        }
    }

    private Storage.PostInfo parseJSONObjectToPostInfo(JSONObject jsonObject) throws JSONException {
        Storage.PostInfo postInfo = new Storage.PostInfo();

        postInfo.id = jsonObject.getString("id");
        postInfo.nickname = jsonObject.getString("nickname");
        postInfo.image = jsonObject.getString("image");
        postInfo.image_preview = jsonObject.getString("image_preview");
        postInfo.text = jsonObject.getString("text");

        return postInfo;
    }
}
