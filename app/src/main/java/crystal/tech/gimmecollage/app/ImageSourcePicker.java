package crystal.tech.gimmecollage.app;

import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.squareup.picasso.Picasso;

import crystal.tech.gimmecollage.collagemaker.ImageData;
import crystal.tech.gimmecollage.collagemaker.ImageStorage;
import crystal.tech.gimmecollage.instagram_api.InstagramAPI;
import crystal.tech.gimmecollage.instagram_api.Storage;
import crystal.tech.gimmecollage.utility.ComplexImageItem;

public class ImageSourcePicker extends ActionBarActivity
        implements AdapterView.OnItemSelectedListener {

    static final String TAG = "ImageSourcePicker";
    static final String STATE_SELECTION_MODE = "selectionMode";

    static final String STATE_SELECTED_NUM = "selectedNum";
    static final String STATE_SELECTED_IMAGES = "selectedImages";
    static final String STATE_SELECTED_THUMBNAILS = "selectedThumbnails";
    static final String STATE_SELECTED_SELECTED = "selectedSeleted";

    Toolbar mToolbar;
    RecyclerViewAdapter mAdapter;

    MenuItem mItemCounter;
    MenuItem mItemConfirm;
    MenuItem mItemRemove;
    MenuItem mItemLogout;

    int mRequestCode;
    boolean mSelectionMode = false;
    List<ComplexImageItem> mCurrentItems = new ArrayList<>();
    List<ComplexImageItem> mSelectedItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_source_picker);

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            mSelectionMode = savedInstanceState.getBoolean(STATE_SELECTION_MODE);

            int num = savedInstanceState.getInt(STATE_SELECTED_NUM);
            mSelectedItems.clear();
            for(int i = 0; i < num; i++) {
                mSelectedItems.add(new ComplexImageItem());
            }
            ComplexImageItem.RestoreImagesFromArray(mSelectedItems,
                    savedInstanceState.getStringArray(STATE_SELECTED_IMAGES));
            ComplexImageItem.RestoreThumbnailsFromArray(mSelectedItems,
                    savedInstanceState.getStringArray(STATE_SELECTED_THUMBNAILS));
            ComplexImageItem.RestoreSelectedFromArray(mSelectedItems,
                    savedInstanceState.getBooleanArray(STATE_SELECTED_SELECTED));

            Log.d(TAG, "onCreate() from savedInstanceState");
        } else {
            // Probably initialize members with default values for a new instance
            mSelectionMode = false;
            Log.d(TAG, "onCreate()");
        }

        mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setupSpinner();
        setupRecyclerView();
        // TODO: make choice for gallery or instagram image source.
        mRequestCode = getIntent().getIntExtra("requestCode", 0);
        mCurrentItems.clear();
        if (mRequestCode == ImageSourceActivity.GALLERY_REQUEST) {
            loadImagesFromGalley();
            updateSelectedFlags();
        } else if (mRequestCode == ImageSourceActivity.INSTAGRAM_REQUEST) {
            InstagramAPI.with(new InstagramAPI.Listener() {
                @Override
                public void onSuccess() {
                    loadImagesFromInstagram();
                }

                @Override
                public void onFail(String error) {
                    Toast.makeText(ImageSourcePicker.this, "Error : " + error, Toast.LENGTH_LONG)
                            .show();
                }
            }).updateSelf();
        }
    }

    private void setupSpinner() {
        Spinner spinner = (Spinner) mToolbar.findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        CharSequence[] spinnerItems = {"One", "Two", "Three"};
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this,
                android.R.layout.simple_spinner_item, spinnerItems);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mAdapter = new RecyclerViewAdapter();

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);

        int spanCount;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            spanCount = 3;
        } else {
            spanCount = 5;
        }
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void selectImage(int i) {
        // If it is the first time we select item, we need to start selection mode.
        if(!mSelectionMode) setSelectionMode(true);
        // Check if this item, with index i is selected.
        int pos = checkSelected(i);
        // If this image is already in selected list, we remove it.
        if(pos >= 0)
            mSelectedItems.remove(pos);
        else
            mSelectedItems.add(mCurrentItems.get(i));
        // Update counter.
        mItemCounter.setTitle(String.valueOf(mSelectedItems.size()));

        // If we deselected all item and selection mode is active, we need to disable it.
        if(mSelectedItems.isEmpty()) {
            if(mSelectionMode) setSelectionMode(false);
        }
    }

    public int checkSelected(ComplexImageItem item) {
        int pos = -1;
        for(int i = 0; i < mSelectedItems.size(); i++) {
            if(mSelectedItems.get(i).getImage().equals(item.getImage())) {
                // This element is already in a list.
                pos = i;
                break;
            }
        }
        return pos;
    }

    public int checkSelected(int i) {
        return checkSelected(mCurrentItems.get(i));
    }

    private void loadImagesFromGalley() {
        // Define which columns we need from sql table.
        final String[] columns = {
                MediaStore.Images.Media.DATA
        };

        Cursor imageCursor = getContentResolver().query(
                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                columns,
                null,
                null,
                null); // we sort results by ?name?

        int image_column_index = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
        int thumbnail_column_index = imageCursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA);

        imageCursor.moveToLast();
        for (int i = 0; i < imageCursor.getCount(); i++) {
            // Create new item.
            ComplexImageItem item = new ComplexImageItem();
            // Set image path.
            item.setImage(imageCursor.getString(image_column_index));
            // Set thumbnail path.
            item.setThumbnail(imageCursor.getString(thumbnail_column_index));
            // Add this item to items.
            mCurrentItems.add(item);
            imageCursor.moveToPrevious();
        }
        imageCursor.close();
    }

    private void loadImagesFromInstagram() {
        InstagramAPI.with(new InstagramAPI.Listener() {
            @Override
            public void onSuccess() {
                for(Storage.ImageInfo imageInfo : InstagramAPI.getImages()) {
                    ComplexImageItem item = new ComplexImageItem();
                    item.setImage(imageInfo.standard_resolution.url);
                    item.setThumbnail(imageInfo.thumbnail.url);
                    mCurrentItems.add(item);
                }
                updateSelectedFlags();
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(ImageSourcePicker.this, "Error : " + error, Toast.LENGTH_LONG).show();
            }
        }).updateImages("self");
    }

    private void updateSelectedFlags() {
        for(ComplexImageItem item : mCurrentItems) {
            item.setSelected(checkSelected(item) >= 0);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void setSelectionMode(boolean selection) {
        mSelectionMode = selection;
        mItemRemove.setVisible(selection);
        mItemCounter.setVisible(selection);
        mItemConfirm.setVisible(selection);
        getSupportActionBar().setDisplayHomeAsUpEnabled(!selection);

        // item logout is available only for INSTAGRAM
        if(mRequestCode == ImageSourceActivity.INSTAGRAM_REQUEST) {
            mItemLogout.setVisible(!selection);
        } else {
            mItemLogout.setVisible(false);
        }

        if(selection) {
            // Set counter so 0.
            mItemCounter.setTitle(String.valueOf(mSelectedItems.size()));
        } else {
            for(ComplexImageItem item : mCurrentItems) {
                item.setSelected(false);
            }
            mSelectedItems.clear();
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_source_picker, menu);
        mItemRemove = menu.getItem(0);
        mItemCounter = menu.getItem(1);
        mItemConfirm = menu.getItem(2);
        mItemLogout = menu.getItem(3);
        // Selection mode!
        setSelectionMode(mSelectionMode);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id) {
            case R.id.action_remove:
                // Stop selection mode, clear selected images.
                setSelectionMode(false);
                return true;
            case R.id.action_confirm:

                for (ComplexImageItem imageItem : mSelectedItems) {
                    ImageStorage.addImageToPull(new ImageData(imageItem.getImage(),
                            imageItem.getThumbnail(),
                            mRequestCode != ImageSourceActivity.GALLERY_REQUEST));
                }

                ImageSourcePicker.this.setResult(RESULT_OK);
                ImageSourcePicker.this.finish();
                return true;
            case R.id.action_logout:
                InstagramAPI.resetAuthentication();
                ImageSourcePicker.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Event for pressing back button on a phone.
    @Override
    public void onBackPressed() {
        if(mSelectionMode) {
            setSelectionMode(false);
        } else {
            super.onBackPressed();
        }
    }

    // Spinner item selected event.
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    }

    // Spinner nothing selected event.
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    // Adapter, which manages data for RecyclerView class.
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        public RecyclerViewAdapter() {}

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(
                    R.layout.image_source_picker_item, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
            final ComplexImageItem item = mCurrentItems.get(i);
            if (mRequestCode == ImageSourceActivity.GALLERY_REQUEST) {
                Picasso.with(ImageSourcePicker.this).load(new File(item.getThumbnail()))
                        .into(viewHolder.imageView);
            } else if (mRequestCode == ImageSourceActivity.INSTAGRAM_REQUEST) {
                Picasso.with(ImageSourcePicker.this).load(item.getThumbnail())
                        .into(viewHolder.imageView);
            }
            viewHolder.showSelection(item.getSelected());

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.toggle();
                    viewHolder.showSelection(item.getSelected());
                    ImageSourcePicker.this.selectImage(i);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mCurrentItems.size();
        }

        // Class, that holds widgets for a single RecyclerView item.
        class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView;
            private ImageView imageBackground;
            private ImageView imageSelect;

            public ViewHolder(View itemView) {
                super(itemView);

                imageView = (ImageView) itemView.findViewById(R.id.imageView);
                imageBackground = (ImageView) itemView.findViewById(R.id.imageSelectBackground);
                imageSelect = (ImageView) itemView.findViewById(R.id.imageSelect);
            }

            public void showSelection(boolean selection) {
                if(selection) {
                    imageSelect.setVisibility(View.VISIBLE);
                    imageBackground.setVisibility(View.VISIBLE);
                } else {
                    imageSelect.setVisibility(View.INVISIBLE);
                    imageBackground.setVisibility(View.INVISIBLE);
                }
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the current selected items & selectionMode.
        savedInstanceState.putBoolean(STATE_SELECTION_MODE, mSelectionMode);

        savedInstanceState.putInt(STATE_SELECTED_NUM, mSelectedItems.size());
        savedInstanceState.putStringArray(STATE_SELECTED_IMAGES,
                ComplexImageItem.GenerateImagesArray(mSelectedItems));
        savedInstanceState.putStringArray(STATE_SELECTED_THUMBNAILS,
                ComplexImageItem.GenerateThumbnailsArray(mSelectedItems));
        savedInstanceState.putBooleanArray(STATE_SELECTED_SELECTED,
                ComplexImageItem.GenerateSelectedArray(mSelectedItems));

        Log.d(TAG, "onSaveInstanceState()");

        super.onSaveInstanceState(savedInstanceState);
    }
}
