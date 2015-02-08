package crystal.tech.gimmecollage.collagemaker;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
    private MainActivity mainActivity = null;
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
        int id = generateID(img);
        pullImages.put(id, img);
        pullImagesOrder.add(0, id);
    }

    public static void addImageToCollage(ImageData img) {
        getInstance().addImageToCollageImpl(img);
    }
    private void addImageToCollageImpl(ImageData img) {
        if (img.dataPath.isEmpty())
            throw new IllegalArgumentException("dataPath is empty");
        int id = generateID(img);
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


    public static void putMainActivity(MainActivity activity) {
        getInstance().mainActivity = activity;
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
        CollageUtils.fillView(iv, image, null, image.fromNetwork);
    }

    public static void fillCollageView(ImageView iv, int i) {
        getInstance().fillCollageViewImpl(iv, i);
    }
    private void fillCollageViewImpl(ImageView iv, int i) {
        ImageData image = getCollageImageByIndex(i);
        if (image == null)
            return; // not such many photos available
        ImageViewData viewData = CollageMaker.getViewDataByFLView(ImageViewData.getParentFLByIV(iv));
        CollageUtils.fillView(iv, image, viewData, image.fromNetwork);
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

        updatePull();
    }

    public static void dropPullImageToCollage(int pullIndex, ImageView collageImageView) {
        getInstance().dropPullImageToCollageImpl(pullIndex, collageImageView);
    }
    private void dropPullImageToCollageImpl(int pullIndex, ImageView collageImageView) {
        View parentFLView = (View) collageImageView.getParent().getParent();
        int collageIndex = CollageMaker.getInstance().getIndexByFLView(parentFLView);
        if (collageIndex < 0 || collageIndex >= collageImages.size()) {
            Log.e(TAG, "trying drop to nowhere");
            return;
        }
        ImageData pullImageData = getPullImageByIndex(pullIndex, true);
        ImageData collageImageData = replaceCollageImageByIndex(collageIndex, pullImageData);
        addImageToPull(collageImageData);

        updatePull();
        updateCollage();
    }

    public static void dropCollageImageToCollage(int collageIndex1, ImageView collageImageView) {
        getInstance().dropCollageImageToCollageImpl(collageIndex1, collageImageView);
    }
    private void dropCollageImageToCollageImpl(int collageIndex1, ImageView collageImageView) {
        View parentFLView = (View) collageImageView.getParent().getParent();
        int collageIndex2 = CollageMaker.getInstance().getIndexByFLView(parentFLView);
        if (collageIndex1 == collageIndex2)
            return; // nothing to swap
        ImageData image1 = getCollageImage(collageIndex1);
        ImageData image2 = replaceCollageImageByIndex(collageIndex2, image1);
        replaceCollageImageByIndex(collageIndex1, image2);

        updateCollage();
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

    private ImageData replaceCollageImageByIndex(int index, ImageData newImage) {
        if (index >= collageImagesOrder.size())
            return null;
        int old_id = collageImagesOrder.get(index);
        ImageData oldImage = collageImages.get(old_id);
        int new_id = generateID(newImage);
        collageImagesOrder.set(index, new_id);
        collageImages.remove(old_id);
        collageImages.put(new_id, newImage);
        return oldImage;
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

    private void updatePull() {
        mainActivity.getRightDrawer().getAdapter().notifyDataSetChanged();
    }

    private void updateCollage() {
        CollageMaker.updateImageData();
    }

    private int generateID(ImageData img) {
        // TODO: remove this debug random
        return img.dataPath.hashCode() + (int)(Math.random() * 10000);
    }
}
