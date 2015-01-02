package crystal.tech.gimmecollage.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by prohor on 02/01/15.
 */
public class DragDropView extends RelativeLayout {

    private final String TAG = "DragDropView";

    private int _xDelta;
    private int _yDelta;
    private boolean isMoving = false;

    /**
     * Default Constructor
     * @param context
     */
    public DragDropView(Context context) {
        super(context);
    }

    /**
     * Default Constructor
     * @param context
     * @param attrs
     */
    public DragDropView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Default Constructor
     * @param context
     * @param attrs
     * @param defStyle
     */
    public DragDropView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /** Adding draggable object to the dragView
     * @param draggableObject - object to be dragged
     * @param - x horizontal position of the view
     * @param - y vertical position of the view
     * @param - width width of the view
     * @param - height height of the view
     */
    public void AddDraggableView(View draggableObject, int x, int y, int width, int height) {
        LayoutParams lpDraggableView = new LayoutParams(width, height);
//        lpDraggableView.gravity = Gravity.TOP;
        lpDraggableView.leftMargin = x;
        lpDraggableView.topMargin = y;
        if(draggableObject instanceof ImageView) {
            ImageView ivDrag = (ImageView) draggableObject;
            ivDrag.setLayoutParams(lpDraggableView);
            ivDrag.setOnTouchListener(OnTouchToDrag);
            this.addView(ivDrag);
        }
        //TODO implement to do other type of view
//		else if(draggableObject instanceof TextView) {
//			TextView tvDrag = (TextView) draggableObject;
//			tvDrag.setLayoutParams(lpDraggableView);
//			tvDrag.setOnTouchListener(OnTouchToDrag);
//			this.addView(tvDrag);
//		}
//		else if(draggableObject instanceof Button) {
//			Button btnDrag = (Button) draggableObject;
//			btnDrag.setLayoutParams(lpDraggableView);
//			btnDrag.setOnTouchListener(OnTouchToDrag);
//			this.addView(btnDrag);
//		}

    }

    private final double delta_size = 0.25;

    /**
     * Draggable object ontouch listener
     * Handle the movement of the object when dragged and dropped
     */
    public View.OnTouchListener OnTouchToDrag = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View img_view, MotionEvent event) {
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();

//            Log.v(TAG, "onTouch");
            View view = (View)img_view.getParent();
            switch(event.getAction())
            {
                case MotionEvent.ACTION_MOVE:
                {
                    RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    lParams.leftMargin = X - _xDelta;
                    lParams.topMargin = Y - _yDelta;
//                    layoutParams.rightMargin = -250;
//                    layoutParams.bottomMargin = -250;
                    view.setLayoutParams(lParams);
                    if (!isMoving) {
                        // starting moving
                        lParams.leftMargin -= lParams.width * delta_size / 2.0;
                        lParams.topMargin -= lParams.height * delta_size / 2.0;
                        lParams.width *= 1.0 + delta_size;
                        lParams.height *= 1.0 + delta_size;
                        view.setLayoutParams(lParams);
                        view.bringToFront();
                    }
                    isMoving = true;
                    break;
                }
                case MotionEvent.ACTION_UP:
                {
                    if (isMoving) {
                        isMoving = false;
                        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                        lParams.leftMargin += lParams.width * delta_size / 2.0;
                        lParams.topMargin += lParams.height * delta_size / 2.0;
                        lParams.width /= 1.0 + delta_size;
                        lParams.height /= 1.0 + delta_size;
                        view.setLayoutParams(lParams);
                    } else {
                        return false;
                    }
                    break;
                }
                case MotionEvent.ACTION_DOWN:
                {
                    RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    _xDelta = X - lParams.leftMargin;
                    _yDelta = Y - lParams.topMargin;
                    return  false;  // should handle clicking
                }
            }
            invalidate();
            return true;
        }

    };

}