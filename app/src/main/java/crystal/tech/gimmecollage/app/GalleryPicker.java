package crystal.tech.gimmecollage.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

import crystal.tech.gimmecollage.analytics.GoogleAnalyticsUtils;
import crystal.tech.gimmecollage.collagemaker.CollageMaker;
import crystal.tech.gimmecollage.collagemaker.ImageStorage;

public class GalleryPicker extends ActionBarActivity {

    private static final String TAG = "GalleryPicker";

    class GalleryImageHolder {
        private String mImagePath;
        private boolean mSelected;

        GalleryImageHolder() {
            mImagePath = "";
            mSelected = false;
        }

        public void setImagePath(String imagePath) {
            mImagePath = imagePath;
        }

        public String getImagePath() {
            return mImagePath;
        }

        public void setSelected(boolean selected) {
            mSelected = selected;
        }

        public boolean isSelected() {
            return mSelected;
        }

        public void toggle() {
            setSelected(!mSelected);
        }
    }

    private GalleryImageHolder[] mGalleryImages;
    private int mNumSelected;
    private GridView mGridView;
    private ActionMode mActionMode;

    private String[] mResultArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_picker);

//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);

        mResultArray = null;

        mNumSelected = 0;
        loadGalleryImages();

        mGridView = (GridView) findViewById(R.id.gridView);
        mGridView.setAdapter(new GalleryPickerAdapter(this));


        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mGalleryImages[position].toggle();

                if(mGalleryImages[position].isSelected()) {
                    showImageSelection(view);
                    mNumSelected++;
                } else {
                    hideImageSelection(view);
                    mNumSelected--;
                }

                if (mActionMode == null) {
                    // Start the CAB using the ActionMode.Callback defined above
                    mActionMode = GalleryPicker.this.startSupportActionMode(mActionModeCallback);
                }

                mActionMode.setTitle(String.valueOf(mNumSelected) + " " +
                        getResources().getString(R.string.gp_counter_text));
            }
        });

    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.gallery_context_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_confirm:
                    // Send all selected images to CollageActivity!
                    constructResultArray();

                    mActionMode.finish();

                    GoogleAnalyticsUtils.SendEvent(GalleryPicker.this,
                            R.string.ga_event_category_apply_gallery_images,
                            R.string.ga_event_action_apply_gallery_images,
                            R.string.ga_event_label_apply_gallery_images);

                    Intent intent = new Intent();
                    ImageStorage.getInstance().setImagesFromGallery(mResultArray);
                    GalleryPicker.this.setResult(RESULT_OK, intent);
                    GalleryPicker.this.finish();

                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            deselectAllImages();
            mActionMode = null;
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gallery_picker, menu);
        //MenuItem confirmItem = menu.findItem(R.id.action_confirm);
        //SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        // Configure the search info and add any event listeners
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class GalleryPickerAdapter extends BaseAdapter {
        private Context mContext;

        public GalleryPickerAdapter(Context context) { mContext = context; }

        public int getCount() {
            return mGalleryImages.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (view == null) {  // if it's not recycled, initialize some attributes
                view = getLayoutInflater().inflate(R.layout.gallery_item, parent, false);

                view.setTag(R.id.picture, view.findViewById(R.id.picture));
                view.setTag(R.id.imageSelectBackground,
                        view.findViewById(R.id.imageSelectBackground));
                view.setTag(R.id.imageSelect, view.findViewById(R.id.imageSelect));

                //Log.d(TAG, "View constructed, " + position);
            }

            // Check if current position is Selected, and show/hide selection effect.
            if(mGalleryImages[position].isSelected()) {
                showImageSelection(view);
            } else {
                hideImageSelection(view);
            }

            View forgroundView = (View) view.getTag(R.id.imageSelectBackground);
            float alpha = 0.5f;

            // Modify Image Background Alpha
            if (Build.VERSION.SDK_INT < 11) {
                final AlphaAnimation animation = new AlphaAnimation(alpha, alpha);
                animation.setDuration(0);
                animation.setFillAfter(true);
                forgroundView.startAnimation(animation);
            } else {
                forgroundView.setAlpha(alpha);
            }

            ImageView imageView = (ImageView) view.getTag(R.id.picture);

            Picasso.with(mContext).load(new File(mGalleryImages[position].getImagePath()))
                    .into(imageView);

            //Log.d(TAG, "View updated, " + position);

            return view;
        }

    }

    private void loadGalleryImages() {
        // Define which columns we need from sql table.
        final String[] columns = {
                MediaStore.Images.Thumbnails.DATA,
                MediaStore.Images.Thumbnails._ID
        };

        Cursor imageCursor = getContentResolver().query(
                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                columns,
                null,
                null,
                MediaStore.Images.Thumbnails._ID); // we sort results by ?name?

        int data_column_index = imageCursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA);

        int count = imageCursor.getCount();

        Log.d(TAG, "count = " + count);

        mGalleryImages = new GalleryImageHolder[count];

        imageCursor.moveToLast();
        for (int i = 0; i < count; i++) {
            mGalleryImages[i] = new GalleryImageHolder();
            mGalleryImages[i].setImagePath(imageCursor.getString(data_column_index));
            imageCursor.moveToPrevious();
        }
        imageCursor.close();
    }

    private void showImageSelection(View view) {
        ((View) view.getTag(R.id.imageSelect)).setVisibility(View.VISIBLE);
        ((View) view.getTag(R.id.imageSelectBackground)).setVisibility(View.VISIBLE);
    }

    private void hideImageSelection(View view) {
        ((View) view.getTag(R.id.imageSelect)).setVisibility(View.INVISIBLE);
        ((View) view.getTag(R.id.imageSelectBackground)).setVisibility(View.INVISIBLE);
    }

    private void deselectAllImages() {
        for(int i = 0; i < mGalleryImages.length; i++) {
            mGalleryImages[i].setSelected(false);
        }

        for(int i = 0; i < mGridView.getChildCount(); i++) {
            hideImageSelection(mGridView.getChildAt(i));
        }

        mNumSelected = 0;
    }

    private void constructResultArray() {
        mResultArray = new String[mNumSelected];
        int array_index = 0;
        for(int i = 0; i < mGalleryImages.length; i++) {
            if(mGalleryImages[i].isSelected()) {
                if (array_index < mNumSelected) {
                    mResultArray[array_index] = mGalleryImages[i].getImagePath();
                    array_index++;
                }
            }
        }
    }
}
