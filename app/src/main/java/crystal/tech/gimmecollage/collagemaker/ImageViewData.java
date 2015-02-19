package crystal.tech.gimmecollage.collagemaker;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.internal.pb;

import crystal.tech.gimmecollage.app.R;

/**
 * Created by prohor on 08/02/15.
 *
 * This class represents data of each collage ImageView (e.g. selected or not)
 */
public class ImageViewData {

    // big_frame -> big_rl -> frame layout -> image_view + progress + back_image
    // this is fls
    final public FrameLayout parentFL;
    final public int index;
    private ImageLoadingTarget loadingTarget = null;
    private boolean selected = false;
    private int loadedDataHash = -1;
    private ImageView imageView;
    private ProgressBar progressBar;

    ImageViewData(View v, int i) {
        parentFL = (FrameLayout) v;
        index = i;
        imageView = (ImageView) parentFL.findViewById(R.id.ivMain);
        progressBar = (ProgressBar) parentFL.findViewById(R.id.progressBar);
    }

    // constructor for pull ImageView
    // TODO: separate this two?
    ImageViewData(View v) {
        View parent = (View) v.getParent();
        imageView = (ImageView) parent.findViewById(R.id.drawerImageView);
        progressBar = (ProgressBar) parent.findViewById(R.id.progressBar);

        parentFL = null;
        index = -1;
    }

    public void putSelected(boolean sel) {
        selected = sel;
    }
    public boolean getSelected() {
        return selected;
    }

    public boolean isLoading() {
        return loadingTarget != null;
    }

    public boolean isAlreadyLoaded(String dataPath) {
        return dataPath.hashCode() == loadedDataHash;
    }

    public void startLoading(String data_path, ImageLoadingTarget loading_target) {
        loadedDataHash = data_path.hashCode();
        loadingTarget = loading_target;
        imageView.setImageDrawable(null);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void finishLoading() {
        if (loadingTarget != null)
            loadingTarget.cancel();
        loadingTarget = null;
        progressBar.setVisibility(View.GONE);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public static View getParentFLByIV(ImageView iv) {
        return (View) iv.getParent().getParent();   // it's FrameLayout, actually
    }

    public void clearView() {
        finishLoading();
        putSelected(false);
        loadedDataHash = -1;
    }
}
