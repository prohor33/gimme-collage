package crystal.tech.gimmecollage.collagemaker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.RippleDrawable;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import crystal.tech.gimmecollage.analytics.GoogleAnalyticsUtils;
import crystal.tech.gimmecollage.app.Application;
import crystal.tech.gimmecollage.app.MainActivity;
import crystal.tech.gimmecollage.app.R;
import crystal.tech.gimmecollage.app.view.CollageTypeSelectorImageView;
import crystal.tech.gimmecollage.app.view.GestureRelativeLayout;
import crystal.tech.gimmecollage.utility.OnSwipeTouchListener;
import crystal.tech.gimmecollage.utility.MyDragEventListener;

/**
 * Created by prohor on 04/10/14.
 */
public class CollageMaker {

    private static final String TAG = "CollageMaker";

    private static CollageMaker instance;
    private CollageType eType = CollageType.Grid;
    private Map<CollageType, CollageConfig> mCollages;
    private GestureRelativeLayout rlCollage = null;
    private int collageWidth;   // relative layout width
    private Activity parentActivity = null;
    private MainActivity mainActivity = null;
    private CollageAnimation collageAnimation =  new CollageAnimation();
    private View rootView = null;

    // big_frame -> big_rl -> frame layout -> image_view + progress + back_image
    // this is fls
    private ArrayList<View> imageFLViews = new ArrayList<>();

    public enum CollageType {
        Grid,
        CenterWithGridAround,
        Test1,
        Test2,
        Test3
    };

    public static synchronized CollageMaker getInstance() {
        if (instance == null) {
            instance = new CollageMaker();
        }
        return instance;
    }

    private CollageMaker() {
        eType = CollageType.Grid; // by default
        mCollages = new HashMap<>();
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

        ImageStorage.updateImageCountInCollage();
        updateImageDataImpl();
    }

    public static void init(Activity collage_activity, View rootView) {
        getInstance().parentActivity = collage_activity;
        getInstance().rootView = rootView;
        CollageUtils.Init(getInstance().mainActivity, collage_activity, rootView);
        ImageStorage.putCollageActivity(getInstance().parentActivity);
        ImageStorage.putMainActivity(getInstance().mainActivity);
    }

    public void putMainActivity(MainActivity activity) {
        mainActivity = activity;
    }

    public static void initImageViews(View rootView) {
        getInstance().initImageViewsImpl(instance.parentActivity, rootView);
    }

    private void initImageViewsImpl(final Activity activity, View rootView) {
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
            ImageView iv =(ImageView) flImage.findViewById(R.id.ivMain);
            iv.setOnDragListener(new MyDragEventListener());

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
                updateCollageLayoutSize();
                updateImageViews();

                ViewTreeObserver obs = rlCollage.getViewTreeObserver();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
            }

        });

        ImageStorage.updateImageCountInCollage();
        updateImageDataImpl();
    }

    public int getAllImageViewCount() {
        return imageFLViews.size();
    }
    public RelativeLayout getImageFL(int index) {
        return (RelativeLayout) imageFLViews.get(index);
    }
    public int getIndexByFLView(View v) {
        return imageFLViews.indexOf(v);
    }

    public void updateImageViews() {
        prepareImages();

        for (int i = 0; i < getVisibleImageCount(); i++) {
            updateViewPosition(i);
        }
    }

    public static void updateImageData() {
        getInstance().updateImageDataImpl();
    }
    private void updateImageDataImpl() {
        for (int i = 0; i < getVisibleImageCount(); i++) {
            View v = imageFLViews.get(i);
            ImageView iv = (ImageView)v.findViewById(R.id.ivMain);
            ImageStorage.fillCollageView(iv, i);
        }
    }

    public int getVisibleImageCount() {
        return Math.min(imageFLViews.size(), getCollageConf().getPhotoCount());
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

    public Bitmap GenerateCollageImage() {
        final int target_size = 1024;
        Bitmap collageImage = Bitmap.createBitmap(target_size,
                target_size, Bitmap.Config.ARGB_8888);
        Canvas comboCanvas = new Canvas(collageImage);
        comboCanvas.drawColor(parentActivity.getResources().getColor(R.color.white));

        for (int i = 0; i < getVisibleImageCount(); i++) {
            FrameLayout fl = (FrameLayout) imageFLViews.get(i);
            ImageView iv = (ImageView) fl.findViewById(R.id.ivMain);
            PhotoPosition photoPos = getCollageConf().getPhotoPos(i);

            BitmapDrawable bitmapDrawable = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                RippleDrawable rippleDrawable = (RippleDrawable) iv.getDrawable();
                if (rippleDrawable == null)
                    continue;
                bitmapDrawable = (BitmapDrawable) rippleDrawable.getDrawable(0);
            } else {
                bitmapDrawable = (BitmapDrawable) iv.getDrawable();
            }
            if (bitmapDrawable == null) {
                Log.e(TAG, "BuildCollage: No bitmap not loaded, leave blank");
                continue;
            }
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

    // private members only ================

    private void updateCollageLayoutSize() {
        float aspect_ratio = 1.0f;  // height / width   TODO: grab from config

        FrameLayout parent = (FrameLayout) rlCollage.getParent();

        int collageHeight;
        int max_width = parent.getWidth();
        int max_height = parent.getHeight();
        if (max_height > aspect_ratio * max_width) {
            collageWidth = max_width;
            collageHeight = (int)(aspect_ratio * max_width);
        } else {
            collageHeight = max_height;
            collageWidth = (int)(max_height / aspect_ratio);
        }

        FrameLayout.LayoutParams layoutParams =
                (FrameLayout.LayoutParams) rlCollage.getLayoutParams();

        layoutParams.width = collageWidth;
        layoutParams.height = collageHeight;
        rlCollage.setLayoutParams(layoutParams);
    }

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

    private int getMaxImageCount() {
        int max_count = -1;
        for (CollageType type : CollageType.values()) {
            int count = getCollageConf(type).getPhotoCount();
            max_count = count > max_count ? count : max_count;
        }
        return max_count;
    }

    private void onImageClick(View view) {
        int index = imageFLViews.indexOf(view);
        ImageData imageData = ImageStorage.getCollageImage(index);
        if (imageData == null) {
            Application.moveRightDrawer(true);
        }
        collageAnimation.animateOnImageClick(view);
    }
}

