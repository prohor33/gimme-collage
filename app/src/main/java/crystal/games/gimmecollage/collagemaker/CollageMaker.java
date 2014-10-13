package crystal.games.gimmecollage.collagemaker;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import crystal.games.gimmecollage.app.MainActivity;

/**
 * Created by prohor on 04/10/14.
 */
public class CollageMaker {

    private static final String TAG = "CollageMaker";

    // Singletone pattern
    public static synchronized CollageMaker getInstance() {
        if (m_pInstance == null) {
            m_pInstance = new CollageMaker();
        }
        return m_pInstance;
    }

    public CollageConfig getCollageConf() {
        return getCollageConf(m_eType);
    }
    public CollageConfig getCollageConf(CollageType eType) {
        if (!m_mCollages.containsKey(eType)) {
            Log.v(TAG, "Error: no such a collage: " + eType);
            return null;
        }
        return m_mCollages.get(eType);
    }

    public void putCollageType(CollageType type) {
        m_eType = type;
    }

    public void moveToTheOtherCollageType(int type_index) {
        if (type_index < 0 || type_index >= CollageType.values().length) {
            Log.v(TAG, "Error: wrong collage type index = " + type_index);
            return;
        }
        moveToTheOtherCollageType(CollageMaker.CollageType.values()[type_index]);
    }

    public void moveToTheOtherCollageType(CollageType type) {
        putCollageType(type);
        ArrangeImages();
    }

    public void setCollageLayout(RelativeLayout collage_layout) {
        m_rlCollage = collage_layout;
    }

    public void putCollageSize(int collage_size) {
        this.m_iCollageSize = collage_size;
    }

    public int getMaxImageCount() {
        int max_count = -1;
        for (CollageType type : CollageType.values()) {
            int count = getCollageConf(type).getPhotoCount();
            max_count = count > max_count ? count : max_count;
        }
        return max_count;
    }

    public enum CollageType {
        Grid,
        CenterWithGridAround
    };


    private CollageType m_eType;
    private Map<CollageType, CollageConfig> m_mCollages;
    private RelativeLayout m_rlCollage = null;
    private int m_iCollageSize;

    private static CollageMaker m_pInstance;
    private CollageMaker() {
        m_eType = CollageType.Grid; // by default
        m_mCollages = new HashMap<CollageType, CollageConfig>();
        for (CollageType type : CollageType.values()) {
            m_mCollages.put(type, new CollageConfig(type));
        }

        this.m_iCollageSize = 100;  // in pixels
    }

    private void ArrangeImages() {
        if (getCollageConf().getPhotoCount() > m_rlCollage.getChildCount()) {
            Log.v(TAG, "Error: too few image views");
            return;
        }

        for (int i = 0; i < m_rlCollage.getChildCount(); i++) {
            ImageView iv = (ImageView)m_rlCollage.getChildAt(i);

            if (i < getCollageConf().getPhotoCount()) {
                PhotoPosition pPhotoPos = getCollageConf().getPhotoPos(i);
                int collage_padding = 10;
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) iv.getLayoutParams();
                params.height = (int) (m_iCollageSize * pPhotoPos.getSize());
                params.width = (int) (m_iCollageSize * pPhotoPos.getSize());
                params.leftMargin = (int) (m_iCollageSize * pPhotoPos.getX()) + collage_padding;
                params.topMargin = (int) (m_iCollageSize * pPhotoPos.getY()) + collage_padding;

                iv.setLayoutParams(params);
                iv.setVisibility(ImageView.VISIBLE);
            } else {
                iv.setVisibility(ImageView.GONE);
            }
        }
    }

//    private class SaveFileTask extends AsyncTask<Float, Void, Void> {
//
//        protected Void doInBackground(Float... speeds) {
//            Float speed = speeds[0];
//            Prepare();
//        }
//        protected Void onPostExecute() {
//
//        }
//
//        private void Prepare() {
//            for (int i = 0; i < m_rlCollage.getChildCount(); i++) {
//                ImageView iv = (ImageView)m_rlCollage.getChildAt(i);
//
//                if (i < getCollageConf().getPhotoCount()) {
//                    iv.setVisibility(ImageView.VISIBLE);
//                } else {
//                    iv.setVisibility(ImageView.GONE);
//                }
//            }
//        }
//        private boolean updateImageViews(Float speed) {
//            for (int i = 0; i < m_rlCollage.getChildCount() &&
//                    i < getCollageConf().getPhotoCount(); i++) {
//                ImageView iv = (ImageView)m_rlCollage.getChildAt(i);
//
//                PhotoPosition pPhotoPos = getCollageConf().getPhotoPos(i);
//                int collage_padding = 10;
//                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) iv.getLayoutParams();
//                int target_h = (int) (m_iCollageSize * pPhotoPos.getSize());
//                int target_w = (int) (m_iCollageSize * pPhotoPos.getSize());
//                int target_left = (int) (m_iCollageSize * pPhotoPos.getX()) + collage_padding;
//                int target_top = (int) (m_iCollageSize * pPhotoPos.getY()) + collage_padding;
//
//
//                iv.setLayoutParams(params);
//            }
//        }
//    }
}

