package crystal.tech.gimmecollage.app;

import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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

/**
 * Created by Дмитрий on 28.01.2015.
 */
public class ImageSourceActivity extends ActionBarActivity {

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
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(imageSourceItems);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(itemAnimator);
    }

    private void populateImageSourceItems(List<ImageSourceItem> items) {
        for (int i = 0; i<10; i++) {
            ImageSourceItem item = new ImageSourceItem();
            item.setText("Instagram");
            item.setDrawable(R.drawable.ic_instagram);
            items.add(item);
        }
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

        public String getText() {
            return mText;
        }

        public void setText(String text) {
            mText = text;
        }

        public Drawable getDrawable() {
            return mDrawable;
        }

        public void setDrawable(Drawable drawable) {
            mDrawable = drawable;
        }

        public void setDrawable(int id) {
            mDrawable = getResources().getDrawable(id);
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
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            ImageSourceItem item = mItems.get(i);
            viewHolder.textView.setText(item.getText());
            //viewHolder.textView.setCompoundDrawablesWithIntrinsicBounds(item.getDrawable(), null, null, null);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        /**
         * Реализация класса ViewHolder, хранящего ссылки на виджеты.
         */
        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(R.id.textView);
            }
        }
    }
}
