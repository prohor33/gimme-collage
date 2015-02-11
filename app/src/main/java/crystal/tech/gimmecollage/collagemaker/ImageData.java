package crystal.tech.gimmecollage.collagemaker;

/**
 * Created by prohor on 30/01/15.
 */
public class ImageData {
    public String dataPath = "";
    public String previewDataPath = "";
    public boolean fromNetwork = false;
    public float angle = 0.0f;
    public long id;

    public ImageData(String data_path, String preview_data_path, long id_tmp, boolean from_network) {
        if (data_path != null)
            dataPath = data_path;
        if (preview_data_path != null)
            previewDataPath = preview_data_path;
        fromNetwork = from_network;
        id = id_tmp;
    }
//    public ImageData(String data_path, boolean from_network) {
//        this(data_path, data_path, from_network);
//    }

    public String getDataPath(boolean fullImage) {
        if (!dataPath.isEmpty() && !previewDataPath.isEmpty())
            return  fullImage ? dataPath : previewDataPath;
        if (!dataPath.isEmpty())
            return dataPath;
        return previewDataPath;
    }
}
