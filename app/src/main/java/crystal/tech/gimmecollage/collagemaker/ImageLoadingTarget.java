package crystal.tech.gimmecollage.collagemaker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by prohor on 31/01/15.
 */
public class ImageLoadingTarget  implements Target {
    private final String TAG = "ImageLoadingTarget";

    private ImageViewData viewData;
    private Activity activity;
    private boolean canceled = false;
    private ImageData imageData;

    ImageLoadingTarget(ImageViewData view_data, ImageData image_data, Activity context) {
        viewData = view_data;
        activity = context;
        imageData = image_data;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        if (canceled)
            return;

        if (bitmap != null) {
            onSuccess();
            CollageUtils.applyBMPIntoImageView(viewData.getImageView(), imageData, bitmap);
        } else {
            Log.e(TAG, "Error on load image: bitmap == null");
            onError();
        }
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        if (canceled)
            return;
        viewData.getImageView().setImageDrawable(errorDrawable);
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
        viewData.finishLoading();
    }
}
