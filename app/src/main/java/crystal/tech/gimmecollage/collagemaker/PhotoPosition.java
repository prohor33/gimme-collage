package crystal.tech.gimmecollage.collagemaker;

/**
 * Created by prohor on 05/10/14.
 */

public class PhotoPosition {
    public PhotoPosition(float x, float y, float size) {
        this.m_dX = x;
        this.m_dY = y;
        this.m_dSize = size;
        m_dAngle = 0;  // by default
    }
    public PhotoPosition(float x, float y, float size, float angle) {
        this(x, y, size);
        this.m_dAngle = angle;
    }
    public float getSize() {
        return m_dSize;
    }
    public float getX() {
        return m_dX;
    }
    public float getY() {
        return m_dY;
    }
    public float getAngle() {
        return m_dAngle;
    }

    private float m_dX;
    private float m_dY;
    private float m_dSize;
    private float m_dAngle;
}