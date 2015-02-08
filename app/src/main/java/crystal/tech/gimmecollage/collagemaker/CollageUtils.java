package crystal.tech.gimmecollage.collagemaker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import crystal.tech.gimmecollage.app.MainActivity;
import crystal.tech.gimmecollage.app.R;
import crystal.tech.gimmecollage.app.view.CollageTypeSelectorImageView;
import crystal.tech.gimmecollage.floating_action_btn.FloatingActionButton;

/**
 * Created by prohor on 26/01/15.
 */
public class CollageUtils {

    private final String TAG = "CollageUtils";

    private static CollageUtils instance;
    private boolean fabCollapsed = true;
    private Activity collageActivity = null;
    private MainActivity mainActivity = null;
    private View rootView = null;
    private ProgressDialog progressDialog = null;
    private ImageActionButtons imageActionButtons = new ImageActionButtons();

    public static synchronized CollageUtils getInstance() {
        if (instance == null) {
            instance = new CollageUtils();
        }
        return instance;
    }

    public static void Init(MainActivity main_activity, Activity collage_activity,
                            View root_view) {
        getInstance().collageActivity = collage_activity;
        getInstance().rootView = root_view;
        getInstance().mainActivity = main_activity;
        getInstance().imageActionButtons.init(collage_activity, root_view);
    }

    public interface FileSaveCallback {
        void onSuccess(File file_result);

        void onError();

        public static class EmptyFileSaveCallback implements FileSaveCallback {

            @Override public void onSuccess(File file_result) {
            }

            @Override public void onError() {
            }
        }
    }

    public void shareCollage() {
        if (progressDialog == null)
            progressDialog = new ProgressDialog(collageActivity);
        progressDialog.setTitle("Just a second");
        progressDialog.setMessage("Generating collage for you...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Bitmap imgCollage = CollageMaker.getInstance().GenerateCollageImage();
        // Save file on disk and open share dialog
        new SaveFileTask().with(new FileSaveCallback() {
            @Override
            public void onSuccess(File file_result) {
                openShareDialog(file_result);
            }

            @Override
            public void onError() {

            }
        }).execute(imgCollage);
    }

    private class SaveFileTask extends AsyncTask<Bitmap, Void, File> {

        FileSaveCallback callback = null;

        public SaveFileTask with(FileSaveCallback cb) {
            callback = cb;
            return this;
        }

        protected File doInBackground(Bitmap... bmpImages) {
            Bitmap bmpImage = bmpImages[0];
            Log.v(TAG, "Start to save a file");

            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            OutputStream outStream = null;
            String temp = new String("collage");
            File file = new File(extStorageDirectory, temp + ".png");
            if (file.exists()) {
                file.delete();
                file = new File(extStorageDirectory, temp + ".png");
            }

            try {
                outStream = new FileOutputStream(file);
                bmpImage.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                outStream.flush();
                outStream.close();
            } catch (Exception e) {
                Log.v(TAG, "Error saving file: " + e.toString());
                return null;
            }
            return file;
        }

        protected void onPostExecute(File fileResult) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (fileResult == null) {
                Log.v(TAG, "Error saving file");
                if (callback != null)
                    callback.onError();
            } else {
                Log.v(TAG, "File successfully saved");
                if (callback != null)
                    callback.onSuccess(fileResult);
            }
        }
    }

    private void openShareDialog(File fileResult) {
        if (fileResult == null) {
            Log.v(TAG, "Null file pointer");
            return;
        }

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("image/png");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Little candy");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                "Look! I have nice photo collage here, special for you!\nVia GimmeCollage");

        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileResult));
        collageActivity.startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    public void saveCollageOnDisk() {
        if (progressDialog == null)
            progressDialog = new ProgressDialog(collageActivity);
        progressDialog.setTitle("Just a second");
        progressDialog.setMessage("Saving collage...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Bitmap imgCollage = CollageMaker.getInstance().GenerateCollageImage();
        // Save file on disk and open share dialog
        new SaveFileTask().with(new FileSaveCallback() {
            @Override
            public void onSuccess(File file_result) {
                String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
                extStorageDirectory += "/" + file_result.getName();
                Toast.makeText(collageActivity, "Collage saved to " + extStorageDirectory,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError() {

            }
        }).execute(imgCollage);
    }

    public static void putFabCollapsed(boolean x) {
        getInstance().fabCollapsed = x;
    }
    public static boolean getFabCollapsed() {
        return  getInstance().fabCollapsed;
    }

    public static void addFloatingActionButtons(View rootView) {
        getInstance().addFloatingActionButtonsImpl(rootView);
    }
    private void addFloatingActionButtonsImpl(final View rootView) {
        final FloatingActionButton ok_fab = (FloatingActionButton)rootView.findViewById(R.id.fabbutton0);
        ok_fab.setColor(collageActivity.getResources().getColor(R.color.design_blue));
        ok_fab.setDrawable(collageActivity.getResources().getDrawable(R.drawable.ic_action_accept));
        ok_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CollageMaker.deselectAllViews();
                FloatingActionButton mFab1 = (FloatingActionButton) rootView.findViewById(R.id.fabbutton1);
                mFab1.hide(!mFab1.getHidden());
                FloatingActionButton mFab2 = (FloatingActionButton) rootView.findViewById(R.id.fabbutton2);
                mFab2.hide(!mFab2.getHidden());
            }
        });

        FloatingActionButton save_fab = (FloatingActionButton)rootView.findViewById(R.id.fabbutton1);
        save_fab.setParentFAB(ok_fab);
        save_fab.setColor(collageActivity.getResources().getColor(R.color.design_yellow));    // maroon
        save_fab.setDrawable(collageActivity.getResources().getDrawable(R.drawable.ic_action_save));
        save_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CollageMaker.deselectAllViews();
                CollageMaker.saveCollageOnDisk();
            }
        });

        FloatingActionButton share_fab = (FloatingActionButton)rootView.findViewById(R.id.fabbutton2);
        share_fab.setParentFAB(ok_fab);
        share_fab.setColor(collageActivity.getResources().getColor(R.color.design_red));
        share_fab.setDrawable(collageActivity.getResources().getDrawable(R.drawable.ic_action_share));
        share_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CollageMaker.deselectAllViews();
                CollageMaker.shareCollage();
            }
        });
    }

    public static void addCollageTypeSelectorLayout(View rootView) {
        getInstance().addCollageTypeSelectorLayoutImpl(rootView);
    }
    private void addCollageTypeSelectorLayoutImpl(final View rootView) {
        final LinearLayout llTemplates = (LinearLayout) rootView.findViewById(R.id.layoutTemplates);
        for (int i = 0; i < CollageMaker.CollageType.values().length; i++) {
            final int selector_size =
                    collageActivity.getResources().getDimensionPixelSize(R.dimen.selector_size);

            CollageTypeSelectorImageView ivSelector =
                    (CollageTypeSelectorImageView) collageActivity.getLayoutInflater().inflate(
                            R.layout.layout_collage_type_selector, llTemplates, false);
            ivSelector.putIndex(i);
            CollageMaker.getInstance().DrawCollageTypeSelector(ivSelector, i, selector_size);

            ivSelector.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CollageTypeSelectorImageView iv = (CollageTypeSelectorImageView) v;
                    int index = iv.getIndex();
                    CollageMaker.getInstance().changeCollageType(index);
                }
            });

            llTemplates.addView(ivSelector);
        }
    }

    public void updateCollageTypeSelectors(){
        final LinearLayout llTemplates = (LinearLayout) rootView.findViewById(R.id.layoutTemplates);
        // reset all the others
        for (int i = 0; i < llTemplates.getChildCount(); i++) {
            ImageView iv = (ImageView)llTemplates.getChildAt(i);
            iv.setColorFilter(0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (i == CollageMaker.getInstance().getCollageTypeIndex()) {
                    final float clickElevation =
                            collageActivity.getResources().getDimension(R.dimen.selector_elevation);
                    iv.animate().translationZ(clickElevation);
                    continue;
                }
                iv.setTranslationZ(0);
            }
        }
    }

    public static ImageActionButtons getImageActionButtons() {
        return getInstance().imageActionButtons;
    }

    public static void fillView(final ImageView iv, ImageData image, ImageViewData viewData,
                                boolean from_network) {
        getInstance().fillViewImpl(iv, image, viewData, from_network);
    }
    private void fillViewImpl(final ImageView iv, ImageData image, ImageViewData viewData,
                              boolean from_network) {

        // hack for pull image
        if (viewData == null) {
            if (iv.getTag() != null) {
                viewData = (ImageViewData) iv.getTag();
            } else {
                viewData = new ImageViewData(iv);
                iv.setTag(viewData);
            }
        }

        boolean loadFullImage = isFullImageView(iv);
        String dataPath = image.getDataPath(loadFullImage);

        if (viewData.isLoading()) {
            if (viewData.isAlreadyLoaded(dataPath))
                return;
            viewData.finishLoading();
        }

        ImageLoadingTarget target = new ImageLoadingTarget(viewData, image, mainActivity);
        viewData.startLoading(dataPath, target);

        if (from_network) {
            Picasso.with(mainActivity)
                    .load(dataPath)
                    .error(R.drawable.ic_content_problem)
                    .into(target);
        } else {
            Picasso.with(mainActivity)
                    .load(new File(dataPath))
                    .error(R.drawable.ic_content_problem)
                    .into(target);
        }
    }

    public static void rotateImage(ImageView imageView, ImageData imageData, float angle) {
        getInstance().rotateImageImpl(imageView, imageData, angle);
    }
    private void rotateImageImpl(ImageView imageView, ImageData imageData, float angle) {
        BitmapDrawable bitmapDrawable = getBMPFromImageViewImpl(imageView);
        if (bitmapDrawable == null) {
            Log.e(TAG, "rotateImageImpl: No bitmap not loaded, do nothing");
            return;
        }
        imageData.angle += angle;

        putBMPIntoImageViewImpl(imageView, imageData, bitmapDrawable.getBitmap());
    }

    private boolean isFullImageView(ImageView iv) {
        // standard gallery thumbnail 320x240 or 240x320
        int image_view_square = iv.getWidth() * iv.getHeight();
        View grandParent = (View)iv.getParent().getParent();
        if (grandParent instanceof FrameLayout) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)grandParent.getLayoutParams();
            image_view_square = params.width * params.height;
//                Log.d(TAG, "image_view_square = " + image_view_square);
        }

        final int max_thumbnail_square = 60000;
        return image_view_square > max_thumbnail_square;
    }

    public static BitmapDrawable getBMPFromImageView(ImageView imageView) {
        return getInstance().getBMPFromImageViewImpl(imageView);
    }
    private BitmapDrawable getBMPFromImageViewImpl(ImageView imageView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            RippleDrawable rippleDrawable = (RippleDrawable) imageView.getDrawable();
            if (rippleDrawable == null)
                return null;
            return  (BitmapDrawable) rippleDrawable.getDrawable(0);
        } else {
            return  (BitmapDrawable) imageView.getDrawable();
        }
    }

    public static void putBMPIntoImageView(ImageView imageView, ImageData imageData, Bitmap bitmap) {
        getInstance().putBMPIntoImageViewImpl(imageView, imageData, bitmap);
    }
    private void putBMPIntoImageViewImpl(ImageView imageView, ImageData imageData, Bitmap bitmap) {

        if (imageData.angle != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(imageData.angle);

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                    matrix, true);
        }

        ColorStateList imageColorList =
                collageActivity.getResources().getColorStateList(R.color.image_colorlist);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageView.setImageDrawable(new RippleDrawable(imageColorList,
                    new BitmapDrawable(bitmap), null));
        } else {
            imageView.setImageDrawable(new BitmapDrawable(bitmap));
        }
    }
}
