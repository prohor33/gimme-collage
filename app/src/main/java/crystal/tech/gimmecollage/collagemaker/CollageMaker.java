package crystal.tech.gimmecollage.collagemaker;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.File;
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
    private Point collageSize = new Point();    // relative layout size
    private Activity parentActivity = null;
    private MainActivity mainActivity = null;
    private CollageAnimation collageAnimation =  new CollageAnimation();
    private View rootView = null;
    private ArrayList<ImageViewData> imageViewsData = new ArrayList<>();
    private int backgroundColor = R.color.color_picker_dialog_item0;

    public enum CollageType {
        FiveRectanglesTwoSidesAngle,
        FourRectanglesTwoSidesAngle,
        FourRectanglesAndSquare,
        FourSquaresAndRhombus,
        FourSquareTilt,
        FiveSquares,
        WithAngles1,
        Polygons1,
        Grid,
        Polygons2,
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
        eType = CollageType.FiveRectanglesTwoSidesAngle; // by default
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
        updateCollageLayoutSize();
        updateImageViews();
        CollageUtils.getInstance().updateCollageTypeSelectors();
        collageAnimation.onChangeCollageType();

        ImageStorage.updateImageCountInCollage();
        updateImageDataImpl();

        deselectAllViewsImpl();
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
            final FrameLayout flImage = (FrameLayout) activity.getLayoutInflater().inflate(
                    R.layout.collage_image_item, null, false);

            flImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onImageClick(view);
                }
            });
            final ImageView iv =(ImageView) flImage.findViewById(R.id.ivMain);
            iv.setOnDragListener(new MyDragEventListener());

            iv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int index = getIndexByFLView(flImage);
                    ImageData imageData = ImageStorage.getCollageImage(index);
                    if (imageData == null)
                        return false;   // empty image => do not swap


                    ClipData.Item itemSource =
                            new ClipData.Item(MyDragEventListener.FROM_COLLAGE_DRAG_SOURCE);
                    String[] mimeTypes = new String[1];
                    mimeTypes[0] = ClipDescription.MIMETYPE_TEXT_PLAIN;
                    ClipData dragData = new ClipData("blah", mimeTypes, itemSource);

                    ClipData.Item itemPullIndex =
                            new ClipData.Item(Integer.toString(getIndexByFLView(flImage)));
                    dragData.addItem(itemPullIndex);

                    // Instantiates the drag shadow builder.
                    View.DragShadowBuilder myShadow = new View.DragShadowBuilder(iv);

                    // Starts the drag

                    iv.startDrag(dragData,  // the data to be dragged
                            myShadow,  // the drag shadow builder
                            null,      // no need to use local data
                            0          // flags (not currently used, set to 0)
                    );

                    deselectAllViews();

                    return false;
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

        imageViewsData.clear();
        for (int i = 0; i < rlCollage.getChildCount(); i++) {
            imageViewsData.add(new ImageViewData(rlCollage.getChildAt(i), i));
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
        return imageViewsData.size();
    }
    public FrameLayout getImageFL(int index) {
        return (FrameLayout) imageViewsData.get(index).parentFL;
    }

    public static ImageViewData getViewDataByFLView(View v) {
        return getInstance().getViewDataByFLViewImpl(v);
    }
    private ImageViewData getViewDataByFLViewImpl(View v) {
        int index = getIndexByFLView(v);
        return imageViewsData.get(index);
    }

    public static int getIndexByFLView(View v) {
        return getInstance().getIndexByFLViewImpl(v);
    }
    private int getIndexByFLViewImpl(View v) {
        // TODO: improve data structure to make search faster?
        for (int i = 0; i < imageViewsData.size(); i++) {
            if (imageViewsData.get(i).parentFL == v)
                return i;
        }
        return -1;
    }

    public void updateImageViews() {
        prepareImages();

        for (int i = 0; i < getVisibleImageCount(); i++) {
            updateViewPosition(i);
        }

        updateViewsFrames();
    }

    public void updateViewsFrames() {
        for (int i = 0; i < getVisibleImageCount(); i++) {
            updateViewFrame(i);
        }
    }

    public static void updateImageData() {
        getInstance().updateImageDataImpl();
    }
    private void updateImageDataImpl() {
        for (int i = 0; i < getVisibleImageCount(); i++) {
            View v = imageViewsData.get(i).parentFL;
            ImageView iv = (ImageView) v.findViewById(R.id.ivMain);

            ImageData image = ImageStorage.getCollageImage(i);
            if (image == null) {
                iv.setImageDrawable(null);
                continue; // not so many photos available
            }
            ImageViewData viewData = getViewDataByFLViewImpl(ImageViewData.getParentFLByIV(iv));
            CollageUtils.fillView(iv, image, viewData, image.fromNetwork);
        }

        updateViewsFrames();
    }

    public int getVisibleImageCount() {
        return Math.min(imageViewsData.size(), getCollageConf().getPhotoCount());
    }

    public void updateViewPosition(int i) {
        if (i >= imageViewsData.size()) {
            Log.e(TAG, "Error: updateViewPosition: i = " + i + "size = " + imageViewsData.size());
            return;
        }
        final View v = imageViewsData.get(i).parentFL;

        PhotoPosition photoPos = getCollageConf().getPhotoPos(i);
        PointF p = new PointF(collageSize.x * photoPos.p.x, collageSize.y * photoPos.p.y);
        Point size = new Point((int) (collageSize.x * photoPos.size.x),
                (int) (collageSize.y * photoPos.size.y));
        PointF center = new PointF();
        center.set(p);
        center.offset(size.x / 2, size.y / 2);

        Matrix transform = new Matrix();
        transform.setRotate(photoPos.angle, p.x, p.y);
        PointF new_center = CollageUtils.applyMatrix(center, transform);
        Point start = new Point((int) (p.x + (new_center.x - center.x)),
                (int) (p.y + (new_center.y - center.y)));

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
        params.width = size.x;
        params.height = size.y;
        params.leftMargin = start.x;
        params.topMargin = start.y;
        params.rightMargin = collageSize.x - start.x - size.x;
        params.bottomMargin = collageSize.y - start.y - size.y;
        v.setLayoutParams(params);
        v.setRotation(photoPos.angle);

        // bug fix #46
        ImageView iv =(ImageView) v.findViewById(R.id.ivMain);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (photoPos.angle != 0) {
                iv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            } else {
                iv.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }
        }
    }

    public void updateViewFrame(int i) {
        if (i >= imageViewsData.size()) {
            Log.e(TAG, "Error: updateViewFrame: i = " + i + "size = " + imageViewsData.size());
            return;
        }
        final View v = imageViewsData.get(i).parentFL;
        PhotoPosition photoPos = getCollageConf().getPhotoPos(i);

        boolean empty_view = ImageStorage.getCollageImage(i) == null;
        boolean frame = photoPos.frame && !empty_view;

        View rippleView = v.findViewById(R.id.rippleView);
        int frame_width = frame ? mainActivity.getResources().
                getDimensionPixelOffset(R.dimen.collage_iv_back_padding) : 0;

        GradientDrawable backShape = (GradientDrawable)
                mainActivity.getResources().getDrawable(R.drawable.collage_image_back);
        backShape.setColor(mainActivity.getResources().getColor(getBackgroundColor()));

        rippleView.setBackgroundDrawable(frame ? backShape : null);
        rippleView.setPadding(frame_width, frame_width, frame_width, frame_width);
    }

    public void updateViewPosition(View v) {
        updateViewPosition(getIndexByFLView(v));
    }

    public static void saveCollageOnDisk() {
        GoogleAnalyticsUtils.SendEvent(getInstance().parentActivity,
                R.string.ga_event_category_save_via_fab,
                R.string.ga_event_action_save_via_fab,
                R.string.ga_event_label_save_via_fab);

        CollageUtils.getInstance().buildCollage(false);
    }

    public static void shareCollage() {
        GoogleAnalyticsUtils.SendEvent(getInstance().parentActivity,
                R.string.ga_event_category_share_via_fab,
                R.string.ga_event_action_share_via_fab,
                R.string.ga_event_label_share_via_fab);

        CollageUtils.getInstance().buildCollage(true);
    }

    public void drawCollageTypeSelector(CollageTypeSelectorImageView ivSelector,
                                        int index, int selector_size) {
        if (index < 0 || index >= mCollages.size())
            return;
        CollageConfig config = getCollageConf(CollageMaker.CollageType.values()[index]);
        float aspectRatio = config.getCollageAspectRatio();
        Point sel_size;
        if (mainActivity.getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT) {
            sel_size = new Point((int)(selector_size / aspectRatio), selector_size);
        } else {
            sel_size = new Point(selector_size, (int) (selector_size * aspectRatio));
        }


        LinearLayout.LayoutParams layoutParams =
                (LinearLayout.LayoutParams) ivSelector.getLayoutParams();
        layoutParams.width = sel_size.x;
        layoutParams.height = sel_size.y;
        ivSelector.setLayoutParams(layoutParams);

        final int selector_padding = sel_size.y / 20;
        sel_size.x -= selector_padding * 2;
        sel_size.y -= selector_padding * 2;
        Matrix transform = new Matrix();
        for (int i = 0; i < config.getPhotoCount(); i++) {
            PhotoPosition photo_pos = config.getPhotoPos(i);

            PointF p = new PointF();
            p.set(photo_pos.p);
            p.x *= sel_size.x;
            p.y *= sel_size.y;
            PointF size = new PointF();
            size.set(photo_pos.size);
            size.x *= sel_size.x;
            size.y *= sel_size.y;

            // rotate point
            transform.setRotate(photo_pos.angle, p.x, p.y);
            PointF p1 = CollageUtils.applyMatrix(p, transform);
            PointF p2 = CollageUtils.applyMatrix(new PointF(p.x + size.x, p.y), transform);
            PointF p3 = CollageUtils.applyMatrix(new PointF(p.x + size.x,
                    p.y + size.y), transform);
            PointF p4 = CollageUtils.applyMatrix(new PointF(p.x, p.y + size.y), transform);

            p1.x += selector_padding;
            p1.y += selector_padding;
            p2.x += selector_padding;
            p2.y += selector_padding;
            p3.x += selector_padding;
            p3.y += selector_padding;
            p4.x += selector_padding;
            p4.y += selector_padding;

            ivSelector.AddLine(new CollageTypeSelectorImageView.Line(p1, p2));
            ivSelector.AddLine(new CollageTypeSelectorImageView.Line(p2, p3));
            ivSelector.AddLine(new CollageTypeSelectorImageView.Line(p3, p4));
            ivSelector.AddLine(new CollageTypeSelectorImageView.Line(p4, p1));
        }
    }

    public Bitmap GenerateCollageImage() {
        float aspect_ratio = getCollageConf().getCollageAspectRatio();
        final int bmp_pxl_size = 1024;
        final Point target_size = new Point(bmp_pxl_size, (int)(bmp_pxl_size * aspect_ratio));
        Bitmap collageImage = Bitmap.createBitmap(target_size.x,
                target_size.y, Bitmap.Config.ARGB_8888);
        Canvas comboCanvas = new Canvas(collageImage);
        comboCanvas.drawColor(parentActivity.getResources().getColor(backgroundColor));

        for (int i = 0; i < getVisibleImageCount(); i++) {
            FrameLayout fl = imageViewsData.get(i).parentFL;
            ImageView iv = (ImageView) fl.findViewById(R.id.ivMain);
            PhotoPosition photoPos = getCollageConf().getPhotoPos(i);
            ImageData imageData = ImageStorage.getCollageImage(i);
            if (imageData == null)
                continue;

            BitmapDrawable bitmapDrawable = CollageUtils.getBMPFromImageView(iv);
            if (bitmapDrawable == null || bitmapDrawable.getBitmap() == null) {
                Log.d(TAG, "BuildCollage: No bitmap are loaded, leave blank");
                continue;
            }
            Bitmap bitmap = bitmapDrawable.getBitmap();

            if (!imageData.fromNetwork) {
                // gallery
                if (CollageUtils.isFullImageView(iv)) {
                    // reload image with better quality
                    File imgFile = new  File(imageData.dataPath);
                    if(imgFile.exists()) {
                        bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        bitmap = CollageUtils.applyDataToBitmap(imageData, bitmap);
                        Log.d(TAG, "Load image with better quality");
                    }
                }
            }

            Point place_size = new Point((int)(target_size.x * photoPos.size.x),
                    (int)(target_size.y * photoPos.size.y));
            float place_aspect = (float)place_size.y / place_size.x;
            float image_aspect = (float)bitmap.getHeight() / bitmap.getWidth();

            Point img_t_size = new Point();
            if (place_aspect > image_aspect) {
                img_t_size.x = (int)(bitmap.getHeight() / place_aspect);
                img_t_size.y = bitmap.getHeight();
            } else {
                img_t_size.x = bitmap.getWidth();
                img_t_size.y = (int)(bitmap.getWidth() * place_aspect);
            }

            // crop square in center
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, img_t_size.x, img_t_size.y);
            final int frame_w = photoPos.frame ? mainActivity.getResources().
                    getDimensionPixelOffset(R.dimen.collage_iv_back_padding) : 0;
            bitmap = Bitmap.createScaledBitmap(bitmap, place_size.x - 2 * frame_w,
                    place_size.y - 2 * frame_w, true);

            if (photoPos.frame) {
                // prepare frame bitmap
                Bitmap frameImage = Bitmap.createBitmap(place_size.x, place_size.y, Bitmap.Config.ARGB_8888);
                Canvas frameCanvas = new Canvas(frameImage);
                frameCanvas.drawColor(parentActivity.getResources().getColor(backgroundColor));
                frameCanvas.drawBitmap(bitmap, frame_w, frame_w, new Paint(Paint.FILTER_BITMAP_FLAG));
                bitmap = frameImage;
            }

            PointF p = new PointF(target_size.x * photoPos.p.x, target_size.y * photoPos.p.y);
            Matrix transform = new Matrix();
            transform.setTranslate(p.x, p.y);
            transform.postRotate(photoPos.angle, p.x, p.y);
            comboCanvas.drawBitmap(bitmap, transform, new Paint(Paint.FILTER_BITMAP_FLAG));
        }

        Log.v(TAG, "Collage is successfully generated!");

        return collageImage;
    }

    public static CollageAnimation getCollageAnimation() {
        return getInstance().collageAnimation;
    }

    public static void putBackgroundColor(int id) {
        getInstance().backgroundColor = id;
        getInstance().updateBackgroundColor();
    }
    public static int getBackgroundColor() {
        return getInstance().backgroundColor;
    }

    public static void clearViewsData() {
        getInstance().clearViewsDataImpl();
    }
    private void clearViewsDataImpl() {
        for (ImageViewData data : imageViewsData) {
            data.clearView();
        }
    }

    // private members only ================

    private void updateCollageLayoutSize() {
        float aspect_ratio = getCollageConf().getCollageAspectRatio();

        FrameLayout parent = (FrameLayout) rlCollage.getParent();

        int max_width = parent.getWidth();
        int max_height = parent.getHeight();
        if (max_height > aspect_ratio * max_width) {
            collageSize.x = max_width;
            collageSize.y = (int)(aspect_ratio * max_width);
        } else {
            collageSize.y = max_height;
            collageSize.x = (int)(max_height / aspect_ratio);
        }

        FrameLayout.LayoutParams layoutParams =
                (FrameLayout.LayoutParams) rlCollage.getLayoutParams();

        layoutParams.width = collageSize.x;
        layoutParams.height = collageSize.y;
        rlCollage.setLayoutParams(layoutParams);
    }

    private void prepareImages() {
        for (int i = 0; i < imageViewsData.size(); i++) {
            View iv = imageViewsData.get(i).parentFL;

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
        ImageViewData viewData = getViewDataByFLView(view);
        ImageData imageData = ImageStorage.getCollageImage(viewData.index);

        if (imageData == null) {
            // empty collage view
            Application.moveRightDrawer(true);
            return;
        }

        if (CollageUtils.getImageActionButtons().getDisabled())
            CollageUtils.getImageActionButtons().onRotateLeft((FrameLayout) view);

        if (viewData.getSelected()) {
            deselectAllViews();
        } else {
            deselectAllViews();
            collageAnimation.animateOnImageClick(view);
            CollageUtils.getImageActionButtons().showOnView(view);
            viewData.putSelected(true);
        }
    }

    public static void deselectAllViews() {
        getInstance().deselectAllViewsImpl();
    }
    private void deselectAllViewsImpl() {
        collageAnimation.dischargeAllSelection();
        for (ImageViewData viewData : imageViewsData) {
            viewData.putSelected(false);
        }
    }

    private void updateBackgroundColor() {
        GradientDrawable gradientDrawable = (GradientDrawable)rlCollage.getBackground();
        gradientDrawable.setColor(parentActivity.getResources().getColor(backgroundColor));
        updateViewsFrames();
    }
}

