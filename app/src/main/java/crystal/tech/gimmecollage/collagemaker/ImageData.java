package crystal.tech.gimmecollage.collagemaker;

/**
 * Created by prohor on 30/01/15.
 */
public class ImageData {
    public ImageData(String data_path, String preview_data_path, boolean from_network) {
        dataPath = data_path;
        peviewDataPath = preview_data_path;
        fromNetwork = from_network;
    }
    public ImageData(String data_path, boolean from_network) {
        this(data_path, data_path, from_network);
    }

    public String dataPath = "";
    public String peviewDataPath = "";
    public boolean fromNetwork = false;
//    public int width; // unused?
//    public int height;
}
