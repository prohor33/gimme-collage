package crystal.tech.gimmecollage.collagemaker;

import android.graphics.PointF;
import android.util.Log;

import java.util.ArrayList;

import crystal.tech.gimmecollage.collagemaker.CollageMaker.CollageType;

/**
 * Created by prohor on 05/10/14.
 */

public class CollageConfig {
    private static final String TAG = "CollageConfig";

    private ArrayList<PhotoPosition> m_vPhotoPos = new ArrayList<PhotoPosition>();
    private float collageAspectRatio = 1.0f; // height / width

    public CollageConfig(CollageType type) {
        switch (type) {
            case Grid: {
                int grid_size_x = 4;
                int grid_size_y = 4;
                float padding = 0.02f;
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
                float padding = 0.02f;
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
                float padding = 0.02f;
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
                float padding = 0.02f;
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
                float padding = 0.02f;
                float size = (1.0f - (grid_size_x + 1) * padding) / (float)grid_size_x;
                for (int x = 0; x < grid_size_x; x++) {
                    for (int y = 0; y < grid_size_y; y++) {
                        m_vPhotoPos.add(new PhotoPosition(size * x + padding * (x + 1),
                                size * y + padding * (y + 1), size));
                    }
                }
                break;
            }
            case Polygons1: {
                collageAspectRatio = 0.8f;
                float d_x = 0.02f;
                float d_y = d_x / collageAspectRatio;
                float size1_x = 0.3f;
                float size1_y = (1.0f - 3 * d_x) / 2;
                float size2_x = 1.0f - size1_x - 3 * d_x;
                float size2_y = 1.0f - size1_y - 3 * d_y;
                m_vPhotoPos.add(new PhotoPosition(d_x, d_y, size1_x, size1_y));
                m_vPhotoPos.add(new PhotoPosition(2 * d_x + size1_x, d_y, size2_x, size1_y));
                m_vPhotoPos.add(new PhotoPosition(d_x, 2 * d_y + size1_y, size2_x, size2_y));
                m_vPhotoPos.add(new PhotoPosition(2 * d_x + size2_x, 2 * d_y + size1_y, size1_x, size2_y));
                break;
            }
            case Polygons2: {
                collageAspectRatio = 1.0f;
                float d_x = 0.02f;
                float d_y = d_x / collageAspectRatio;
                float size1_x = 0.3f;
                float size1_y = (1.0f - 3 * d_x) / 2;
                float size2_x = 1.0f - size1_x - 3 * d_x;
                float size2_y = 1.0f - size1_y - 3 * d_y;
                m_vPhotoPos.add(new PhotoPosition(d_x, d_y, size1_x, size1_y));
                m_vPhotoPos.add(new PhotoPosition(2 * d_x + size1_x, d_y, size2_x, size1_y));
                m_vPhotoPos.add(new PhotoPosition(d_x, 2 * d_y + size1_y, size2_x, size2_y));
                m_vPhotoPos.add(new PhotoPosition(2 * d_x + size2_x, 2 * d_y + size1_y, size1_x, size2_y));
                break;
            }
            case WithAngles1: {
                collageAspectRatio = 1.2f;
                float s_x = 0.6f;
                float s_y = s_x / collageAspectRatio;
                m_vPhotoPos.add(new PhotoPosition(0.3f, 0.1f, s_x, s_y, 35.0f));
                m_vPhotoPos.add(new PhotoPosition(0.4f, 0.1f, s_x, s_y, -25.0f));
                m_vPhotoPos.add(new PhotoPosition(0.3f, 0.55f, s_x, s_y, 25.0f));
                m_vPhotoPos.add(new PhotoPosition(0.4f, 0.55f, s_x, s_y, -30.0f));
                break;
            }
            case FiveSquares: {
                // add color padding
                collageAspectRatio = 1.0f;
                float padding = 0.02f;
                float size_big = (1.0f - padding * 3) / 2.0f;
                float size_small = 0.4f;
                float start_small = (1.0f - size_small) / 2.0f;
                m_vPhotoPos.add(new PhotoPosition(padding, padding, size_big, size_big));
                m_vPhotoPos.add(new PhotoPosition(padding * 2 + size_big, padding, size_big, size_big));
                m_vPhotoPos.add(new PhotoPosition(padding * 2 + size_big, padding * 2 + size_big, size_big, size_big));
                m_vPhotoPos.add(new PhotoPosition(padding, padding * 2 + size_big, size_big, size_big));
                m_vPhotoPos.add(new PhotoPosition(start_small, start_small, size_small, size_small));
                break;
            }
            case FourSquareTilt: {
                collageAspectRatio = 1.0f;
                float size = 0.65f;
                float padding = 0.02f / 2.0f;
                float angle = 15.0f;
                float small_slash = (float) Math.sin(Math.toRadians(angle)) * size;
                float big_slash = (float) Math.cos(Math.toRadians(angle)) * size;

                m_vPhotoPos.add(new PhotoPosition(0.5f + padding, 0.5f + padding, size, size, angle));
                m_vPhotoPos.add(new PhotoPosition(0.5f + padding + small_slash,
                        0.5f - padding - big_slash, size, size, angle));
                m_vPhotoPos.add(new PhotoPosition(0.5f - padding - big_slash + small_slash,
                        0.5f - padding - big_slash - small_slash, size, size, angle));
                m_vPhotoPos.add(new PhotoPosition(0.5f - padding - big_slash,
                        0.5f + padding - small_slash, size, size, angle));
                break;
            }
            case FourSquaresAndRomb: {
                // add color padding
                collageAspectRatio = 1.0f;
                float padding = 0.02f;
                float size_big = (1.0f - padding * 3) / 2.0f;
                float size_small = 0.48f;
                float start_small = 0.5f - size_small / (float) Math.sqrt(2);
                m_vPhotoPos.add(new PhotoPosition(padding, padding, size_big, size_big));
                m_vPhotoPos.add(new PhotoPosition(padding * 2 + size_big, padding, size_big, size_big));
                m_vPhotoPos.add(new PhotoPosition(padding * 2 + size_big, padding * 2 + size_big, size_big, size_big));
                m_vPhotoPos.add(new PhotoPosition(padding, padding * 2 + size_big, size_big, size_big));
                m_vPhotoPos.add(new PhotoPosition(0.5f, start_small, size_small, size_small, 45.0f));
                break;
            }
            case FourRectanglesAndSquare: {
                collageAspectRatio = 1.0f;
                float padding = 0.02f;
                float size_small = 0.31f;
                float size_big = 1.0f - size_small - 3 * padding;
                float size_center = 1.0f - size_small * 2 - padding * 4;
                m_vPhotoPos.add(new PhotoPosition(padding, padding, size_big, size_small));
                m_vPhotoPos.add(new PhotoPosition(padding, size_small + 2 * padding, size_small, size_big));
                m_vPhotoPos.add(new PhotoPosition(size_small + 2 * padding,
                        size_small + size_center + 3 * padding, size_big, size_small));
                m_vPhotoPos.add(new PhotoPosition(size_big + 2 * padding, padding, size_small, size_big));
                m_vPhotoPos.add(new PhotoPosition(size_small + 2 * padding,
                        size_small + 2 * padding, size_center, size_center));
                break;
            }
            case FourRectanglesTwoSidesAngle: {
                // add color padding
                collageAspectRatio = 1.0f;
                PointF size1 = new PointF(0.56f, 0.43f);
                PointF size2 = new PointF(0.43f, 0.55f);
                float angle = 5.0f;

                m_vPhotoPos.add(new PhotoPosition(0.02f, 0.06f, size1.x, size1.y, -angle)); // 1 (left top)
                m_vPhotoPos.add(new PhotoPosition(0.015f, 0.435f, size2.x, size2.y, -angle)); // 2
                m_vPhotoPos.add(new PhotoPosition(0.56f, 0.02f, size2.x, size2.y, angle));  // 4
                m_vPhotoPos.add(new PhotoPosition(0.42f, 0.51f, size1.x, size1.y, angle));   // 3

                break;
            }
            case FiveRectanglesTwoSidesAngle: {
                // add color padding
                collageAspectRatio = 1.0f;
                float angle = 5.0f;

                m_vPhotoPos.add(new PhotoPosition(0.68f, 0.03f, 0.31f, 0.37f, angle)); // 1 (left right)
                m_vPhotoPos.add(new PhotoPosition(0.35f, 0.03f, 0.31f, 0.37f, 0.0f)); // 2
                m_vPhotoPos.add(new PhotoPosition(0.03f, 0.06f, 0.31f, 0.37f, -angle)); // 3
                m_vPhotoPos.add(new PhotoPosition(0.03f, 0.41f, 0.45f, 0.57f, -angle)); // 4
                m_vPhotoPos.add(new PhotoPosition(0.53f, 0.38f, 0.45f, 0.57f, angle)); // 5

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

    public float getCollageAspectRatio() {
        return collageAspectRatio;
    }
}
