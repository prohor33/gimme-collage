package crystal.tech.gimmecollage.collagemaker;

import android.app.Activity;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by prohor on 29/01/15.
 */
public class ImageStorage {

    private final String TAG = "ImageStorage";

    private static ImageStorage instance;
    private Map<Integer, ImageData> pullImages = new HashMap<>();
    private Map<Integer, ImageData> collageImages = new HashMap<>();
    private Activity pullActivity = null;
    private Activity collageActivity = null;

    public static synchronized ImageStorage getInstance() {
        if (instance == null) {
            instance = new ImageStorage();
        }
        return instance;
    }

    public static void addImage(ImageData img) {
        getInstance().addImageImpl(img);
    }
    private void addImageImpl(ImageData img) {
        if (img.dataPath.isEmpty())
            throw new IllegalArgumentException("dataPath is empty");
        // TODO: remove this debug rundom
        pullImages.put(img.dataPath.hashCode() + (int)(Math.random() * 10000), img);
    }

    public static int getPullImageCount() {
        return getInstance().pullImages.size();
    }

    public static int getCollageImageCount() {
        return getInstance().collageImages.size();
    }

    public static ImageData getPullImage(int i) {
        return getInstance().getImagePullImpl(i);
    }
    private ImageData getImagePullImpl(int i) {
        if (i < 0 || i >= pullImages.size())
            throw new IllegalArgumentException("wrong index");
        return pullImages.get(i);
    }

    public static void putPullActivity(Activity activity) {
        getInstance().pullActivity = activity;
    }

    public static void putCollageActivity(Activity activity) {
        getInstance().collageActivity = activity;
    }

    public static void fillPullView(ImageView iv, int i) {
        getInstance().fillPullViewImpl(iv, i);
    }
    private void fillPullViewImpl(ImageView iv, int i) {
        ImageData image = getPullImageByIndex(i);
        if (image.fromNetwork) {
            fillViewFromNetwork(iv, image);
        } else {
            fillViewFromHardDrive(iv, image);
        }
    }

    private void fillViewFromNetwork(ImageView iv, ImageData image) {
        Callback on_load = new Callback() {
            @Override
            public void onSuccess() {
//                if (pb != null)
//                    pb.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
//                if (pb != null)
//                    pb.setVisibility(View.GONE);
            }
        };

        Picasso.with(pullActivity)
               .load(image.peviewDataPath)
                .into(iv, on_load);
    }

    private void fillViewFromHardDrive(ImageView iv, ImageData image) {
        // TODO: to implement
        Log.w(TAG, "fillViewFromHardDrive not implemented yet");
    }

    private ImageData getPullImageByIndex(int index) {
        // TODO: smart ordering
        Iterator it = pullImages.entrySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (i == index) {
                return (ImageData)pair.getValue();
            }
            i++;
        }
        throw new RuntimeException("index = " + index + ", size = " + pullImages.size());
    }

    public static void fillCollageView(ImageView iv, int i) {
        getInstance().fillCollageViewImpl(iv, i);
    }
    private void fillCollageViewImpl(ImageView iv, int i) {
        ImageData image = getCollageImageByIndex(i);
        if (image.fromNetwork) {
            fillViewFromNetwork(iv, image);
        } else {
            fillViewFromHardDrive(iv, image);
        }
    }

    private ImageData getCollageImageByIndex(int index) {
        // TODO: smart ordering
        Iterator it = collageImages.entrySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            if (i == index) {
                Map.Entry pair = (Map.Entry) it.next();
                return (ImageData)pair.getValue();
            }
            i++;
        }
        throw new RuntimeException("index = " + index + ", size = " + collageImages.size());
    }
}
