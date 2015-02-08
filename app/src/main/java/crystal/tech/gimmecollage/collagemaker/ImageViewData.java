package crystal.tech.gimmecollage.collagemaker;

import android.view.View;

/**
 * Created by prohor on 08/02/15.
 *
 * This class represents data of each collage ImageView (e.g. selected or not)
 */
public class ImageViewData {

    // big_frame -> big_rl -> frame layout -> image_view + progress + back_image
    // this is fls
    public View view;
    public int index;
    private boolean selected = false;

    ImageViewData(View v, int i) {
        view = v;
        index = i;
    }

    public void putSelected(boolean sel) {
        selected = sel;
    }
    public boolean getSelected() {
        return selected;
    }
}
