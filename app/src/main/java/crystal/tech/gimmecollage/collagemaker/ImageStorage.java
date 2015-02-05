package crystal.tech.gimmecollage.collagemaker;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import crystal.tech.gimmecollage.app.MainActivity;
import crystal.tech.gimmecollage.app.R;

/**
 * Created by prohor on 29/01/15.
 */
public class ImageStorage {

    private final String TAG = "ImageStorage";

    private static ImageStorage instance;
    private Map<Integer, ImageData> pullImages = new HashMap<>();
    private Map<Integer, ImageData> collageImages = new HashMap<>();
    private List<Integer> pullImagesOrder = new ArrayList<>();
    private List<Integer> collageImagesOrder = new ArrayList<>();
    private MainActivity pullActivity = null;
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
        int id = img.dataPath.hashCode() + (int)(Math.random() * 10000);
        pullImages.put(id, img);
        pullImagesOrder.add(0, id);
    }

    public static void addImageToCollage(ImageData img) {
        getInstance().addImageToCollageImpl(img);
    }
    private void addImageToCollageImpl(ImageData img) {
        if (img.dataPath.isEmpty())
            throw new IllegalArgumentException("dataPath is empty");
        // TODO: remove this debug random
        int id = img.dataPath.hashCode() + (int)(Math.random() * 10000);
        collageImages.put(id, img);
        collageImagesOrder.add(id);
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


    public static void putPullActivity(MainActivity activity) {
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
        fillView(iv, image, image.fromNetwork);
    }

    public static void fillCollageView(ImageView iv, int i) {
        getInstance().fillCollageViewImpl(iv, i);
    }
    private void fillCollageViewImpl(ImageView iv, int i) {
        ImageData image = getCollageImageByIndex(i);
        if (image == null)
            return;
        fillView(iv, image, image.fromNetwork);
    }

    // return true if some images where moved to collage
    public static boolean moveAllImagesFromPullToCollage() {
        return getInstance().moveAllImagesFromPullToCollageImpl();
    }
    private boolean moveAllImagesFromPullToCollageImpl() {
        int count = CollageMaker.getInstance().getVisibleImageCount();
        int move_to_collage = count - collageImages.size();
        getInstance().updateImageCountInCollageImpl();
        return move_to_collage > 0;
    }

    public static void updateImageCountInCollage() {
        getInstance().updateImageCountInCollageImpl();
    }
    private void updateImageCountInCollageImpl() {
        int count = CollageMaker.getInstance().getVisibleImageCount();
        int move_to_collage = count - collageImages.size();
        if (move_to_collage != 0)
            moveImagesBetweenPullAndCollage(Math.abs(move_to_collage), move_to_collage > 0);

        pullActivity.getRightDrawer().getAdapter().notifyDataSetChanged();
    }

    // private members only ==========

    private ImageData getPullImageByIndex(int index) {
        return getPullImageByIndex(index, false);
    }

    private ImageData getPullImageByIndex(int index, boolean remove) {
        if (index >= pullImagesOrder.size())
            return null;
        Integer id = pullImagesOrder.get(index);
        ImageData imageData = pullImages.get(id);
        if (remove)
            removePullImage(id);
        return imageData;
    }

    private ImageData getCollageImageByIndex(int index) {
        return getCollageImageByIndex(index, false);
    }

    private ImageData getCollageImageByIndex(int index, boolean remove) {
        if (index >= collageImagesOrder.size())
            return null;
        int id = collageImagesOrder.get(index);
        ImageData imageData = collageImages.get(id);
        if (remove)
            removeCollageImage(id);
        return imageData;
    }

    private void fillView(final ImageView iv, ImageData image, boolean from_network) {
        if (iv.getTag() != null) {
            // It's already loading
            ImageLoadingTarget target = (ImageLoadingTarget) iv.getTag();
            if (target.url == image.peviewDataPath)
                return;
            // Abort loading to start new one
            target.cancel();
            iv.setTag(null);
        }

        View parent = (View)iv.getParent();
        iv.setImageDrawable(null);
        final ProgressBar pb = (ProgressBar)parent.findViewById(R.id.progressBar);
        if (pb != null)
            pb.setVisibility(View.VISIBLE);

        ImageLoadingTarget t = new ImageLoadingTarget(iv, pb, pullActivity);
        t.url = image.peviewDataPath;
        iv.setTag(t);

        if (from_network) {
            Picasso.with(pullActivity)
                    .load(image.peviewDataPath)
                    .error(R.drawable.ic_content_problem)
                    .into(t);
        } else {
            Picasso.with(pullActivity)
                    .load(new File(image.peviewDataPath))
                    .error(R.drawable.ic_content_problem)
                    .into(t);
        }
    }

    private void moveImagesBetweenPullAndCollage(int count, boolean from_pull_to_collage) {
        if (from_pull_to_collage) {
            for (int i = 0; i < count && pullImages.size() > 0; i++) {
                addImageToCollage(grabPullImage());
            }
        } else {
            for (int i = 0; i < count && collageImages.size() > 0; i++) {
                addImageToPull(grabCollageImage());
            }
        }
    }

    // removing
    private ImageData grabPullImage() {
        return getPullImageByIndex(0, true);
    }

    // removing
    private ImageData grabCollageImage() {
        return getCollageImageByIndex(collageImagesOrder.size() - 1, true);
    }

    private void removePullImage(Integer id) {
        pullImages.remove(id);
        pullImagesOrder.remove(id);
    }

    private void removeCollageImage(Integer id) {
        collageImages.remove(id);
        collageImagesOrder.remove(id);
    }
}
