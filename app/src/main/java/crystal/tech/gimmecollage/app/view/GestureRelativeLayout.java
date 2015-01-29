package crystal.tech.gimmecollage.app.view;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by prohor on 25/01/15.
 */
public class GestureRelativeLayout extends RelativeLayout {

    private GestureDetector gestureDetector;

    public GestureRelativeLayout(android.content.Context context) {
        super(context);
    }

    public GestureRelativeLayout(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return false;
    }

    public void setGestureDetector(GestureDetector gd) {
        gestureDetector = gd;
    }
}
