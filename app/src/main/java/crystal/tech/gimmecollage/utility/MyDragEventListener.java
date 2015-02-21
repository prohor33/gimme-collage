package crystal.tech.gimmecollage.utility;

import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;

import crystal.tech.gimmecollage.app.R;
import crystal.tech.gimmecollage.collagemaker.ImageStorage;

/**
 * Created by prohor on 05/02/15.
 */

public class MyDragEventListener implements View.OnDragListener {

    public static String FROM_PULL_DRAG_SOURCE = "FromPull";
    public static String FROM_COLLAGE_DRAG_SOURCE = "FromCollage";

    private final String TAG = "MyDragEventListener";

    // This is the method that the system calls when it dispatches a drag event to the
    // listener.
    public boolean onDrag(View view, DragEvent event) {

        ImageView iv = (ImageView)view;
        final int action = event.getAction();

        switch(action) {

            case DragEvent.ACTION_DRAG_STARTED:

                // Determines if this View can accept the dragged data
                if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {

                    // As an example of what your application might do,
                    // applies a blue color tint to the View to indicate that it can accept
                    // data.
                    // v.setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);

                    // Invalidate the view to force a redraw in the new tint
//                    v.invalidate();

                    // returns true to indicate that the View can accept the dragged data.
                    return true;

                }

                // Returns false. During the current drag and drop operation, this View will
                // not receive events again until ACTION_DRAG_ENDED is sent.
                return false;

            case DragEvent.ACTION_DRAG_ENTERED:

                // Applies a green tint to the View. Return true; the return value is ignored.

                onDragOverBegin(iv);
                return true;

            case DragEvent.ACTION_DRAG_LOCATION:

                // Ignore the event
                return true;

            case DragEvent.ACTION_DRAG_EXITED:

                onDragOverEnd(iv);
                return true;

            case DragEvent.ACTION_DROP:

                // Gets the item containing the dragged data
                ClipData.Item itemSource = event.getClipData().getItemAt(0);
                // Gets the text data from the item.
                String sourceName = itemSource.getText().toString();

                ClipData.Item itemIndex = event.getClipData().getItemAt(1);

                int imageIndex = Integer.parseInt(itemIndex.getText().toString());

                if (sourceName.compareTo(FROM_PULL_DRAG_SOURCE) == 0) {
                    Log.d(TAG, "Drop from pull i = " + imageIndex);
                    ImageStorage.dropPullImageToCollage(imageIndex, iv);
                } else if (sourceName.compareTo(FROM_COLLAGE_DRAG_SOURCE) == 0) {
                    Log.d(TAG, "Drop from collage i = " + imageIndex);
                    ImageStorage.dropCollageImageToCollage(imageIndex, iv);
                } else {
                    Log.e(TAG, "Wrong source name. Drop from nowhere.");
                }

                onDragOverEnd(iv);

                // Returns true. DragEvent.getResult() will return true.
                return true;

            case DragEvent.ACTION_DRAG_ENDED:

                // Turns off any color tinting
                onDragOverEnd(iv);

                // Does a getResult(), and displays what happened.
                if (event.getResult()) {
//                    Toast.makeText(this, "The drop was handled.", Toast.LENGTH_LONG);

                } else {
//                    Toast.makeText(this, "The drop didn't work.", Toast.LENGTH_LONG);

                }

                // returns true; the value is ignored.
                return true;

            // An unknown action type was received.
            default:
                Log.e("DragDrop Example", "Unknown action type received by OnDragListener.");
                break;
        }

        return false;
    }

    void onDragOverBegin(ImageView iv) {
        iv.setColorFilter(iv.getResources().getColor(R.color.collage_image_drag_sel_clr),
                PorterDuff.Mode.MULTIPLY);
        iv.setBackgroundResource(R.drawable.collage_image_second_back);
        iv.invalidate();
    }

    void onDragOverEnd(ImageView iv) {
        iv.clearColorFilter();
        iv.setBackgroundResource(0);
        iv.invalidate();
    }
};