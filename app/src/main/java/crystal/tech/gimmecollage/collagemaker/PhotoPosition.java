package crystal.tech.gimmecollage.collagemaker;

import android.graphics.Point;
import android.graphics.PointF;

/**
 * Created by prohor on 05/10/14.
 */

public class PhotoPosition {
    public PointF p = new PointF();
    public PointF size = new PointF();
    public float angle;
    public boolean frame = false;

    public PhotoPosition(float x, float y, float size) {
        this(x, y, size, size, 0);
    }
    public PhotoPosition(float x, float y, float size_x, float size_y) {
        this(x, y, size_x, size_y, 0);
    }
    public PhotoPosition(float x, float y, float size_x, float size_y, float angle) {
        this(x, y, size_x, size_y, angle, false);
    }
    public PhotoPosition(float x, float y, float size_x, float size_y, float angle, boolean frame) {
        p.x = x;
        p.y = y;
        size.x = size_x;
        size.y = size_y;
        this.angle = angle;
        this.frame = frame;
    }
}