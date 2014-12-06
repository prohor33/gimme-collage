package crystal.tech.gimmecollage.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import crystal.tech.gimmecollage.floating_action_btn.FloatingActionButton;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import crystal.tech.gimmecollage.collagemaker.CollageMaker;
import crystal.tech.gimmecollage.instagram_api.InstagramAPI;
import crystal.tech.gimmecollage.instagram_api.Storage;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";
    private static final int INSTAGRAM_FRIEND_REQUEST = 1;
    private static final int GALLERY_REQUEST = 2;
    private ProgressDialog m_dialogProgress = null;

    public class ImageData {
        ImageData(String url, int like_count) {
            m_strUrl = url;
            m_iLikeCount = like_count;
            m_bmpImage = null;
        }
        public String m_strUrl;
        public int m_iLikeCount;
        public Bitmap m_bmpImage;
    }

    private List<ImageData> m_lImages = new ArrayList<ImageData>();
    private final int m_iTemplateImageViewsID = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!InstagramAPI.initialized()) {
            InstagramAPI.init(this, ApplicationData.CLIENT_ID, ApplicationData.CLIENT_SECRET,
                    ApplicationData.CALLBACK_URL);
        }

        //ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);

        AddCollageTypeSelectorLayout();

        SortImages();

        AddCollageLayout();
        CollageMaker.getInstance().InitImageViews();

        AddFloatingActionButton();
    }

    private int mInstagramSelctedFriendID = -1;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case INSTAGRAM_FRIEND_REQUEST:
                if (data != null) {
                    int friendID = data.getIntExtra("intSelectedFriendID", 0);
                    if (friendID != mInstagramSelctedFriendID) {
                        SortImages();
                        ReloadCollageImages();
                    }
                }
                break;
            case GALLERY_REQUEST:
                if(resultCode == RESULT_OK && data != null) {
                    String[] galleryImagesPaths = data.getStringArrayExtra("selected");
                    // TODO: do smth with them.
                }
                break;
            default:
        }
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

        switch (id) {
            case R.id.action_share:
                ShareCollage();
                break;
            case R.id.action_save:
                SaveCollageOnDisk();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public interface FileSaveCallback {
        void onSuccess(File file_result);

        void onError();

        public static class EmptyFileSaveCallback implements FileSaveCallback {

            @Override public void onSuccess(File file_result) {
            }

            @Override public void onError() {
            }
        }
    }

    private void ShareCollage() {
        if (m_dialogProgress == null)
            m_dialogProgress = new ProgressDialog(MainActivity.this);
        m_dialogProgress.setTitle("Just a second");
        m_dialogProgress.setMessage("Generating collage for you...");
        m_dialogProgress.setCancelable(false);
        m_dialogProgress.show();

        Bitmap imgCollage = CollageMaker.getInstance().GenerateCollageImage();
        // Save file on disk and open share dialog
        new SaveFileTask().with(new FileSaveCallback() {
            @Override
            public void onSuccess(File file_result) {
                OpenShareDialog(file_result);
            }

            @Override
            public void onError() {

            }
        }).execute(imgCollage);
    }

    private class SaveFileTask extends AsyncTask<Bitmap, Void, File> {

        FileSaveCallback callback = null;

        public SaveFileTask with(FileSaveCallback cb) {
            callback = cb;
            return this;
        }

        protected File doInBackground(Bitmap... bmpImages) {
            Bitmap bmpImage = bmpImages[0];
            Log.v(TAG, "Start to save a file");

            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            OutputStream outStream = null;
            String temp = new String("collage");
            File file = new File(extStorageDirectory, temp + ".png");
            if (file.exists()) {
                file.delete();
                file = new File(extStorageDirectory, temp + ".png");
            }

            try {
                outStream = new FileOutputStream(file);
                bmpImage.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                outStream.flush();
                outStream.close();
            } catch (Exception e) {
                Log.v(TAG, "Error saving file: " + e.toString());
                return null;
            }
            return file;
        }

        protected void onPostExecute(File fileResult) {
            if (m_dialogProgress != null && m_dialogProgress.isShowing()) {
                m_dialogProgress.dismiss();
            }
            if (fileResult == null) {
                Log.v(TAG, "Error saving file");
                if (callback != null)
                    callback.onError();
            } else {
                Log.v(TAG, "File successfully saved");
                if (callback != null)
                    callback.onSuccess(fileResult);
            }
        }
    }

    private void OpenShareDialog(File fileResult) {
        if (fileResult == null) {
            Log.v(TAG, "Null file pointer");
            return;
        }

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("image/png");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Little candy");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                "Look! I have nice photo collage here, special for you!\nVia GimmeCollage");

        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileResult));
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    private void AddCollageTypeSelectorLayout() {
        final LinearLayout llTemplates = (LinearLayout) findViewById(R.id.layoutTemplates);
        for (int i = 0; i < CollageMaker.CollageType.values().length; i++) {
            final int selector_size = Utils.getScrSizeInPxls(MainActivity.this).y / 8;
            CollageTypeSelectorImageView ivSelector =
                    new CollageTypeSelectorImageView(MainActivity.this, null, selector_size, i);
            ivSelector.setId(m_iTemplateImageViewsID + i);
            ivSelector.setContentDescription(getString(R.string.desc));
            CollageMaker.getInstance().DrawCollageTypeSelector(ivSelector, i, selector_size);

            ivSelector.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // reset all the others
                    for (int i = 0; i < llTemplates.getChildCount(); i++) {
                        ImageView iv2 = (ImageView) llTemplates.getChildAt(i);
                        iv2.setColorFilter(0);
                    }

                    ImageView iv = (ImageView) v;
                    int index = iv.getId() - m_iTemplateImageViewsID;
                    CollageMaker.getInstance().MoveToTheOtherCollageType(index);
                }
            });

            llTemplates.addView(ivSelector);
        }
    }

    static public class Line {
        float startX, startY, stopX, stopY;
        public Line(float startX, float startY, float stopX, float stopY) {
            this.startX = startX;
            this.startY = startY;
            this.stopX = stopX;
            this.stopY = stopY;
        }
        public Line(float startX, float startY) { // for convenience
            this(startX, startY, startX, startY);
        }
    }

    public class CollageTypeSelectorImageView extends ImageView {
        private Paint currentPaint;
        private ArrayList<Line> lines = new ArrayList<Line>();
        private int selectorSize;
        private int selectorIndex;

        public CollageTypeSelectorImageView(Context context, AttributeSet attrs,
                                            int selector_size, int index) {
            super(context, attrs);

            currentPaint = new Paint();
            currentPaint.setDither(true);
            currentPaint.setColor(0xFFCCCCCC);  // alpha.r.g.b
            currentPaint.setStyle(Paint.Style.STROKE);
            currentPaint.setStrokeJoin(Paint.Join.ROUND);
            currentPaint.setStrokeCap(Paint.Cap.ROUND);
            currentPaint.setStrokeWidth(Utils.dipToPixels(MainActivity.this, 2.0f));

            selectorSize = selector_size;
            selectorIndex = index;
        }

        public void AddLine(Line line) {
            lines.add(line);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            currentPaint.setStyle(Paint.Style.FILL);
//            currentPaint.setColor(0xfffb8c00);  // orange
            currentPaint.setColor(0xfffba941);  // orange
            canvas.drawRect(0, 0, selectorSize, selectorSize, currentPaint);

            if (selectorIndex == CollageMaker.getInstance().getCollageTypeIndex()) {
                currentPaint.setColor(0xffffffff);  // white
            } else {
                currentPaint.setColor(0xFF918e8b);  // gray
            }
            currentPaint.setStyle(Paint.Style.STROKE);
            for (Line l : lines) {
                canvas.drawLine(l.startX, l.startY, l.stopX, l.stopY, currentPaint);
            }
        }
    }

    private void AddCollageLayout() {
        final int collage_padding = 45;
        int collage_size_x = Utils.getScrSizeInPxls(this).x - collage_padding * 2;
        CollageMaker.getInstance().putCollageSize(collage_size_x);
        CollageMaker.getInstance().putCollagePadding(new Point(collage_padding, 0));
        Log.v(TAG, "init()");

        final RelativeLayout rlCollage = (RelativeLayout)findViewById(R.id.layoutCollage);
        CollageMaker.getInstance().putCollageLayout(rlCollage);
        for (int i = 0; i < CollageMaker.getInstance().getMaxImageCount(); i++) {
            RelativeLayout rl = (RelativeLayout)getLayoutInflater().
                    inflate(R.layout.layout_collage_image, rlCollage, false);
            ImageView ivImage = (ImageView)rl.findViewById(R.id.imageView);
            final ProgressBar progressBar = (ProgressBar)rl.findViewById(R.id.progressBar);

            if (i < m_lImages.size()) {
                Picasso.with(MainActivity.this)
                       .load(m_lImages.get(i).m_strUrl)
                       .into(ivImage, new Callback() {
                           @Override
                           public void onSuccess() {
                               progressBar.setVisibility(View.GONE);
                           }

                           @Override
                           public void onError() {
                               progressBar.setVisibility(View.GONE);
                           }
                       });
            } else {
                ivImage.setImageResource(R.drawable.ic_add_file_action);
            }

            ivImage.setBackgroundResource(R.drawable.collage_image_back);
            ivImage.setPadding(0, 0, 0, 0);

            ivImage.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ImageView iv = (ImageView)v;
//                    int index = iv.getId() - m_iCollageImageViewsID;

                    ShowImageSourceDialog();
                }
            });

            rlCollage.addView(rl);
        }
    }

    private void ReloadCollageImages() {
        Log.v(TAG, "ReloadCollageImages() m_lImages.size() = " + m_lImages.size());
        final RelativeLayout rlCollage = (RelativeLayout)findViewById(R.id.layoutCollage);
        for (int i = 0; i < rlCollage.getChildCount(); i++) {
            RelativeLayout rl = (RelativeLayout) rlCollage.getChildAt(i);
            final ImageView iv = (ImageView) rl.getChildAt(0);
            final View pb = rl.getChildAt(1);
            if (i < m_lImages.size()) {
                pb.setVisibility(View.VISIBLE);
                Picasso.with(MainActivity.this)
                        .load(m_lImages.get(i).m_strUrl)
                        .into(iv, new Callback() {
                            @Override
                            public void onSuccess() {
                                pb.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {
                                pb.setVisibility(View.GONE);
                            }
                        });
            } else {
                iv.setImageResource(R.drawable.ic_add_file_action);
                pb.setVisibility(View.GONE);
            }
        }
    }

    private void AddFloatingActionButton() {
        final FloatingActionButton mFab0 = (FloatingActionButton)findViewById(R.id.fabbutton);
        mFab0.setColor(getResources().getColor(R.color.android_green));
        mFab0.setDrawable(getResources().getDrawable(R.drawable.ic_navigation_accept));
        mFab0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FloatingActionButton mFab1 = (FloatingActionButton) findViewById(R.id.fabbutton0);
                mFab1.hide(!mFab1.getHidden());
                FloatingActionButton mFab2 = (FloatingActionButton) findViewById(R.id.fabbutton1);
                mFab2.hide(!mFab2.getHidden());
            }
        });

        FloatingActionButton mFab1 = (FloatingActionButton)findViewById(R.id.fabbutton0);
        mFab1.setColor(getResources().getColor(R.color.purple));    // maroon
        mFab1.setDrawable(getResources().getDrawable(R.drawable.ic_action_content_save));
        mFab1.setParentFAB(mFab0);
        mFab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // for debug
//                InstagramAPI.resetAuthentication();
//                Toast.makeText(MainActivity.this, "Authentication has been reset",
//                        Toast.LENGTH_LONG).show();
                SaveCollageOnDisk();
            }
        });

        FloatingActionButton mFab2 = (FloatingActionButton)findViewById(R.id.fabbutton1);
        mFab2.setColor(getResources().getColor(R.color.action_btn_clr));
        mFab2.setDrawable(getResources().getDrawable(R.drawable.ic_action_share));
        mFab2.setParentFAB(mFab0);
        mFab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareCollage();
            }
        });
    }

    private void ShowImageSourceDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                MainActivity.this);
        builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle("Select Image Source");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                MainActivity.this,
                android.R.layout.select_dialog_singlechoice);
        final String strInstagram = "Instagram";
        final String strGallery = "Gallery";
        arrayAdapter.add(strInstagram);
        arrayAdapter.add(strGallery);
        builderSingle.setNegativeButton("cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);

                        if (strName == strInstagram) {
                            Intent intent = new Intent(MainActivity.this, FriendPicker.class);
                            startActivityForResult(intent, INSTAGRAM_FRIEND_REQUEST);
                        } else {
                            Intent intent = new Intent(MainActivity.this, GalleryPicker.class);
                            startActivityForResult(intent, GALLERY_REQUEST);
                            /*
                            AlertDialog.Builder builderInner = new AlertDialog.Builder(
                                    MainActivity.this);
                            builderInner.setTitle("Sorry");
                            builderInner.setMessage("Not implemented yet");
                            builderInner.setPositiveButton("Ok",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            builderInner.show();
                            */
                        }
                    }
                });
        builderSingle.show();
    }

    private void SortImages() {
        List<Storage.ImageInfo> imagesInfo = InstagramAPI.getImages();
        Log.v(TAG, "have " + imagesInfo.size() + " images");
        m_lImages.clear();
        for (int i = 0; i < imagesInfo.size(); i++) {
            m_lImages.add(new ImageData(imagesInfo.get(i).standard_resolution.url,
                    imagesInfo.get(i).likes_count));
        }

        class ImageComparator implements Comparator<ImageData> {
            @Override
            public int compare(ImageData o1, ImageData o2) {
                return o2.m_iLikeCount - o1.m_iLikeCount;
            }
        }

        // Let's sort images by likes count
        Collections.sort(m_lImages, new ImageComparator());
    }

    private void SaveCollageOnDisk() {
        if (m_dialogProgress == null)
            m_dialogProgress = new ProgressDialog(MainActivity.this);
        m_dialogProgress.setTitle("Just a second");
        m_dialogProgress.setMessage("Saving collage...");
        m_dialogProgress.setCancelable(false);
        m_dialogProgress.show();

        Bitmap imgCollage = CollageMaker.getInstance().GenerateCollageImage();
        // Save file on disk and open share dialog
        new SaveFileTask().with(new FileSaveCallback() {
            @Override
            public void onSuccess(File file_result) {
                String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
                extStorageDirectory += "/" + file_result.getName();
                Toast.makeText(MainActivity.this, "Collage saved to " + extStorageDirectory,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError() {

            }
        }).execute(imgCollage);
    }
}