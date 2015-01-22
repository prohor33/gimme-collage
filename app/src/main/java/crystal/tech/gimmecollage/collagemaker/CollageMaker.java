package crystal.tech.gimmecollage.collagemaker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import crystal.tech.gimmecollage.analytics.GoogleAnalyticsUtils;
import crystal.tech.gimmecollage.app.CollageActivity;
import crystal.tech.gimmecollage.app.R;
import crystal.tech.gimmecollage.instagram_api.InstagramAPI;
import crystal.tech.gimmecollage.instagram_api.Storage;

/**
 * Created by prohor on 04/10/14.
 */
public class CollageMaker {

    private static final String TAG = "CollageMaker";

    private CollageType m_eType = CollageType.Grid;
    private Map<CollageType, CollageConfig> m_mCollages;
    private RelativeLayout m_rlCollage = null;
    private int m_iCollageSize;
    private Point m_pCollagePadding = new Point(0, 0);
    private Activity m_pActivity;

    // big_frame -> big_rl -> rl -> image_view + progress
    // this is rls
    private ArrayList<View> m_vImageRLViews = new ArrayList<View>();

    public enum ImageSourceType {Instagram, Gallery, None};

    public class ImageData {
        ImageData(String url, int like_count) {
            m_strUrl = url;
            m_iLikeCount = like_count;
            m_eSrc = ImageSourceType.Instagram;
        }
        ImageData(String path) {
            m_strImagePath = path;
            m_eSrc = ImageSourceType.Gallery;
        }

        public String getUrl() { return m_strUrl; }
        public int getLikes() { return m_iLikeCount; }
        public String getImagePath() { return m_strImagePath; }
        public ImageSourceType getSrc() { return m_eSrc; };


        private ImageSourceType m_eSrc = ImageSourceType.None;

        // instagram data
        private String m_strUrl = "";
        private int m_iLikeCount = -1;
        // gallery data
        private String m_strImagePath = "";
    }

    private List<ImageData> m_lImages = new ArrayList<ImageData>();

    private static CollageMaker m_pInstance;

    public static synchronized CollageMaker getInstance() {
        if (m_pInstance == null) {
            m_pInstance = new CollageMaker();
        }
        return m_pInstance;
    }

    public CollageConfig getCollageConf() {
        return getCollageConf(m_eType);
    }
    public CollageConfig getCollageConf(CollageType eType) {
        if (!m_mCollages.containsKey(eType)) {
            Log.v(TAG, "Error: no such a collage: " + eType);
            return null;
        }
        return m_mCollages.get(eType);
    }

    public int getCollageTypeIndex() {
        return m_eType.ordinal();
    }
    public void putCollageType(CollageType type) {
        m_eType = type;
    }

    public void MoveToTheOtherCollageType(int type_index) {
        if (type_index < 0 || type_index >= CollageType.values().length) {
            Log.v(TAG, "Error: wrong collage type index = " + type_index);
            return;
        }
        moveToTheOtherCollageType(CollageMaker.CollageType.values()[type_index]);
    }

    public void moveToTheOtherCollageType(CollageType type) {
        putCollageType(type);
        UpdateCollageRestructuring();
    }

    public void putCollageLayout(RelativeLayout collage_layout) {
        m_rlCollage = collage_layout;
    }

    public void putCollageSize(int collage_size) {
        this.m_iCollageSize = collage_size;
    }
    public void putCollagePadding(Point collage_padding) {
        this.m_pCollagePadding = collage_padding;
    }

    public int getImageCount() {
        return m_vImageRLViews.size();
    }
    public RelativeLayout getImageRL(int index) {
        return (RelativeLayout)m_vImageRLViews.get(index);
    }

    public int getMaxImageCount() {
        int max_count = -1;
        for (CollageType type : CollageType.values()) {
            int count = getCollageConf(type).getPhotoCount();
            max_count = count > max_count ? count : max_count;
        }
        return max_count;
    }

    public void UpdateCollageRestructuring() {
        if (m_rlCollage == null)
            return;
        if (getCollageConf().getPhotoCount() > m_rlCollage.getChildCount()) {
            Log.v(TAG, "Error: too few image views");
            return;
        }

        // TODO: should we do this each frame?
        PrepareImages();
        RunAnimImageViews();
    }

    public void InitImageViews(Activity activity) {
        m_pActivity = activity;
        Log.v(TAG, "InitImageViews()");
        Log.v(TAG, "m_rlCollage.getChildCount() = " + m_rlCollage.getChildCount());
        Log.v(TAG, "getCollageConf().getPhotoCount() = " + getCollageConf().getPhotoCount());

        m_vImageRLViews.clear();
        for (int i = 0; i < m_rlCollage.getChildCount(); i++) {
            m_vImageRLViews.add(m_rlCollage.getChildAt(i));
        }

        for (int i = 0; i < m_vImageRLViews.size() &&
                i < getCollageConf().getPhotoCount(); i++) {
            updateViewPosition(i);
        }

        PrepareImages();
        InitCollageLayoutSize();
    }

    private void InitCollageLayoutSize() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) m_rlCollage.getLayoutParams();
        layoutParams.width = m_iCollageSize;
        layoutParams.height = m_iCollageSize;
        m_rlCollage.setLayoutParams(layoutParams);
        m_rlCollage.setClipChildren(false);
    }


    public enum CollageType {
        Grid,
        CenterWithGridAround,
        Test1,
        Test2,
        Test3
    };

    public Bitmap GenerateCollageImage() {
        final int target_size = 1024;
        Bitmap collageImage = Bitmap.createBitmap(target_size,
                target_size, Bitmap.Config.ARGB_8888);
        Canvas comboCanvas = new Canvas(collageImage);

        for (int i = 0; i < m_vImageRLViews.size() &&
                i < getCollageConf().getPhotoCount(); i++) {
            RelativeLayout rl = (RelativeLayout)m_vImageRLViews.get(i);
            ImageView iv = (ImageView)rl.getChildAt(0);
            PhotoPosition photoPos = getCollageConf().getPhotoPos(i);

            BitmapDrawable bitmapDrawable = ((BitmapDrawable) iv.getDrawable());
            if (bitmapDrawable == null)
                continue;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            final int square_size = Math.min(bitmap.getHeight(), bitmap.getWidth());
            // crop square in center
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, square_size, square_size);
            int size = (int)(target_size * photoPos.getSize());
            comboCanvas.drawBitmap(Bitmap.createScaledBitmap(bitmap, size, size, true),
                    target_size * photoPos.getX(),
                    target_size * photoPos.getY(),
                    new Paint(Paint.FILTER_BITMAP_FLAG));
        }

        Log.v(TAG, "Collage is successfully generated!");

        return collageImage;
    }

    private CollageMaker() {
        m_eType = CollageType.Grid; // by default
        m_mCollages = new HashMap<CollageType, CollageConfig>();
        for (CollageType type : CollageType.values()) {
            m_mCollages.put(type, new CollageConfig(type));
        }

        this.m_iCollageSize = 100;  // in pixels
    }



    private void PrepareImages() {
        for (int i = 0; i < m_vImageRLViews.size(); i++) {
            View iv = m_vImageRLViews.get(i);

            if (i < getCollageConf().getPhotoCount()) {
                iv.setVisibility(View.VISIBLE);
            } else {
                iv.setVisibility(View.GONE);
            }
        }
    }

    private void RunAnimImageViews() {
        for (int i = 0; i < m_vImageRLViews.size() &&
                i < getCollageConf().getPhotoCount(); i++) {
            final View iv = m_vImageRLViews.get(i);

            PhotoPosition pPhotoPos = getCollageConf().getPhotoPos(i);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)iv.getLayoutParams();
            final int new_h = (int) (m_iCollageSize * pPhotoPos.getSize());
            final int new_w = (int) (m_iCollageSize * pPhotoPos.getSize());
            final int new_left = (int) (m_iCollageSize * pPhotoPos.getX()) + m_pCollagePadding.x;
            final int new_top = (int) (m_iCollageSize * pPhotoPos.getY()) + m_pCollagePadding.y;
//            Log.v(TAG, "RunAnimImageViews()");
            float coef_w = (float)new_w / params.width;
            float coef_h = (float)new_h / params.height;
            int delta_left = new_left - params.leftMargin;
            int delta_top = new_top - params.topMargin;
            final int anim_duration = 500;
            TranslateAnimation translateAnimation = new TranslateAnimation(
                    Animation.ABSOLUTE, 0.0f,
                    Animation.ABSOLUTE, delta_left,
                    Animation.ABSOLUTE, 0.0f,
                    Animation.ABSOLUTE, delta_top);
            translateAnimation.setDuration(anim_duration);
            translateAnimation.setRepeatCount(0);

            Animation scaleAnimation = new ScaleAnimation(
                    1.0f, coef_w,
                    1.0f, coef_h,
                    Animation.RELATIVE_TO_SELF, (float)delta_left / params.width,
                    Animation.RELATIVE_TO_SELF, (float)delta_top / params.height);
            scaleAnimation.setDuration(anim_duration);
            scaleAnimation.setRepeatCount(0);

            AnimationSet animSet = new AnimationSet(true);
            animSet.setFillEnabled(true);
            animSet.setFillAfter(true);

            animSet.setAnimationListener(new TranslateAnimation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) { }

                @Override
                public void onAnimationRepeat(Animation animation) { }

                @Override
                public void onAnimationEnd(Animation animation)
                {
                    iv.clearAnimation();
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)iv.getLayoutParams();
                    params.width = new_w;
                    params.height = new_h;
                    params.topMargin = new_top;
                    params.leftMargin = new_left;
                    iv.setLayoutParams(params);
                }
            });

            animSet.addAnimation(translateAnimation);
            animSet.addAnimation(scaleAnimation);
            iv.startAnimation(animSet);
        }
    }

    public void swapViews(View view1, View view2) {
        int i1 = m_vImageRLViews.indexOf(view1);
        int i2 = m_vImageRLViews.indexOf(view2);
        m_vImageRLViews.set(i1, view2);
        m_vImageRLViews.set(i2, view1);
        updateViewPosition(i1);
        updateViewPosition(i2);
        GoogleAnalyticsUtils.SendEvent(m_pActivity,
                R.string.ga_event_category_swap_images,
                R.string.ga_event_action_swap_images,
                R.string.ga_event_label_swap_images);
    }

    public void updateViewPosition(int i) {
        if (i >= m_vImageRLViews.size()) {
            Log.e(TAG, "Error: updateViewPosition: i = " + i + "size = " + m_vImageRLViews.size());
            return;
        }
        final View v = m_vImageRLViews.get(i);

        PhotoPosition pPhotoPos = getCollageConf().getPhotoPos(i);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
        params.height = (int) (m_iCollageSize * pPhotoPos.getSize());
        params.width = (int) (m_iCollageSize * pPhotoPos.getSize());
        params.leftMargin = (int) (m_iCollageSize * pPhotoPos.getX()) + m_pCollagePadding.x;
        params.topMargin = (int) (m_iCollageSize * pPhotoPos.getY()) + m_pCollagePadding.y;
        v.setLayoutParams(params);
    }

    public void updateViewPosition(View v) {
        updateViewPosition(m_vImageRLViews.indexOf(v));
    }

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
                return o2.m_iLikeCount - o1.m_iLikeCount;
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

