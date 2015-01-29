package crystal.tech.gimmecollage.collagemaker;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import crystal.tech.gimmecollage.instagram_api.InstagramAPI;
import crystal.tech.gimmecollage.instagram_api.Storage;

/**
 * Created by prohor on 29/01/15.
 */
public class ImageStorage {

    private final String TAG = "ImageStorage";

    private static ImageStorage instance;

    public static synchronized ImageStorage getInstance() {
        if (instance == null) {
            instance = new ImageStorage();
        }
        return instance;
    }

    public enum ImageSourceType {Instagram, Gallery, None};

    public class ImageData {
        ImageData(String url, int like_count) {
            strUrl = url;
            likeCount = like_count;
            eSrc = ImageSourceType.Instagram;
        }
        ImageData(String path) {
            m_strImagePath = path;
            eSrc = ImageSourceType.Gallery;
        }

        public String getUrl() { return strUrl; }
        public String getImagePath() { return m_strImagePath; }
        public ImageSourceType getSrc() { return eSrc; };


        private ImageSourceType eSrc = ImageSourceType.None;

        // instagram data
        private String strUrl = "";
        private int likeCount = -1;
        // gallery data
        private String m_strImagePath = "";
    }

    private List<ImageData> m_lImages = new ArrayList<ImageData>();

    public void setImagesFromGallery(String[] image_paths) {
        m_lImages.clear();
        for (String str : image_paths) {
            m_lImages.add(new ImageData(str));
        }
    }

    public void getImagesFromInstagram() {
        List<Storage.ImageInfo> imagesInfo = InstagramAPI.getImages();
        Log.v(TAG, "have " + imagesInfo.size() + " images");
        m_lImages.clear();
        for (int i = 0; i < imagesInfo.size(); i++) {
            m_lImages.add(new ImageData(imagesInfo.get(i).standard_resolution.url,
                    imagesInfo.get(i).likes_count));
        }

        class ImageComparator implements Comparator<ImageData> {
            @Override
            public int compare(ImageData o1, ImageData o2) {
                return o2.likeCount - o1.likeCount;
            }
        }

        // Let's sort images by likes count
        Collections.sort(m_lImages, new ImageComparator());
    }

    public int getImagesDataSize() {
        return m_lImages.size();
    }

    public ImageData getImageData(int i) {
        if (i < 0 || i >= m_lImages.size())
            throw new RuntimeException("wrong index");
        return m_lImages.get(i);
    }
}
