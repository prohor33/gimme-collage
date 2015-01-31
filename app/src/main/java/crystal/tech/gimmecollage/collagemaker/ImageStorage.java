package crystal.tech.gimmecollage.collagemaker;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import crystal.tech.gimmecollage.app.R;

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

    public static void addImageToPull(ImageData img) {
        getInstance().addImageToPullImpl(img);
    }
    private void addImageToPullImpl(ImageData img) {
        if (img.dataPath.isEmpty())
            throw new IllegalArgumentException("dataPath is empty");
        // TODO: remove this debug random
        pullImages.put(img.dataPath.hashCode() + (int)(Math.random() * 10000), img);
    }

    public static void addImageToCollage(ImageData img) {
        getInstance().addImageToCollageImpl(img);
    }
    private void addImageToCollageImpl(ImageData img) {
        if (img.dataPath.isEmpty())
            throw new IllegalArgumentException("dataPath is empty");
        // TODO: remove this debug random
        collageImages.put(img.dataPath.hashCode() + (int)(Math.random() * 10000), img);
    }

    public static int getPullImageCount() {
        return getInstance().pullImages.size();
    }

    public static ImageData getPullImage(int i) {
        return getInstance().getPullImageByIndex(i);
    }

    public static ImageData getCollageImage(int i) {
        return getInstance().getCollageImageByIndex(i);
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
        if (i >= pullImages.size())
            return;
        ImageData image = getPullImageByIndex(i);
        if (image.fromNetwork) {
            fillViewFromNetwork(iv, image);
        } else {
            fillViewFromHardDrive(iv, image);
        }
    }

    public static void fillCollageView(ImageView iv, int i) {
        getInstance().fillCollageViewImpl(iv, i);
    }
    private void fillCollageViewImpl(ImageView iv, int i) {
        if (i >= collageImages.size())
            return;
        ImageData image = getCollageImageByIndex(i);
        if (image.fromNetwork) {
            fillViewFromNetwork(iv, image);
        } else {
            fillViewFromHardDrive(iv, image);
        }
    }

    public static void moveAllImagesFromPullToCollage() {
        getInstance().updateImageCountInCollageImpl();
    }

    public static void updateImageCountInCollage() {
        getInstance().updateImageCountInCollageImpl();
    }
    private void updateImageCountInCollageImpl() {
        int count = CollageMaker.getInstance().getVisibleImageCount();
        int move_to_collage = count - collageImages.size();
        if (move_to_collage != 0)
            moveImagesBetweenPullAndCollage(Math.abs(move_to_collage), move_to_collage > 0);
    }

    // private members only ==========

    private ImageData getCollageImageByIndex(int index) {
        return getCollageImageByIndex(index, false);
    }

    private ImageData getCollageImageByIndex(int index, boolean remove) {
        // TODO: smart ordering
        Iterator it = collageImages.entrySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (i == index) {
                if (remove)
                    it.remove();
                return (ImageData)pair.getValue();
            }
            i++;
        }
        return null;
    }

    private void fillViewFromNetwork(ImageView iv, ImageData image) {
        View parent = (View)iv.getParent();
        final ProgressBar pb = (ProgressBar)parent.findViewById(R.id.progressBar);
        if (pb != null)
            pb.setVisibility(View.VISIBLE);

        Callback on_load = new Callback() {
            @Override
            public void onSuccess() {
                if (pb != null)
                    pb.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                if (pb != null)
                    pb.setVisibility(View.GONE);
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
        return getPullImageByIndex(index, false);
    }

    private ImageData getPullImageByIndex(int index, boolean remove) {
        // TODO: smart ordering
        Iterator it = pullImages.entrySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (i == index) {
                if (remove)
                    it.remove();
                return (ImageData)pair.getValue();
            }
            i++;
        }
        return null;
    }

    private void moveImagesBetweenPullAndCollage(int count, boolean from_pull_to_collage) {
        if (from_pull_to_collage) {
            for (int i = 0; i < count && pullImages.size() > 0; i++) {
                Log.d(TAG, "i = " + i + " pullImages.size() = " + pullImages.size());
                addImageToCollage(getPullImageByIndex(0, true));
            }
        } else {
            for (int i = 0; i < count && collageImages.size() > 0; i++) {
                addImageToPull(getCollageImageByIndex(0, true));
            }
        }
    }
}
