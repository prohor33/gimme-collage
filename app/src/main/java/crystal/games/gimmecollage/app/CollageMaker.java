package crystal.games.gimmecollage.app;

import android.util.Log;

import java.util.ArrayList;
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

    class PhotoPosition {
        public PhotoPosition(float x, float y, float size) {
            this.m_dX = x;
            this.m_dY = y;
            this.m_dSize = size;
            m_dAngle = 0;  // by default
            m_iCollageSize = 100;  // in pixels
        }
        public PhotoPosition(float x, float y, float size, float angle) {
            this(x, y, size);
            this.m_dAngle = angle;
        }
        public void putCollageSize(int collage_size) {
            this.m_iCollageSize = collage_size;
        }
        public int getSize() {
            return (int)(m_iCollageSize * m_dSize);
        }
        public int getX() {
            return (int)(m_iCollageSize * m_dX);
        }
        public int getY() {
            return (int)(m_iCollageSize * m_dY);
        }
        public int getAngle() {
            return (int)(m_iCollageSize * m_dAngle);
        }

        private float m_dX;
        private float m_dY;
        private float m_dSize;
        private float m_dAngle;

        private int m_iCollageSize;
    }

    class CollageConfig {
        public CollageConfig(CollageType type) {
            switch (type) {
                case Grid: {
                    int grid_size_x = 4;
                    int grid_size_y = 4;
                    float size = 1.0f / grid_size_x;
                    for (int x = 0; x < grid_size_x; x++) {
                        for (int y = 0; y < grid_size_y; y++) {
                            m_vPhotoPos.add(new PhotoPosition(size * x, size * y, size));
                        }
                    }
                    break;
                }
                case CenterWithGridAround: {
                    int grid_size_x = 4;
                    int grid_size_y = 4;
                    float size = 1.0f / grid_size_x;
                    m_vPhotoPos.add(new PhotoPosition(size, size, size * (grid_size_x - 2)));
                    for (int x = 0; x < grid_size_x; x++) {
                        for (int y = 0; y < grid_size_y; y++) {
                            if (x == 0 || x == (grid_size_x - 1) ||
                                y == 0 || y == (grid_size_y - 1)) {
                                m_vPhotoPos.add(new PhotoPosition(size * x, size * y, size));
                            }
                        }
                    }
                    break;
                }
                default: {
                    Log.v(TAG, "Error: wrong collage type");
                    break;
                }
            }
        }

        public PhotoPosition getPhotoPos(int i) {
            if (i < 0 || i >= m_vPhotoPos.size()) {
                Log.v(TAG, "Error: No such a photo: i = " + i +
                        ", but size = " + m_vPhotoPos.size());
                return null;
            }
            return m_vPhotoPos.get(i);
        }

        public int getPhotoCount() {
            return m_vPhotoPos.size();
        }

        private ArrayList<PhotoPosition> m_vPhotoPos = new ArrayList<PhotoPosition>();
    }



    enum CollageType {
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

