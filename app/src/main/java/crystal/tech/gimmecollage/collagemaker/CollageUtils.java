package crystal.tech.gimmecollage.collagemaker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
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
import crystal.tech.gimmecollage.app.Utils;
import crystal.tech.gimmecollage.app.view.CollageTypeSelectorImageView;
import crystal.tech.gimmecollage.floating_action_btn.FloatingActionButton;
import crystal.tech.gimmecollage.utility.ColorPickerDialogFragment;
import crystal.tech.gimmecollage.utility.ImageLoader;
import crystal.tech.gimmecollage.utility.SimpleAsyncListener;
import crystal.tech.gimmecollage.utility.SimpleAsyncTask;

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
    private ImageLoader imageLoader = null;

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
        getInstance().imageLoader = new ImageLoader(main_activity);
    }

    public void buildCollage(final boolean share) {
        if (ImageStorage.getCollageImageCount() == 0) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            //Add images button clicked
                            Utils.spawnAddImagesActivity(mainActivity);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(collageActivity);
            builder.setMessage(mainActivity.getString(R.string.main_activity_dialog_no_images)).
                    setPositiveButton(mainActivity.getString(R.string.main_activity_dialog_add_images),
                            dialogClickListener)
                    .setNegativeButton(mainActivity.getString(R.string.main_activity_dialog_no),
                            dialogClickListener).show();

            return;
        }

        if (progressDialog == null)
            progressDialog = new ProgressDialog(collageActivity);
        progressDialog.setTitle(
                collageActivity.getString(R.string.main_activity_progress_just_a_second));
        progressDialog.setMessage(
                collageActivity.getString(share ?
                        R.string.main_activity_progress_generating_collage :
                        R.string.main_activity_progress_saving_collage));
        progressDialog.setCancelable(false);
        progressDialog.show();

        new SimpleAsyncTask(new SimpleAsyncListener() {
            File file;

            @Override
            public void onSuccess() {
                try {
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                } catch (final IllegalArgumentException e) {
                    // Handle or log or ignore
                } catch (final Exception e) {
                    // Handle or log or ignore
                } finally {
                    progressDialog = null;
                }

            if (share) {
                    openShareDialog(file);
                } else {
                    String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
                    extStorageDirectory += "/" + file.getName();
                    Toast.makeText(collageActivity,
                            collageActivity.getString(R.string.main_activity_toast_collage_saved_to)
                                    + extStorageDirectory,
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String error) {
                if (progressDialog != null)
                    progressDialog.dismiss();
            }

            @Override
            public Boolean doInBackground() {
                Bitmap bmpImage = CollageMaker.getInstance().GenerateCollageImage();
                if (bmpImage == null)
                    return false;

                String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
                OutputStream outStream;
                String temp = new String("collage");
                file = new File(extStorageDirectory, temp + ".png");
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
                    return false;
                }
                return true;
            }
        }).execute();
    }

    private void openShareDialog(File fileResult) {
        if (fileResult == null) {
            Log.v(TAG, "Null file pointer");
            return;
        }

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("image/png");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                collageActivity.getString(R.string.main_activity_collage_mail_subject));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                collageActivity.getString(R.string.main_activity_collage_mail_text));

        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileResult));
        collageActivity.startActivity(Intent.createChooser(sharingIntent,
                collageActivity.getString(R.string.main_activity_share_chooser)));
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
        ok_fab.setDrawable(collageActivity.getResources().getDrawable(R.drawable.ic_fab_ok));
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
        save_fab.setDrawable(collageActivity.getResources().getDrawable(R.drawable.ic_fab_save));
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
        share_fab.setDrawable(collageActivity.getResources().getDrawable(R.drawable.ic_fab_share));
        share_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CollageMaker.deselectAllViews();
                CollageMaker.shareCollage();
            }
        });

        FloatingActionButton trash_fab = (FloatingActionButton)rootView.findViewById(R.id.fabbutton_trash);
        trash_fab.setColor(collageActivity.getResources().getColor(R.color.fab_trash_color));
        trash_fab.setDrawable(collageActivity.getResources().getDrawable(R.drawable.ic_fab_trash));
        trash_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CollageMaker.deselectAllViews();
                ImageStorage.ClearAll();
                CollageMaker.clearViewsData();
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
                            R.layout.collage_type_selector_item, llTemplates, false);
            ivSelector.putIndex(i);
            // changing size inside
            CollageMaker.getInstance().drawCollageTypeSelector(ivSelector, i, selector_size);

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

        String dataPath;
        if (from_network) {
            // from instagram preview is really small
            boolean loadFullImage = isFullImageView(iv);
            dataPath = image.getDataPath(loadFullImage);
        } else {
            dataPath = String.valueOf(image.id);
        }

        if (viewData.isAlreadyLoaded(dataPath))
            return;

        if (viewData.isLoading())
            viewData.finishLoading();   // should load another instead


        ImageLoadingTarget target = new ImageLoadingTarget(viewData, image, mainActivity);
        viewData.startLoading(dataPath, target);

        if (from_network) {
            Picasso.with(mainActivity)
                    .load(dataPath)
                    .error(R.drawable.ic_load_failed)
                    .into(target);
        } else {
            imageLoader.loadThumbnail(image.id, iv, target);
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

        putBMPIntoImageViewImpl(rotateBitmap(bitmapDrawable.getBitmap(), angle), imageView);
    }

    public static boolean isFullImageView(ImageView iv) {
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
            Drawable drawable = imageView.getDrawable();
            RippleDrawable rippleDrawable;

            if (drawable instanceof RippleDrawable) {
                rippleDrawable = (RippleDrawable) drawable;
            } else {
                return null;
            }

            if (rippleDrawable == null)
                return null;
            return  (BitmapDrawable) rippleDrawable.getDrawable(0);
        } else {
            Drawable drawable = imageView.getDrawable();

            if (drawable instanceof BitmapDrawable) {
                return (BitmapDrawable) drawable;
            } else {
                return null;
            }
        }
    }

    public static void applyBMPIntoImageView(ImageView imageView, ImageData imageData, Bitmap bitmap) {
        getInstance().applyBMPIntoImageViewImpl(imageView, imageData, bitmap);
    }
    private void applyBMPIntoImageViewImpl(ImageView imageView, ImageData imageData, Bitmap bitmap) {
        // load from scratch
        bitmap = applyDataToBitmapImpl(imageData, bitmap);
        putBMPIntoImageViewImpl(bitmap, imageView);
    }

    public static Bitmap applyDataToBitmap(ImageData imageData, Bitmap bitmap) {
        return getInstance().applyDataToBitmapImpl(imageData, bitmap);
    }
    private Bitmap applyDataToBitmapImpl(ImageData imageData, Bitmap bitmap) {
        bitmap = rotateBitmap(bitmap, imageData.angle);

        // plus something else...

        return bitmap;
    }

    private Bitmap rotateBitmap(Bitmap bitmap, float angle) {
        if (angle != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);

            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                    matrix, true);
        }
        return bitmap;
    }

    private void putBMPIntoImageViewImpl(Bitmap bitmap, ImageView imageView) {
        ColorStateList imageColorList =
                collageActivity.getResources().getColorStateList(R.color.image_colorlist);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageView.setImageDrawable(new RippleDrawable(imageColorList,
                    new BitmapDrawable(bitmap), null));
        } else {
            imageView.setImageDrawable(new BitmapDrawable(bitmap));
        }
    }

    public static PointF applyMatrix(PointF p, Matrix transform) {
        // Create new float[] to hold the rotated coordinates
        float[] pts = new float[2];

        // Initialize the array with our Coordinate
        pts[0] = p.x;
        pts[1] = p.y;

        // Use the Matrix to map the points
        transform.mapPoints(pts);

        // NOTE: pts will be changed by transform.mapPoints call
        // after the call, pts will hold the new cooridnates

        // Now, create a new Point from our new coordinates
        PointF newPoint = new PointF(pts[0], pts[1]);

        // Return the new point
        return newPoint;
    }

    public static void showColorPickerDialog() {
        FragmentManager fm = getInstance().mainActivity.getFragmentManager();
        ColorPickerDialogFragment colorPickerDialog = new ColorPickerDialogFragment();
        colorPickerDialog.show(fm, "ColorPickerDialogTAG");
    }


}
