package crystal.tech.gimmecollage.collagemaker;

import android.util.Log;

import java.util.ArrayList;

import crystal.tech.gimmecollage.collagemaker.CollageMaker.CollageType;

/**
 * Created by prohor on 05/10/14.
 */

public class CollageConfig {
    private static final String TAG = "CollageConfig";

    public CollageConfig(CollageType type) {
        switch (type) {
            case Grid: {
                int grid_size_x = 4;
                int grid_size_y = 4;
                float padding = 0.03f;
                float size = (1.0f - (grid_size_x + 1) * padding) / (float)grid_size_x;
                for (int x = 0; x < grid_size_x; x++) {
                    for (int y = 0; y < grid_size_y; y++) {
                        m_vPhotoPos.add(new PhotoPosition(size * x + padding * (x + 1),
                                size * y + padding * (y + 1), size));
                    }
                }
                break;
            }
            case CenterWithGridAround: {
                int grid_size_x = 4;
                int grid_size_y = 4;
                float padding = 0.03f;
                float size = (1.0f - (grid_size_x + 1) * padding) / (float)grid_size_x;
                m_vPhotoPos.add(new PhotoPosition(size + 2 * padding, size + 2 * padding,
                        1.0f - 2 * (size + 2 * padding)));
                for (int x = 0; x < grid_size_x; x++) {
                    for (int y = 0; y < grid_size_y; y++) {
                        if (x == 0 || x == (grid_size_x - 1) ||
                                y == 0 || y == (grid_size_y - 1)) {
                            m_vPhotoPos.add(new PhotoPosition(size * x + padding * (x + 1),
                                    size * y + padding * (y + 1), size));
                        }
                    }
                }
                break;
            }
            case Test1: {
                int grid_size_x = 4;
                int grid_size_y = 4;
                float padding = 0.03f;
                float size = (1.0f - (grid_size_x + 1) * padding) / (float)grid_size_x;
                m_vPhotoPos.add(new PhotoPosition(padding, padding,
                        1.0f - (size + 3 * padding)));
                for (int x = 0; x < grid_size_x; x++) {
                    for (int y = 0; y < grid_size_y; y++) {
                        if (x == (grid_size_x - 1) || y == (grid_size_y - 1)) {
                            m_vPhotoPos.add(new PhotoPosition(size * x + padding * (x + 1),
                                    size * y + padding * (y + 1), size));
                        }
                    }
                }
                break;
            }
            case Test2: {
                int grid_size_x = 6;
                int grid_size_y = 6;
                float padding = 0.03f;
                float size = (1.0f - (grid_size_x + 1) * padding) / (float)grid_size_x;
                m_vPhotoPos.add(new PhotoPosition(size + 2 * padding, size + 2 * padding,
                        1.0f - 2 * (size + 2 * padding)));
                for (int x = 0; x < grid_size_x; x++) {
                    for (int y = 0; y < grid_size_y; y++) {
                        if (x == 0 || x == (grid_size_x - 1) ||
                                y == 0 || y == (grid_size_y - 1)) {
                            m_vPhotoPos.add(new PhotoPosition(size * x + padding * (x + 1),
                                    size * y + padding * (y + 1), size));
                        }
                    }
                }
                break;
            }
            case Test3: {
                int grid_size_x = 6;
                int grid_size_y = 6;
                float padding = 0.03f;
                float size = (1.0f - (grid_size_x + 1) * padding) / (float)grid_size_x;
                for (int x = 0; x < grid_size_x; x++) {
                    for (int y = 0; y < grid_size_y; y++) {
                        m_vPhotoPos.add(new PhotoPosition(size * x + padding * (x + 1),
                                size * y + padding * (y + 1), size));
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
