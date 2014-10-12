package crystal.games.gimmecollage.collagemaker;

import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.HashMap;
import java.util.Map;

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
        if (!m_mCollages.containsKey(m_eType)) {
            Log.v(TAG, "Error: no such a collage: " + m_eType);
            return null;
        }
        return m_mCollages.get(m_eType);
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

    public enum CollageType {
        Grid,
        CenterWithGridAround
    };
    private CollageType m_eType;
    private Map<CollageType, CollageConfig> m_mCollages;
    private RelativeLayout m_rlCollage = null;

    private static CollageMaker m_pInstance;
    private CollageMaker() {
        m_eType = CollageType.Grid; // by default
        m_mCollages = new HashMap<CollageType, CollageConfig>();
        for (CollageType type : CollageType.values()) {
            m_mCollages.put(type, new CollageConfig(type));
        }
    }

    private void ArrangeImages() {
        for (int i = 0; i < m_rlCollage.getChildCount(); i++) {
            ImageView iv = (ImageView)m_rlCollage.getChildAt(i);

            PhotoPosition pPhotoPos = getCollageConf().getPhotoPos(i);
            int collage_padding = 10;
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)iv.getLayoutParams();
            params.height = pPhotoPos.getSize();
            params.width = pPhotoPos.getSize();
            params.leftMargin = pPhotoPos.getX() + collage_padding;
            params.topMargin = pPhotoPos.getY() + collage_padding;
            iv.setLayoutParams(params);
        }
    }
}

