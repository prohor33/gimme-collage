package crystal.tech.gimmecollage.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;

import crystal.tech.gimmecollage.instagram_api.InstagramAPI;


public class LoginActivity extends ActionBarActivity {

    private static final String TAG = "LoginActivity";

    private static final int TABLE_NUM_ROWS = 2;
    private static final int TABLE_NUM_COLUMNS = 4;

    private View mLayoutInstagram;
    private View mLayoutGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ViewGroup ll = (ViewGroup) findViewById(R.id.loginLayout);

        mLayoutInstagram = getLayoutInflater().inflate(R.layout.layout_image_source, ll, false);
        mLayoutGallery = getLayoutInflater().inflate(R.layout.layout_image_source, ll, false);

        ll.addView(mLayoutInstagram);
        ll.addView(mLayoutGallery);

        reloadStaticTitle(mLayoutInstagram, R.drawable.ic_instagram,
                getResources().getString(R.string.source_instagram_name));
        updateImageSource(mLayoutInstagram);


        reloadStaticTitle(mLayoutGallery, R.drawable.ic_gallery, "Gallery");
        reloadDynamicTitle(mLayoutGallery, "Last pictures.", "", null);
        reloadPreviewImages(mLayoutGallery, getGalleryImages(TABLE_NUM_COLUMNS * TABLE_NUM_ROWS));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Result of closing auth activity.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            InstagramAPI.with(new InstagramAPI.Listener() {
                @Override
                public void onSuccess() {
                    reloadPreviewImages(mLayoutInstagram, getInstagramImages(TABLE_NUM_COLUMNS * TABLE_NUM_ROWS));
                }

                @Override
                public void onFail(String error) {
                    Toast.makeText(LoginActivity.this, "Error: Can't load images.",
                            Toast.LENGTH_SHORT).show();
                }
            }).updateImages("self");
            updateImageSource(mLayoutInstagram);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Title. not changing part.
    private void reloadStaticTitle(View imageSourceLayout, int icon, String name) {

        /* Get Title layout */
        View titleLayout = imageSourceLayout.findViewById(R.id.titleLayout);

        ImageView titleImageView = (ImageView) titleLayout.findViewById(R.id.imageView);
        titleImageView.setImageResource(icon);

        TextView textView = (TextView) titleLayout.findViewById(R.id.textView);
        textView.setText(name);
    }

    // Title, changing part.
    private void reloadDynamicTitle(View imageSourceLayout, String hint, String buttonName,
                                    OnClickListener listener) {

        /* Get Title layout */
        View titleLayout = imageSourceLayout.findViewById(R.id.titleLayout);

        Button button = (Button) titleLayout.findViewById(R.id.button);
        if (buttonName.isEmpty()) {
            button.setVisibility(View.GONE);
        } else {
            button.setVisibility(View.VISIBLE);
            button.setText(buttonName);
            button.setOnClickListener(listener);
        }

        /* Get TextHint */
        TextView textViewHint = (TextView) imageSourceLayout.findViewById(R.id.textViewHint);
        textViewHint.setText(hint);
    }

    // Images preview.
    private void reloadPreviewImages(View imageSourceLayout, String[] sourceImages) {
        /* Get TableView for favorite photos */
        LinearLayout imagesLayout = (LinearLayout) imageSourceLayout.findViewById(R.id.imagesLayout);
        imagesLayout.removeAllViews();
        if (sourceImages.length > 0) {

            int numRows = (int) Math.ceil(1.0f * sourceImages.length / TABLE_NUM_COLUMNS);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.f);
            for (int i = 0; i < numRows; i++) {
                LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setLayoutParams(lp);
                for (int j = 0; j < TABLE_NUM_COLUMNS; j++) {
                    ImageView imageView = new SquareImageView(this);
                    imageView.setLayoutParams(lp);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    int imageIndex = i * TABLE_NUM_COLUMNS + j;
                    if (imageIndex < sourceImages.length) {
                        if (sourceImages[imageIndex].startsWith("http")) {
                            Picasso.with(this).load(sourceImages[imageIndex]).into(imageView);
                        } else {
                            Picasso.with(this).load(new File(sourceImages[imageIndex]))
                                    .into(imageView);
                        }
                    }

                    linearLayout.addView(imageView);
                }
                imagesLayout.addView(linearLayout);
            }
        }
    }

    // Update login status.
    private void updateImageSource(final View imageSourceView) {
        if (InstagramAPI.isAuthenticated()) {
            OnClickListener logoutListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    InstagramAPI.resetAuthentication();
                    updateImageSource(imageSourceView);
                }
            };
            reloadDynamicTitle(imageSourceView,
                    getResources().getString(R.string.source_instagram_hint_logged),
                    getResources().getString(R.string.source_instagram_logout),
                    logoutListener);
            reloadPreviewImages(imageSourceView, getInstagramImages(TABLE_NUM_COLUMNS * TABLE_NUM_ROWS));
        } else {
            OnClickListener loginListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LoginActivity.this, AuthenticationActivity.class);
                    startActivityForResult(intent, 1);
                }
            };
            reloadDynamicTitle(imageSourceView,
                    getResources().getString(R.string.source_instagram_hint_not_logged),
                    getResources().getString(R.string.source_instagram_login),
                    loginListener);
        }
    }

    // Utility
    private String[] getGalleryImages(int numImages) {
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
        numImages = (count < numImages) ? count : numImages;

        String[] thumbnails = new String[numImages];

        imageCursor.moveToLast();
        for (int i = 0; i < numImages; i++) {
            thumbnails[i] = imageCursor.getString(data_column_index);
            imageCursor.moveToPrevious();
        }
        imageCursor.close();

        return thumbnails;
    }

    private String[] getInstagramImages(int numImages) {
        String[] instagramImages = new String[InstagramAPI.getImages().size()];
        for (int i = 0; (i < InstagramAPI.getImages().size()) && (i < numImages); i++) {
            instagramImages[i] = InstagramAPI.getImages().get(i).thumbnail.url;
        }
        return instagramImages;
    }

}
