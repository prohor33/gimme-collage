package crystal.tech.gimmecollage.app;

import android.app.ProgressDialog;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import crystal.tech.gimmecollage.instagram_api.InstagramAPI;
import crystal.tech.gimmecollage.utility.DividerItemDecoration;

/**
 * Created by Дмитрий on 28.01.2015.
 */
public class ImageSourceActivity extends ActionBarActivity {

    private final String TAG = "ImageSourceActivity";

    static final int GALLERY_REQUEST = 0;
    static final int INSTAGRAM_REQUEST = 1;
    static final int INSTAGRAM_AUTH_REQUEST = 2;

    private List<ImageSourceItem> mImageSourceItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_source);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        populateImageSourceItems(mImageSourceItems);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new RecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void populateImageSourceItems(List<ImageSourceItem> items) {
        items.clear();

        items.add(new ImageSourceItem(R.string.image_source_gallery, R.drawable.ic_source_picker_gallery));
        items.add(new ImageSourceItem(R.string.image_source_instagram, R.drawable.ic_instagram));
    }

    private void selectImageSource(int i) {
        // Check if image source is login???
        // Open AuthActivity or ImageSourceGallery ??
        switch (i) {
            case 0: // Gallery
                startActivityForResult(new Intent(ImageSourceActivity.this,
                        ImagePickerActivity.class), GALLERY_REQUEST);
                break;
            case 1: // Instagram
                // Check if instagram is logged.
                if(!InstagramAPI.isAuthenticated()) {
                    if(Utils.checkAndNotifyConnection(this)) {
                        startActivityForResult(new Intent(ImageSourceActivity.this,
                                AuthenticationActivity.class), INSTAGRAM_AUTH_REQUEST);
                    }
                } else {
                    // if it is authenticated, then we need to update self info first.
                    final ProgressDialog dialog = Utils.createProgressDialog(ImageSourceActivity.this);
                    dialog.show();
                    InstagramAPI.with(new InstagramAPI.Listener() {
                        @Override
                        public void onSuccess() {
                            dialog.dismiss();
                            startActivityForResult(new Intent(ImageSourceActivity.this,
                                    ImagePickerActivity.class), INSTAGRAM_REQUEST);
                            Log.d(TAG, "onSuccess");
                        }

                        @Override
                        public void onFail(String error) {
                            dialog.dismiss();
                            Log.e(TAG, "Error: " + error);
                            if(Utils.checkAndNotifyConnection(ImageSourceActivity.this)) {
                                Toast.makeText(ImageSourceActivity.this,
                                        "Wrong access token, please try again.",
                                        Toast.LENGTH_LONG).show();
                                InstagramAPI.resetAuthentication();
                            }
                        }
                    }).updateSelf();
                }
                break;
            default:
                break;
        }
        Log.d(TAG, "selectImageSource " + i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.image_source, menu);
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

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        intent.putExtra("requestCode", requestCode);
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, requestCode + " " + resultCode);

        if (requestCode == GALLERY_REQUEST) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "setResult = " + RESULT_OK);
                ImageSourceActivity.this.setResult(RESULT_OK);
                ImageSourceActivity.this.finish();
            }
        } else if (requestCode == INSTAGRAM_REQUEST) {
            if (resultCode == RESULT_OK) {
                ImageSourceActivity.this.setResult(RESULT_OK);
                ImageSourceActivity.this.finish();
            }
        } else if (requestCode == INSTAGRAM_AUTH_REQUEST) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(ImageSourceActivity.this, ImagePickerActivity.class);
                startActivityForResult(intent, INSTAGRAM_REQUEST);
            }
        }
    }

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

        public RecyclerViewAdapter() {}

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.image_source_item, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int i) {
            ImageSourceItem item = mImageSourceItems.get(i);
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
            return mImageSourceItems.size();
        }

        // Special ViewHolder class for holding recylcer view elements.
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
