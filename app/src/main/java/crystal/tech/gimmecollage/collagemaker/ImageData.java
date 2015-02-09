package crystal.tech.gimmecollage.collagemaker;

/**
 * Created by prohor on 30/01/15.
 */
public class ImageData {
    public String dataPath = "";
    public String previewDataPath = "";
    public boolean fromNetwork = false;
    public float angle = 0.0f;

    public ImageData(String data_path, String preview_data_path, boolean from_network) {
        dataPath = data_path;
        previewDataPath = preview_data_path;
        fromNetwork = from_network;
    }
    public ImageData(String data_path, boolean from_network) {
        this(data_path, data_path, from_network);
    }

    public String getDataPath(boolean fullImage) {
        if (!dataPath.isEmpty() && !previewDataPath.isEmpty())
            return  fullImage ? dataPath : previewDataPath;
        if (!dataPath.isEmpty())
            return dataPath;
        return previewDataPath;
    }
}
