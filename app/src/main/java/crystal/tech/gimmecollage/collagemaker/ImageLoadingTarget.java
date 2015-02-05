package crystal.tech.gimmecollage.collagemaker;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import crystal.tech.gimmecollage.app.R;

/**
 * Created by prohor on 31/01/15.
 */
public class ImageLoadingTarget  implements Target {
    private final String TAG = "ImageLoadingTarget";
    public String url;
    private ProgressBar progressBar;
    private ImageView imageView;
    private Activity activity;
    private boolean canceled = false;

    ImageLoadingTarget(ImageView image_view, ProgressBar progress_bar, Activity context) {
        imageView = image_view;
        progressBar = progress_bar;
        activity = context;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        if (canceled)
            return;

        if (bitmap != null) {
            onSuccess();

            ColorStateList imageColorlist =
                    activity.getResources().getColorStateList(R.color.image_colorlist);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imageView.setImageDrawable(new RippleDrawable(imageColorlist,
                        new BitmapDrawable(bitmap), null));
            } else {
                imageView.setImageDrawable(new BitmapDrawable(bitmap));
            }
        } else {
//                    loadDefaultMarker(listener);
            Log.e(TAG, "Error on load image: bitmap == null");
            onError();
        }
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        if (canceled)
            return;
        imageView.setImageDrawable(errorDrawable);
        onError();
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
    }

    public void cancel(){
        canceled = true;
    }

    private void onSuccess() {
        onEnd();
    }
    private void onError() {
        Log.e(TAG, "Error on load image");
        onEnd();
    }
    private void onEnd() {
        if (progressBar != null)
            progressBar.setVisibility(View.GONE);
        imageView.setTag(null);
    }
}
