package crystal.games.gimmecollage.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;

import crystal.games.gimmecollage.instagram_api.InstagramApp;
import crystal.games.gimmecollage.instagram_api.InstagramSession;

public class LoginActivity extends ActionBarActivity {

    private static final String TAG = "LoginActivity";

    private static final int TABLE_NUM_ROWS = 2;
    private static final int TABLE_NUM_COLUMNS = 4;

    private void configureImageSource(ViewGroup viewImageSource, int drawSource, String name,
                                      String buttonName, String hintMessage,
                                      String[] sourceImages) {

        /* Get Title layout */
        View titleLayout = viewImageSource.findViewById(R.id.titleLayout);

        ImageView titleImageView = (ImageView) titleLayout.findViewById(R.id.imageView);
        titleImageView.setImageResource(drawSource);

        TextView textView = (TextView) titleLayout.findViewById(R.id.textView);
        textView.setText(name);

        Button button = (Button) titleLayout.findViewById(R.id.button);
        if (buttonName.isEmpty()) {
            button.setVisibility(View.GONE);
        } else {
            button.setVisibility(View.VISIBLE);
            button.setText(buttonName);
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LoginActivity.this, AuthenticationActivity.class);
                    startActivity(intent);
                }
            });
        }

        /* Get TextHint */
        TextView textViewHint = (TextView) viewImageSource.findViewById(R.id.textViewHint);
        textViewHint.setText(hintMessage);
        /* Get TableView for favorite photos */
        if (sourceImages.length == (TABLE_NUM_ROWS * TABLE_NUM_COLUMNS)) {

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.f);
            for (int i = 0; i < TABLE_NUM_ROWS; i++) {
                LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setLayoutParams(lp);
                for (int j = 0; j < TABLE_NUM_COLUMNS; j++) {
                    ImageView imageView = new SquareImageView(this);
                    imageView.setLayoutParams(lp);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    Picasso.with(this).load(new File(sourceImages[i * TABLE_NUM_COLUMNS + j]))
                            .into(imageView);
                    linearLayout.addView(imageView);
                }
                viewImageSource.addView(linearLayout);
            }


        }


    }

    private String[] getLastGalleryImages(int numImages) {

        // Define which columns we need from sql table.
        final String[] columns = { MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Thumbnails._ID };
        Cursor imageCursor = getContentResolver().query(
                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                columns,
                null,
                null,
                MediaStore.Images.Thumbnails._ID); // we sort results by ?name?

        int image_column_index = imageCursor.getColumnIndex(MediaStore.Images.Thumbnails._ID);
        int data_column_index = imageCursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA);


        int count = imageCursor.getCount();
        numImages = (count < numImages) ? count : numImages;

        String[] thumbnails = new String[numImages];

        imageCursor.moveToLast();
        for (int i = 0; i < numImages; i++) {

            // picture id for finding thumbnail.
            long id = imageCursor.getLong(image_column_index);
            // we take image path.
            thumbnails[i] = imageCursor.getString(data_column_index);

            Log.i("IMAGE_PATH", thumbnails[i]);

            imageCursor.moveToPrevious();
        }
        imageCursor.close();

        return thumbnails;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        ViewGroup loginLayout = (ViewGroup) findViewById(R.id.loginLayout);

        // Instagram
        ViewGroup viewInstagram =  (ViewGroup) getLayoutInflater()
                .inflate(R.layout.layout_image_source, loginLayout, false);
        loginLayout.addView(viewInstagram);

        String[] favouriteInstagram = {};
        configureImageSource(viewInstagram,
                R.drawable.no_photo,
                "Instagram",
                "SignUp",
                "Please sign up with you Instagram account.",
                favouriteInstagram);

        // Gallery
        ViewGroup viewGallery =  (ViewGroup) getLayoutInflater()
                .inflate(R.layout.layout_image_source, loginLayout, false);
        loginLayout.addView(viewGallery);

        String[] favouriteGallery = getLastGalleryImages(TABLE_NUM_ROWS * TABLE_NUM_COLUMNS);
        configureImageSource(viewGallery,
                R.drawable.ic_gallery,
                "Gallery",
                "",
                "Last pictures.",
                favouriteGallery);


        /*
        InstagramApp.getInstance().Init(this, ApplicationData.CLIENT_ID,
                ApplicationData.CLIENT_SECRET, ApplicationData.CALLBACK_URL);

        tvSummary = (TextView) findViewById(R.id.textView);

        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (InstagramApp.getInstance().getSession().hasAccessToken()) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(
                            LoginActivity.this);
                    builder.setMessage("Disconnect from Instagram?")
                            .setCancelable(false)
                            .setPositiveButton("Yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            InstagramApp.getInstance().getSession().resetAccessToken();
                                            btnConnect.setText("Connect");
                                            tvSummary.setText("Not connected");
                                        }
                                    })
                            .setNegativeButton("No",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    final AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    InstagramApp.getInstance().authorize(auth_listener);
                }
            }
        });

        btnGetImages = (Button)findViewById(R.id.btnPickFriend);
        btnGetImages.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {

                if (!InstagramApp.getInstance().getSession().hasAccessToken()) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(
                            LoginActivity.this);
                    builder.setMessage("You are not connected. Connect to Instagram?")
                            .setCancelable(false)
                            .setPositiveButton("Yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            m_eNextTask = Task.FetchFriends;
                                            InstagramApp.getInstance().authorize(auth_listener);
                                        }
                                    })
                            .setNegativeButton("No",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    final AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    runFetchingFriends();
                }
            }
        });

        if (InstagramApp.getInstance().getSession().hasAccessToken()) {
            tvSummary.setText("Connected as " + InstagramApp.getInstance().getSession().getSelfUserInfo().username);
            btnConnect.setText("Disconnect");
        }

        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
/*
    InstagramApp.APIRequestListener auth_listener = new InstagramApp.APIRequestListener() {

        @Override
        public void onSuccess() {
            tvSummary.setText("Connected as "
                    + InstagramApp.getInstance().getSession().getSelfUserInfo().username);
            btnConnect.setText("Disconnect");
            if (m_eNextTask == Task.FetchFriends) {
                m_eNextTask = Task.None;
                runFetchingFriends();
            }
        }

        @Override
        public void onFail(String error) {
            Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
            m_eNextTask = Task.None;
        }
    };
*/
    InstagramApp.APIRequestListener friends_load_listener = new InstagramApp.APIRequestListener() {

        @Override
        public void onSuccess() {
            Log.v(TAG, "Friends info successfully loaded!");
            Intent intent = new Intent(LoginActivity.this, FriendPicker.class);

            startActivity(intent);
            Log.v(TAG, "Start FriendPicker activity");
        }

        @Override
        public void onFail(String error) {
            Log.v(TAG, "Failed to load friends info");
            Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
        }
    };

    private void runFetchingFriends() {
        InstagramApp.getInstance().updateSelfFollows(friends_load_listener);
    }

}
