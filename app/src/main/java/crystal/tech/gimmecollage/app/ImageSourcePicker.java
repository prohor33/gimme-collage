package crystal.tech.gimmecollage.app;

import android.database.Cursor;
import android.os.Build;
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
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ImageSourcePicker extends ActionBarActivity
        implements AdapterView.OnItemSelectedListener {

    private final String TAG = "ImageSourcePicker";

    Toolbar mToolbar;
    List<PickerItem> mPickerItems;
    RecyclerViewAdapter mAdapter;

    MenuItem mItemCounter;
    MenuItem mItemConfirm;
    MenuItem mItemRemove;
    boolean mSelection;
    List<String> mSelectedImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_source_picker);

        mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mSelectedImages = new ArrayList<>();
        mPickerItems = new ArrayList<>();

        setupSpinner();
        setupRecyclerView();
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
        mPickerItems.clear();
        String[] galleryImages = getGalleryImages();
        for(int i = 0; i < galleryImages.length; i++) {
            mPickerItems.add(new PickerItem(galleryImages[i], false));
        }
        //populateImageSourceItems(imageSourceItems);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mAdapter = new RecyclerViewAdapter(mPickerItems);

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void selectImage(int i) {
        if(!mSelection)
            setSelectionMode(true);
        // Adds image path to selected array.
        String imagePath = mPickerItems.get(i).mImagePath;
        if(!mSelectedImages.contains(imagePath)) {
            mSelectedImages.add(imagePath);
        } else {
            mSelectedImages.remove(imagePath);
        }
        mItemCounter.setTitle(String.valueOf(mSelectedImages.size()));
        Log.d(TAG, "selectImageSource " + i);
    }

    private String[] getGalleryImages() {
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

        String[] galleryImages = new String[count];

        imageCursor.moveToLast();
        for (int i = 0; i < count; i++) {
            galleryImages[i] = imageCursor.getString(data_column_index);
            imageCursor.moveToPrevious();
        }
        imageCursor.close();
        return galleryImages;
    }

    private void setSelectionMode(boolean selection) {
        mSelection = selection;
        mItemRemove.setVisible(selection);
        mItemCounter.setVisible(selection);
        mItemConfirm.setVisible(selection);
        getSupportActionBar().setDisplayHomeAsUpEnabled(!selection);

        if(selection) {
            mItemCounter.setTitle("0");
            mSelectedImages.clear();
        } else {
            for(PickerItem item : mPickerItems) {
                item.mSelected = false;
            }
            mAdapter.notifyDataSetChanged();
        }

        Log.d(TAG, "Selection: " + selection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_source_picker, menu);
        mItemRemove = menu.getItem(0);
        mItemCounter = menu.getItem(1);
        mItemConfirm = menu.getItem(2);
        // Selection mode!
        setSelectionMode(false);
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
                setSelectionMode(false);
                return true;
            case R.id.action_confirm:
                // TODO: send images to main activity.
                // mSelectedImages.toArray()
                ImageSourcePicker.this.setResult(RESULT_OK);
                ImageSourcePicker.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(mSelection) {
            setSelectionMode(false);
        } else {
            super.onBackPressed();
        }
    }

    // OnItemSelected
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    /**
     * RecyclerViewAdapter with Items.
     */
    class PickerItem {
        private String mImagePath;
        private boolean mSelected;

        public PickerItem(String imagePath, boolean selected) {
            mImagePath = imagePath;
            mSelected = selected;
        }

        public void toggle() {
            mSelected = !mSelected;
        }
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private List<PickerItem> mItems;

        public RecyclerViewAdapter(List<PickerItem> items) {
            mItems = items;
        }

        /**
         * Создание новых View и ViewHolder элемента списка, которые впоследствии могут переиспользоваться.
         */
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(
                    R.layout.image_source_picker_item, viewGroup, false);
            return new ViewHolder(v);
        }

        /**
         * Заполнение виджетов View данными из элемента списка с номером i
         */
        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
            final PickerItem item = mItems.get(i);
            Picasso.with(ImageSourcePicker.this).load(new File(item.mImagePath))
                    .into(viewHolder.imageView);
            viewHolder.showSelection(item.mSelected);

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.toggle();
                    viewHolder.showSelection(item.mSelected);
                    ImageSourcePicker.this.selectImage(i);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        /**
         * Реализация класса ViewHolder, хранящего ссылки на виджеты.
         */
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
}
