package crystal.games.gimmecollage.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import crystal.games.gimmecollage.floating_action_btn.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import crystal.games.gimmecollage.collagemaker.CollageMaker;
import crystal.games.gimmecollage.instagram_api.InstagramAPI;
import crystal.games.gimmecollage.instagram_api.Storage;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";

    private class ImageData {
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
    private Bitmap m_imgCollage = null;
    private final int m_iTemplateImageViewsID = 100;
    private final int m_iCollageImageViewsID = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialization of InstagramAPI...!
        InstagramAPI.init(this, ApplicationData.CLIENT_ID, ApplicationData.CLIENT_SECRET,
                ApplicationData.CALLBACK_URL);
//        InstagramAPI.resetAuthentication();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        m_imgCollage = null;

        AddCollageTypeSelectorLayout();

        List<Storage.ImageInfo> imagesInfo = InstagramAPI.getImages();
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

        AddCollageLayout();
        CollageMaker.getInstance().InitImageViews();

        FloatingActionButton mFab0 = (FloatingActionButton)findViewById(R.id.fabbutton0);
        mFab0.setColor(getResources().getColor(R.color.purple));    // maroon
        mFab0.setDrawable(getResources().getDrawable(R.drawable.ic_content_discard));
        mFab0.hide(true);
        FloatingActionButton mFab1 = (FloatingActionButton)findViewById(R.id.fabbutton1);
        mFab1.setColor(getResources().getColor(R.color.android_green));
        mFab1.setDrawable(getResources().getDrawable(R.drawable.ic_navigation_accept));
        mFab1.hide(true);

        final FloatingActionButton mFab = (FloatingActionButton)findViewById(R.id.fabbutton);
        mFab.setColor(getResources().getColor(R.color.action_btn_clr));
        mFab.setDrawable(getResources().getDrawable(R.drawable.ic_action_plus));
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FloatingActionButton mFab0 = (FloatingActionButton)findViewById(R.id.fabbutton0);
                mFab0.hide(!mFab0.getHidden());
                FloatingActionButton mFab1 = (FloatingActionButton)findViewById(R.id.fabbutton1);
                mFab1.hide(!mFab1.getHidden());
            }
        });

        //LoadImagesAndUniteToOne();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image_processor, menu);
        return true;
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

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        int m_iImageIndex;

        public DownloadImageTask(int image_index) {
            this.m_iImageIndex = image_index;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.v(TAG, "Error: " + e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            Log.v(TAG, "Image number " + m_iImageIndex + " successfully loaded");
            onImageLoad(m_iImageIndex, result);
        }
    }

    private void onImageLoad(int index, Bitmap image) {
        m_lImages.get(index).m_bmpImage = image;

        // Check if all images are loaded
        int images_loaded = 0;
        for (ImageData img : m_lImages) {
            images_loaded += img.m_bmpImage != null ? 1 : 0;
        }
        if (images_loaded == m_lImages.size()) {
            Log.v(TAG, "All images are successfully loaded!");
            UniteImagesToOne();
        }
    }

    private void UniteImagesToOne() {
        // for now support only standard resolution
        // TODO: support multiple resolutions
        int img_size_x = 612;
        int img_size_y = 612;

        int image_count_x = 0;
        int image_count_y = 0;

        // Pick collage size
        int collage_images_count = m_lImages.size();
        switch (collage_images_count) {
            case 2:
                image_count_x = 2;
                image_count_y = 1;
                break;
            case 4:
                image_count_x = 2;
                image_count_y = 2;
                break;
            case 6:
                image_count_x = 3;
                image_count_y = 2;
                break;
            case 12:
                image_count_x = 4;
                image_count_y = 3;
                break;
            case 20:
                image_count_x = 5;
                image_count_y = 4;
                break;
            default:
                Log.v(TAG, "Error: wrong collage images count: " + collage_images_count);
                return;
        }

        Bitmap collageImage = Bitmap.createBitmap(image_count_x * img_size_x,
                image_count_y * img_size_y, Bitmap.Config.RGB_565);
        Canvas comboCanvas = new Canvas(collageImage);

        for (int x = 0; x < image_count_x; x++) {
            for (int y = 0; y < image_count_y; y++) {
                comboCanvas.drawBitmap(m_lImages.get(x + y * image_count_x).m_bmpImage,
                        img_size_x * x, img_size_y * y, null);
            }
        }

        Log.v(TAG, "Collage is successfully generated!");

//        ImageView ivCollage = (ImageView)findViewById(R.id.imageView);
//        ivCollage.setImageBitmap(collageImage);
        m_imgCollage = collageImage;
    }

    private void ShareCollage() {
        if (m_imgCollage == null) {
            Log.v(TAG, "Error: collage isn't ready yet");
        }
        // Save file on disk and open share dialog
        new SaveFileTask().execute(m_imgCollage);
    }

    private class SaveFileTask extends AsyncTask<Bitmap, Void, File> {

        private ProgressDialog m_dialogProgress = new ProgressDialog(MainActivity.this);

        protected File doInBackground(Bitmap... bmpImages) {
            Bitmap bmpImage = bmpImages[0];
            Log.v(TAG, "Start to save a file");
//            m_dialogProgress.setMessage("Saving file on disk...");
//            m_dialogProgress.setCancelable(false);
//            m_dialogProgress.show();

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
//            if (m_dialogProgress.isShowing()) {
//                m_dialogProgress.dismiss();
//            }
            if (fileResult == null) {
                Log.v(TAG, "Error saving file");
            } else {
                Log.v(TAG, "File successfully saved");
                onCollageFileSave(fileResult);
            }
        }
    }

    private void onCollageFileSave(File fileResult) {
        if (fileResult == null) {
            Log.v(TAG, "Null file pointer");
            return;
        }

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("image/png");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Text");

        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileResult));
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    private void AddCollageTypeSelectorLayout() {
        final LinearLayout llTemplates = (LinearLayout) findViewById(R.id.layoutTemplates);
        for (int i = 0; i < CollageMaker.CollageType.values().length; i++) {
            final int selector_size = 90;
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
            currentPaint.setStrokeWidth(3);

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
        int collage_size_x = Utils.getScreenSizeInPixels(this).x - collage_padding * 2;
        CollageMaker.getInstance().putCollageSize(collage_size_x);
        CollageMaker.getInstance().putCollagePaddingX(collage_padding);
        CollageMaker.getInstance().putCollagePaddingY(collage_padding / 2);
        Log.v(TAG, "init()");

        final RelativeLayout rlCollage = (RelativeLayout)findViewById(R.id.layoutCollage);
        CollageMaker.getInstance().putCollageLayout(rlCollage);
        for (int i = 0; i < CollageMaker.getInstance().getMaxImageCount(); i++) {
            ImageView ivImage = new ImageView(MainActivity.this);
            ivImage.setId(m_iCollageImageViewsID + i);

            if (i < m_lImages.size()) {
                Picasso.with(MainActivity.this).load(m_lImages.get(i).m_strUrl).into(ivImage);
            } else {
                ivImage.setImageResource(R.drawable.ic_plus_image);
            }

            ivImage.setBackgroundResource(R.drawable.collage_image_back);
            ivImage.setPadding(0, 0, 0, 0);

            ivImage.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ImageView iv = (ImageView)v;
//                    int index = iv.getId() - m_iCollageImageViewsID;

                    // Start LoginActivity activity
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            });

            rlCollage.addView(ivImage);
        }
    }

    private void LoadImagesAndUniteToOne() {
        int image_count = m_lImages.size();
        int collage_image_count = 0;
        ArrayList<Integer> lCollageImageCount = new ArrayList<Integer>(Arrays.asList(2, 4, 6, 12, 20));
        for (Integer good_image_count : lCollageImageCount) {
            if (image_count >= good_image_count)
                collage_image_count = good_image_count;
        }

        if (collage_image_count == 0) {
            Log.v(TAG, "Too few images for collage: " + image_count);
            return;
        }

        Log.v(TAG, "Pick " + collage_image_count + " images for collage.");

        for (int i = 0; i < (image_count - collage_image_count); i++)
            m_lImages.remove(m_lImages.size() - 1);

        int index = 0;
        for (ImageData image : m_lImages) {
            new DownloadImageTask(index++).execute(image.m_strUrl);
        }
    }
}
