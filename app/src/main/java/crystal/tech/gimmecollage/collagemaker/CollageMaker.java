package crystal.tech.gimmecollage.collagemaker;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import crystal.tech.gimmecollage.analytics.GoogleAnalyticsUtils;
import crystal.tech.gimmecollage.app.R;
import crystal.tech.gimmecollage.instagram_api.InstagramAPI;
import crystal.tech.gimmecollage.instagram_api.Storage;

/**
 * Created by prohor on 04/10/14.
 */
public class CollageMaker {

    private static final String TAG = "CollageMaker";

    private CollageType eType = CollageType.Grid;
    private Map<CollageType, CollageConfig> mCollages;
    private GestureRelativeLayout rlCollage = null;
    private int collageWidth;   // relative layout width
    private Activity parentActivity;
    private CollageAnimation collageAnimation =  new CollageAnimation();

    // big_frame -> big_rl -> frame layout -> image_view + progress + back_image
    // this is rls
    private ArrayList<View> imageFLViews = new ArrayList<View>();

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

    private static CollageMaker instance;

    public static synchronized CollageMaker getInstance() {
        if (instance == null) {
            instance = new CollageMaker();
        }
        return instance;
    }

    private CollageMaker() {
        eType = CollageType.Grid; // by default
        mCollages = new HashMap<CollageType, CollageConfig>();
        for (CollageType type : CollageType.values()) {
            mCollages.put(type, new CollageConfig(type));
        }
    }

    public CollageConfig getCollageConf() {
        return getCollageConf(eType);
    }

    public CollageConfig getCollageConf(CollageType eType) {
        if (!mCollages.containsKey(eType)) {
            Log.v(TAG, "Error: no such a collage: " + eType);
            return null;
        }
        return mCollages.get(eType);
    }

    public int getCollageTypeIndex() {
        return eType.ordinal();
    }


    public void changeCollageType(int type_index) {
        if (type_index < 0 || type_index >= CollageType.values().length) {
            Log.v(TAG, "Error: wrong collage type index = " + type_index);
            return;
        }
        changeCollageType(CollageMaker.CollageType.values()[type_index]);
    }

    public void changeCollageType(CollageType type) {
        eType = type;
        updateImageViews();
        CollageUtils.getInstance().updateCollageTypeSelectors();
        collageAnimation.onChangeCollageType();
    }

    public static void initImageViews(Activity activity, View rootView) {
        CollageUtils.Init(activity, rootView);
        getInstance().initImageViewsImpl(activity, rootView);
    }

    private void initImageViewsImpl(final Activity activity, View rootView) {
        parentActivity = activity;
        rlCollage = (GestureRelativeLayout) rootView.findViewById(R.id.rlCollage);
        collageAnimation.init(activity, rlCollage);

        for (int i = 0; i < getMaxImageCount(); i++) {
            FrameLayout flImage = (FrameLayout) activity.getLayoutInflater().inflate(
                    R.layout.layout_collage_image, null, false);

            flImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onImageClick(view);
                }
            });

            rlCollage.addView(flImage);
        }

        OnSwipeTouchListener onSwipeTouchListener = new OnSwipeTouchListener(activity) {
            @Override
            public void onSwipeLeft() {
                changeCollageType(eType.ordinal() + 1);
            }

            @Override
            public void onSwipeRight() {
                changeCollageType(eType.ordinal() - 1);
            }
        };

        rlCollage.setGestureDetector(onSwipeTouchListener.getGestureDetector());
        rlCollage.setOnTouchListener(onSwipeTouchListener);

        imageFLViews.clear();
        for (int i = 0; i < rlCollage.getChildCount(); i++) {
            imageFLViews.add(rlCollage.getChildAt(i));
        }

        ViewTreeObserver vto = rlCollage.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                updateImageViews();

                ViewTreeObserver obs = rlCollage.getViewTreeObserver();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
            }

        });
    }

    public int getImageCount() {
        return imageFLViews.size();
    }
    public RelativeLayout getImageRL(int index) {
        return (RelativeLayout) imageFLViews.get(index);
    }

    private int getMaxImageCount() {
        int max_count = -1;
        for (CollageType type : CollageType.values()) {
            int count = getCollageConf(type).getPhotoCount();
            max_count = count > max_count ? count : max_count;
        }
        return max_count;
    }

    public void updateImageViews() {
        prepareImages();
        updateCollageLayoutSize();

        for (int i = 0; i < getActiveImageN(); i++) {
            updateViewPosition(i);
        }
    }

    private void updateCollageLayoutSize() {
        double aspect_ratio = 1.0;  // height / width   TODO: grab from config
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) rlCollage.getLayoutParams();
        // TODO: more smart sizing

        collageWidth = rlCollage.getWidth();
        layoutParams.width = collageWidth;
        layoutParams.height = (int)(aspect_ratio * collageWidth);
        rlCollage.setLayoutParams(layoutParams);
        rlCollage.setClipChildren(false);
    }

    public enum CollageType {
        Grid,
        CenterWithGridAround,
        Test1,
        Test2,
        Test3
    };


    private void prepareImages() {
        for (int i = 0; i < imageFLViews.size(); i++) {
            View iv = imageFLViews.get(i);

            if (i < getCollageConf().getPhotoCount()) {
                iv.setVisibility(View.VISIBLE);
            } else {
                iv.setVisibility(View.GONE);
            }
        }
    }

    private int getActiveImageN() {
        return Math.min(imageFLViews.size(), getCollageConf().getPhotoCount());
    }

    public void swapViews(View view1, View view2) {
        int i1 = imageFLViews.indexOf(view1);
        int i2 = imageFLViews.indexOf(view2);
        imageFLViews.set(i1, view2);
        imageFLViews.set(i2, view1);
        updateViewPosition(i1);
        updateViewPosition(i2);
        GoogleAnalyticsUtils.SendEvent(parentActivity,
                R.string.ga_event_category_swap_images,
                R.string.ga_event_action_swap_images,
                R.string.ga_event_label_swap_images);
    }

    public void updateViewPosition(int i) {
        if (i >= imageFLViews.size()) {
            Log.e(TAG, "Error: updateViewPosition: i = " + i + "size = " + imageFLViews.size());
            return;
        }
        final View v = imageFLViews.get(i);

        PhotoPosition pPhotoPos = getCollageConf().getPhotoPos(i);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
        params.height = (int) (collageWidth * pPhotoPos.getSize());
        params.width = (int) (collageWidth * pPhotoPos.getSize());
        params.leftMargin = (int) (collageWidth * pPhotoPos.getX());
        params.topMargin = (int) (collageWidth * pPhotoPos.getY());
        v.setLayoutParams(params);
    }

    public void updateViewPosition(View v) {
        updateViewPosition(imageFLViews.indexOf(v));
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

    public static void saveCollageOnDisk() {
        GoogleAnalyticsUtils.SendEvent(getInstance().parentActivity,
                R.string.ga_event_category_save_via_fab,
                R.string.ga_event_action_save_via_fab,
                R.string.ga_event_label_save_via_fab);

        CollageUtils.getInstance().saveCollageOnDisk();
    }

    public static void shareCollage() {
        GoogleAnalyticsUtils.SendEvent(getInstance().parentActivity,
                R.string.ga_event_category_share_via_fab,
                R.string.ga_event_action_share_via_fab,
                R.string.ga_event_label_share_via_fab);

        CollageUtils.getInstance().shareCollage();
    }

    public void DrawCollageTypeSelector(CollageTypeSelectorImageView ivSelector,
                                        int index, int size) {

//        !!!
        // TODO: move it from here!!!
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
        final int margin =
                parentActivity.getResources().getDimensionPixelSize(R.dimen.selector_margin);
        layoutParams.setMargins(margin, margin, margin, margin);
        ivSelector.setLayoutParams(layoutParams);
        if (index < 0 || index >= mCollages.size())
            return;
        final int selector_padding = size / 20;
        size -= selector_padding * 2;
        CollageConfig config = getCollageConf(CollageMaker.CollageType.values()[index]);
        for (int i = 0; i < config.getPhotoCount(); i++) {
            PhotoPosition photo_pos = config.getPhotoPos(i);
            int s_x = (int)(size * photo_pos.getX()) + selector_padding;
            int s_y = (int)(size * photo_pos.getY()) + selector_padding;
            int e_x = s_x + (int)(size * photo_pos.getSize());
            int e_y = s_y + (int)(size * photo_pos.getSize());
            ivSelector.AddLine(new CollageTypeSelectorImageView.Line(s_x, s_y, e_x, s_y));
            ivSelector.AddLine(new CollageTypeSelectorImageView.Line(e_x, s_y, e_x, e_y));
            ivSelector.AddLine(new CollageTypeSelectorImageView.Line(e_x, e_y, s_x, e_y));
            ivSelector.AddLine(new CollageTypeSelectorImageView.Line(s_x, e_y, s_x, s_y));
        }
    }

    private void onImageClick(View view) {
        collageAnimation.animateOnImageClick(view);
    }
}

