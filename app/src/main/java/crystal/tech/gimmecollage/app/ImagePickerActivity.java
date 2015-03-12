package crystal.tech.gimmecollage.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
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
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.squareup.picasso.Picasso;

import crystal.tech.gimmecollage.analytics.GoogleAnalyticsUtils;
import crystal.tech.gimmecollage.collagemaker.ImageData;
import crystal.tech.gimmecollage.collagemaker.ImageStorage;
import crystal.tech.gimmecollage.instagram_api.InstagramAPI;
import crystal.tech.gimmecollage.instagram_api.Storage;
import crystal.tech.gimmecollage.utility.ComplexImageItem;
import crystal.tech.gimmecollage.utility.ImageLoader;
import crystal.tech.gimmecollage.utility.SimpleAsyncListener;
import crystal.tech.gimmecollage.utility.SimpleAsyncTask;

public class ImagePickerActivity extends ActionBarActivity
        implements AdapterView.OnItemSelectedListener {

    static final String TAG = "ImagePickerActivity";
    static final String STATE_SELECTION_MODE = "selectionMode";

    static final String STATE_SELECTED_SPINNER_INDEX = "selectedSpinnerIndex";

    static final String STATE_SELECTED_NUM = "selectedNum";
    static final String STATE_SELECTED_IMAGES = "selectedImages";
    static final String STATE_SELECTED_IDS = "selectedIds";
    static final String STATE_SELECTED_THUMBNAILS = "selectedThumbnails";
    static final String STATE_SELECTED_SELECTED = "selectedSeleted";

    Toolbar mToolbar;
    RecyclerViewAdapter mRecyclerViewAdapter;
    Spinner mSpinner;
    MySpinnerAdapter mSpinnerAdapter;

    MenuItem mItemCounter;
    MenuItem mItemConfirm;
    MenuItem mItemRemove;
    MenuItem mItemLogout;

    int mRequestCode;
    // This variables are for RecyclerView
    boolean mSelectionMode = false;
    List<ComplexImageItem> mCurrentItems = new ArrayList<>();
    List<ComplexImageItem> mSelectedItems = new ArrayList<>();
    // This variables are for Spinner!
    List<SpinnerItem> mCurrentSpinnerItems = new ArrayList<>();
    int mSelectedSpinnerIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            mSelectionMode = savedInstanceState.getBoolean(STATE_SELECTION_MODE);

            mSelectedSpinnerIndex = savedInstanceState.getInt(STATE_SELECTED_SPINNER_INDEX);

            int num = savedInstanceState.getInt(STATE_SELECTED_NUM);
            for(int i = 0; i < num; i++) {
                mSelectedItems.add(new ComplexImageItem());
            }
            ComplexImageItem.RestoreImagesFromArray(mSelectedItems,
                    savedInstanceState.getStringArray(STATE_SELECTED_IMAGES));
            ComplexImageItem.RestoreIdsFromArray(mSelectedItems,
                    savedInstanceState.getLongArray(STATE_SELECTED_IDS));
            ComplexImageItem.RestoreThumbnailsFromArray(mSelectedItems,
                    savedInstanceState.getStringArray(STATE_SELECTED_THUMBNAILS));
            ComplexImageItem.RestoreSelectedFromArray(mSelectedItems,
                    savedInstanceState.getBooleanArray(STATE_SELECTED_SELECTED));

            Log.d(TAG, "onCreate() from savedInstanceState");
        } else {
            // Probably initialize members with default values for a new instance
            mSelectionMode = false;
            mSelectedSpinnerIndex = 0;
            Log.d(TAG, "onCreate()");
        }

        mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mRequestCode = getIntent().getIntExtra("requestCode", 0);

        setupSpinner();
        setupRecyclerView();
        if(mRequestCode == ImageSourceActivity.GALLERY_REQUEST) {
            mToolbar.setTitle("Gallery");
            mSpinner.setVisibility(View.INVISIBLE);
            updateImageItems();
        } else {
            updateSpinnerItems();
        }
    }

    private void updateSpinnerItems() {
        if (mRequestCode == ImageSourceActivity.GALLERY_REQUEST) {
            loadSpinnerItemsFromGallery();
            mSpinnerAdapter.notifyDataSetChanged();
            mSpinner.setSelection(mSelectedSpinnerIndex);
        } else if (mRequestCode == ImageSourceActivity.INSTAGRAM_REQUEST) {
            InstagramAPI.with(new InstagramAPI.Listener() {
                @Override
                public void onSuccess() {
                    loadSpinnerItemsFromInstagram();
                    mSpinnerAdapter.notifyDataSetChanged();
                    mSpinner.setSelection(mSelectedSpinnerIndex);
                }

                @Override
                public void onFail(String error) {
                    Log.e(TAG, error);
                    Utils.checkAndNotifyConnection(ImagePickerActivity.this);
                    ImagePickerActivity.this.finish();
                }
            }).updateFollows();
        }
    }

    private void updateImageItems() {
        final ProgressDialog dialog = Utils.createProgressDialog(this);
        dialog.show();
        mCurrentItems.clear();
        if (mRequestCode == ImageSourceActivity.GALLERY_REQUEST) {
            new SimpleAsyncTask(new SimpleAsyncListener() {
                @Override
                public void onSuccess() {
                    mRecyclerViewAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }

                @Override
                public void onError(String error) {
                    mRecyclerViewAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }

                @Override
                public Boolean doInBackground() {
                    loadImagesFromGalley();
                    updateSelectedFlags();
                    return true;
                }
            }).execute();
        } else if (mRequestCode == ImageSourceActivity.INSTAGRAM_REQUEST) {
            InstagramAPI.with(new InstagramAPI.Listener() {
                @Override
                public void onSuccess() {
                    loadImagesFromInstagram();
                    updateSelectedFlags();
                    mRecyclerViewAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }

                @Override
                public void onFail(String error) {
                    mRecyclerViewAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                    Log.e(TAG, error);
                    Utils.checkAndNotifyConnection(ImagePickerActivity.this);
                }
            }).updateImages(mCurrentSpinnerItems.get(mSelectedSpinnerIndex).getId());
        }
    }

    private void updateSelectedFlags() {
        for(ComplexImageItem item : mCurrentItems) {
            // Check if same image with path is in mSelectedItems.
            if(checkSelected(item) >= 0) {
                item.setSelected(true);
            } else {
                item.setSelected(false);
            }
        }
    }

    private void setupSpinner() {
        mSpinner = (Spinner) mToolbar.findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        mSpinnerAdapter = new MySpinnerAdapter(this, R.layout.image_picker_spinner_item,
                mCurrentSpinnerItems);
        // Apply the adapter to the spinner
        mSpinner.setAdapter(mSpinnerAdapter);
        mSpinner.setOnItemSelectedListener(this);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerViewAdapter = new RecyclerViewAdapter();

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mRecyclerViewAdapter);

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
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_ADDED
        };

        Cursor imageCursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns,
                null,
                null,
                MediaStore.Images.Media.DATE_ADDED);

        int ci_data = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
        int ci_id = imageCursor.getColumnIndex(MediaStore.Images.Media._ID);

        imageCursor.moveToLast();
        for (int i = 0; i < imageCursor.getCount(); i++) {
            // Create new item.
            ComplexImageItem item = new ComplexImageItem();
            // Set image path.
            item.setImage(imageCursor.getString(ci_data));
            // Sei id.
            item.setId(imageCursor.getLong(ci_id));
            // Add this item to items.
            mCurrentItems.add(item);
            imageCursor.moveToPrevious();
        }
        imageCursor.close();
    }

    private void loadSpinnerItemsFromGallery() {
        mCurrentSpinnerItems.add(new SpinnerItem("Gallery", "", ""));
    }

    private void loadImagesFromInstagram() {
        for(Storage.ImageInfo imageInfo : InstagramAPI.getImages()) {
            ComplexImageItem item = new ComplexImageItem();
            item.setImage(imageInfo.standard_resolution.url);
            item.setThumbnail(imageInfo.thumbnail.url);
            mCurrentItems.add(item);
        }
        for(ComplexImageItem item : mCurrentItems) {
            item.setSelected(checkSelected(item) >= 0);
        }
    }

    private void loadSpinnerItemsFromInstagram() {
        mCurrentSpinnerItems.add(new SpinnerItem(
                InstagramAPI.getSelf().getName(),
                InstagramAPI.getSelf().profile_picture,
                InstagramAPI.getSelf().id));
        for(Storage.UserInfo userInfo : InstagramAPI.getFollows()) {
            mCurrentSpinnerItems.add(new SpinnerItem(
                    userInfo.getName(),
                    userInfo.profile_picture,
                    userInfo.id));
        }
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
            boolean requireNotifiction = false;
            for(ComplexImageItem item : mCurrentItems) {
                if(!requireNotifiction && item.isSelected())
                    requireNotifiction = true;
                item.setSelected(false);
            }
            mSelectedItems.clear();
            if(requireNotifiction) {
                mRecyclerViewAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image_picker, menu);
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

                GoogleAnalyticsUtils.trackApplyGalleryImagesInActionBar(ImagePickerActivity.this);
                for (ComplexImageItem imageItem : mSelectedItems) {
                    ImageStorage.addImageToPull(new ImageData(imageItem.getImage(),
                            imageItem.getThumbnail(), imageItem.getId(),
                            mRequestCode != ImageSourceActivity.GALLERY_REQUEST));
                }

                // Analytic
                if (mRequestCode == ImageSourceActivity.GALLERY_REQUEST) {
                    GoogleAnalyticsUtils.trackAddImagesToPullFromGallery(ImagePickerActivity.this);
                } else {
                    GoogleAnalyticsUtils.trackAddImagesToPullFromInstagram(ImagePickerActivity.this);
                }

                ImagePickerActivity.this.setResult(RESULT_OK);
                ImagePickerActivity.this.finish();
                return true;
            case R.id.action_logout:
                InstagramAPI.resetAuthentication();
                ImagePickerActivity.this.finish();
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
        mSelectedSpinnerIndex = pos;
        updateImageItems();
    }

    // Spinner nothing selected event.
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    // Adapter, which manages data for RecyclerView class.
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        public RecyclerViewAdapter() {
            mImageLoader = new ImageLoader(ImagePickerActivity.this);
        }
        private ImageLoader mImageLoader;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(
                    R.layout.image_picker_item, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
            final ComplexImageItem item = mCurrentItems.get(i);
            if (mRequestCode == ImageSourceActivity.GALLERY_REQUEST) {
                mImageLoader.loadThumbnail(item.getId(), viewHolder.imageView);
            } else if (mRequestCode == ImageSourceActivity.INSTAGRAM_REQUEST) {
                Picasso.with(ImagePickerActivity.this).load(item.getThumbnail())
                        .into(viewHolder.imageView);
            }
            viewHolder.showSelection(item.isSelected());

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.toggle();
                    viewHolder.showSelection(item.isSelected());
                    ImagePickerActivity.this.selectImage(i);
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

        savedInstanceState.putInt(STATE_SELECTED_SPINNER_INDEX, mSelectedSpinnerIndex);

        savedInstanceState.putInt(STATE_SELECTED_NUM, mSelectedItems.size());
        savedInstanceState.putStringArray(STATE_SELECTED_IMAGES,
                ComplexImageItem.GenerateImagesArray(mSelectedItems));
        savedInstanceState.putLongArray(STATE_SELECTED_IDS,
                ComplexImageItem.GenerateIdsArray(mSelectedItems));
        savedInstanceState.putStringArray(STATE_SELECTED_THUMBNAILS,
                ComplexImageItem.GenerateThumbnailsArray(mSelectedItems));
        savedInstanceState.putBooleanArray(STATE_SELECTED_SELECTED,
                ComplexImageItem.GenerateSelectedArray(mSelectedItems));

        Log.d(TAG, "onSaveInstanceState()");

        super.onSaveInstanceState(savedInstanceState);
    }

    class SpinnerItem {
        private String mText;
        private String mImage;
        private String mId;

        public SpinnerItem(String text, String image, String id) {
            mText = text;
            mImage = image;
            mId = id;
        }

        public String getText() { return mText; }
        public String getImage() { return mImage; }
        public String getId() { return mId; }
    }

    class MySpinnerAdapter extends ArrayAdapter<SpinnerItem> {
        Context mContext;
        int mViewResourceId;
        List<SpinnerItem> mSpinnerItems;

        public MySpinnerAdapter(Context context, int viewResourceId, List<SpinnerItem> spinnerItems) {
            super(context, viewResourceId, spinnerItems);
            mContext = context;
            mViewResourceId = viewResourceId;
            mSpinnerItems = spinnerItems;
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView,
                                  ViewGroup parent) {
            View view;
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                view = getLayoutInflater().inflate(mViewResourceId, parent, false);
            } else {
                view = convertView;
            }
            TextView textView = (TextView) view.findViewById(R.id.textView);
            ImageView imageView = (ImageView) view.findViewById(R.id.imageView);

            textView.setText(mSpinnerItems.get(position).getText());
            if(mRequestCode == ImageSourceActivity.INSTAGRAM_REQUEST) {
                Picasso.with(mContext).load(mSpinnerItems.get(position).getImage()).into(imageView);
            }

            return view;
        }
    }
}
