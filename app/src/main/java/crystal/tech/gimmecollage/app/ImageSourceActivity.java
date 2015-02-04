package crystal.tech.gimmecollage.app;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import crystal.tech.gimmecollage.utility.DividerItemDecoration;

/**
 * Created by Дмитрий on 28.01.2015.
 */
public class ImageSourceActivity extends ActionBarActivity {

    private final String TAG = "ImageSourceActivity";

    private final int IMAGE_SOURCE_GALLERY = 0;
    private final int IMAGE_SOURCE_INSTAGRAM = 1;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_source);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        List<ImageSourceItem> imageSourceItems = new ArrayList<>();
        populateImageSourceItems(imageSourceItems);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);


        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new RecyclerViewAdapter(imageSourceItems));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void populateImageSourceItems(List<ImageSourceItem> items) {
        items.add(new ImageSourceItem(R.string.image_source_gallery, R.drawable.ic_gallery));
        items.add(new ImageSourceItem(R.string.image_source_instagram, R.drawable.ic_instagram));

        /*
        for (int i = 0; i < 100; i++) {
            ImageSourceItem item = new ImageSourceItem();
            item.setText("Instagram");
            item.setDrawable(R.drawable.ic_instagram);
            items.add(item);
        }
        */
    }

    private void selectImageSource(int i) {
        // Check if image source is login???
        // Open AuthActivity or ImageSourceGallery ??
        switch (i) {
            case IMAGE_SOURCE_GALLERY:
                // Open some special activity with folders .. or something.
                // Send special activity ???
                Intent intent = new Intent(ImageSourceActivity.this, ImageSourcePicker.class);
                startActivity(intent);
                break;
            case IMAGE_SOURCE_INSTAGRAM:
                break;
            default:
                break;
        }
        Log.d(TAG, "selectImageSource " + i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.image_source_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch(item.getItemId()) {
            case R.id.action_settings:
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * RecyclerViewAdapter with Items.
     */
    class ImageSourceItem {
        private String mText;
        private Drawable mDrawable;

        public ImageSourceItem() {}

        public ImageSourceItem(String text, Drawable drawable) {
            mText = text;
            mDrawable = drawable;
        }

        public ImageSourceItem(String text, int id) {
            mText = text;
            mDrawable = getResources().getDrawable(id);
        }

        public ImageSourceItem(int text_id, int drawable_id) {
            mText = getResources().getString(text_id);
            mDrawable = getResources().getDrawable(drawable_id);
        }
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private List<ImageSourceItem> mItems;

        public RecyclerViewAdapter(List<ImageSourceItem> items) {
            mItems = items;
        }

        /**
         * Создание новых View и ViewHolder элемента списка, которые впоследствии могут переиспользоваться.
         */
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_image_source_item, viewGroup, false);
            return new ViewHolder(v);
        }

        /**
         * Заполнение виджетов View данными из элемента списка с номером i
         */
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int i) {
            ImageSourceItem item = mItems.get(i);
            viewHolder.textView.setText(item.mText);
            viewHolder.imageView.setImageDrawable(item.mDrawable);
            //viewHolder.textView.setCompoundDrawablesWithIntrinsicBounds(item.getDrawable(), null, null, null);

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageSourceActivity.this.selectImageSource(i);
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
            private TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);

                imageView = (ImageView) itemView.findViewById(R.id.imageView);
                textView = (TextView) itemView.findViewById(R.id.textView);
            }
        }
    }
}
