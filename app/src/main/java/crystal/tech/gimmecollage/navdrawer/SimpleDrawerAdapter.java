package crystal.tech.gimmecollage.navdrawer;

import android.content.ClipData;
import android.content.ClipDescription;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import crystal.tech.gimmecollage.app.Application;
import crystal.tech.gimmecollage.app.R;
import crystal.tech.gimmecollage.collagemaker.ImageStorage;
import crystal.tech.gimmecollage.utility.MyDragEventListener;

/**
 * Created by poliveira on 24/10/2014.
 */
public class SimpleDrawerAdapter extends RecyclerView.Adapter<SimpleDrawerAdapter.ViewHolder> {

    private final String TAG = "SimpleDrawerAdapter";
    private SimpleDrawerCallbacks mSimpleDrawerCallbacks;
    private int mSelectedPosition;
    private int mTouchedPosition = -1;

    public SimpleDrawerCallbacks getSimpleDrawerCallbacks() {
        return mSimpleDrawerCallbacks;
    }

    public void setSimpleDrawerCallbacks(SimpleDrawerCallbacks SimpleDrawerCallbacks) {
        mSimpleDrawerCallbacks = SimpleDrawerCallbacks;
    }

    @Override
    public SimpleDrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.simple_drawer_row, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SimpleDrawerAdapter.ViewHolder viewHolder, final int i) {
        ImageStorage.fillPullView(viewHolder.imageView, i);


        viewHolder.itemView.setOnTouchListener(new View.OnTouchListener() {
                                                   @Override
                                                   public boolean onTouch(View v, MotionEvent event) {

                                                       switch (event.getAction()) {
                                                           case MotionEvent.ACTION_DOWN:
                                                               touchPosition(i);
                                                               return false;
                                                           case MotionEvent.ACTION_CANCEL:
                                                               touchPosition(-1);
                                                               return false;
                                                           case MotionEvent.ACTION_MOVE:
                                                               return false;
                                                           case MotionEvent.ACTION_UP:
                                                               touchPosition(-1);
                                                               return false;
                                                       }
                                                       return true;
                                                   }
                                               }
        );
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                                   @Override
                                                   public void onClick(View v) {
                                                       if (mSimpleDrawerCallbacks != null)
                                                           mSimpleDrawerCallbacks.onSimpleDrawerItemSelected(i);
                                                   }
                                               }
        );

        final ImageView imageView = (ImageView) viewHolder.itemView.findViewById(R.id.drawerImageView);
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {

            // Defines the one method for the interface, which is called when the View is long-clicked
            public boolean onLongClick(View v) {

                // Create a new ClipData.
                // This is done in two steps to provide clarity. The convenience method
                // ClipData.newPlainText() can create a plain text ClipData in one step.

                // Create a new ClipData.Item from the ImageView object's tag
                ClipData.Item itemSource = new ClipData.Item(MyDragEventListener.FROM_PULL_DRAG_SOURCE);

                // Create a new ClipData using the tag as a label, the plain text MIME type, and
                // the already-created item. This will create a new ClipDescription object within the
                // ClipData, and set its MIME type entry to "text/plain"
                String[] mimeTypes = new String[2];
                mimeTypes[0] = ClipDescription.MIMETYPE_TEXT_PLAIN;
                mimeTypes[1] = ClipDescription.MIMETYPE_TEXT_PLAIN;
                ClipData dragData = new ClipData("blah", mimeTypes, itemSource);

                ClipData.Item itemPullIndex = new ClipData.Item(Integer.toString(i));
                dragData.addItem(itemPullIndex);

                // Instantiates the drag shadow builder.
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(imageView);

                // Starts the drag

                v.startDrag(dragData,  // the data to be dragged
                        myShadow,  // the drag shadow builder
                        null,      // no need to use local data
                        0          // flags (not currently used, set to 0)
                );

                Application.moveRightDrawer(false);

                return true;
            }
        });

        /*
        if (mSelectedPosition == i || mTouchedPosition == i) {
            viewHolder.itemView.setBackgroundColor(viewHolder.itemView.getContext().getResources().getColor(R.color.selected_gray));
        } else {
            viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
        */
    }

    private void touchPosition(int position) {
        int lastPosition = mTouchedPosition;
        mTouchedPosition = position;
        if (lastPosition >= 0)
            notifyItemChanged(lastPosition);
        if (position >= 0)
            notifyItemChanged(position);
    }

    public void selectPosition(int position) {
        int lastPosition = mSelectedPosition;
        mSelectedPosition = position;
        notifyItemChanged(lastPosition);
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return ImageStorage.getPullImageCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.drawerImageView);
        }
    }
}
