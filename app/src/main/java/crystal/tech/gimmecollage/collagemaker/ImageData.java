package crystal.tech.gimmecollage.collagemaker;

/**
 * Created by prohor on 30/01/15.
 */
public class ImageData {
    public ImageData(String data_path, String preview_data_path, int w, int h, boolean from_network) {
        dataPath = data_path;
        peviewDataPath = preview_data_path;
        width = w;
        height = h;
        fromNetwork = from_network;
    }
    public ImageData(String data_path, int w, int h, boolean from_network) {
        this(data_path, data_path, w, h, from_network);
    }

    public String dataPath = "";
    public String peviewDataPath = "";
    public boolean fromNetwork = false;
    public int width;
    public int height;
}
