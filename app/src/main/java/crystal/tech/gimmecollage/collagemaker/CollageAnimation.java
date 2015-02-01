package crystal.tech.gimmecollage.collagemaker;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.widget.RelativeLayout;

import crystal.tech.gimmecollage.app.R;

/**
 * Created by prohor on 24/01/15.
 */
public class CollageAnimation {

    private Activity parentActivity = null;
    private RelativeLayout rlCollage = null;

    public void init(Activity activity, RelativeLayout relativeLayoutCollage) {
        rlCollage = relativeLayoutCollage;
        parentActivity = activity;
    }

    public void animateOnImageClick(final View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (int i = 0; i < rlCollage.getChildCount(); i++) {
                rlCollage.getChildAt(i).animate().translationZ(0);
            }

            final float clickElevation =
                    parentActivity.getResources().getDimension(R.dimen.collage_iv_elevation);
            view.animate().translationZ(clickElevation);
        }
    }

    public void onChangeCollageType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (int i = 0; i < rlCollage.getChildCount(); i++) {
                rlCollage.getChildAt(i).setTranslationZ(0);
            }
        }
    }
}
