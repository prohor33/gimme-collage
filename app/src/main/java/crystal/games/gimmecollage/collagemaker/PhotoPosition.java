package crystal.games.gimmecollage.collagemaker;

/**
 * Created by prohor on 05/10/14.
 */

public class PhotoPosition {
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