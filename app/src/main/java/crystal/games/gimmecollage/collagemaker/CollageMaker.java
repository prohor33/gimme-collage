package crystal.games.gimmecollage.collagemaker;

import android.util.Log;

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

    public enum CollageType {
        Grid,
        CenterWithGridAround
    };
    private CollageType m_eType;
    private Map<CollageType, CollageConfig> m_mCollages;

    private static CollageMaker m_pInstance;
    private CollageMaker() {
        m_eType = CollageType.Grid; // by default
        m_mCollages = new HashMap<CollageType, CollageConfig>();
        for (CollageType type : CollageType.values()) {
            m_mCollages.put(type, new CollageConfig(type));
        }
    }
}

