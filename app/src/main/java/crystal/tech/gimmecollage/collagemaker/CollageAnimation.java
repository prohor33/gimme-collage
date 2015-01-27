package crystal.tech.gimmecollage.collagemaker;

import android.app.Activity;
import android.view.View;
import android.widget.RelativeLayout;

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
        for (int i = 0; i < rlCollage.getChildCount(); i++) {
            rlCollage.getChildAt(i).animate().translationZ(0);
        }

        final float clickElevation = 35.0f;
        view.animate().translationZ(clickElevation);
    }

    public void onChangeCollageType() {
        for (int i = 0; i < rlCollage.getChildCount(); i++) {
            rlCollage.getChildAt(i).setTranslationZ(0);
        }
    }
}
