package crystal.games.gimmecollage.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import crystal.games.gimmecollage.collagemaker.CollageConfig;
import crystal.games.gimmecollage.collagemaker.CollageMaker;
import crystal.games.gimmecollage.collagemaker.PhotoPosition;
import crystal.games.gimmecollage.instagram_api.InstagramSession;


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
    private TextView m_tvSummary;
    private Bitmap m_imgCollage = null;
    private Button m_btnShare;
    private final int m_iTemplateImageViewsID = 100;
    private final int m_iCollageImageViewsID = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        m_imgCollage = null;

        m_btnShare = (Button) findViewById(R.id.btnShare);
        m_btnShare.setEnabled(false);
        m_btnShare.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                shareCollage();
            }
        });

        final LinearLayout llTemplates = (LinearLayout) findViewById(R.id.layoutTemplates);
        final int template_count = 7;
        for (int i = 1; i <= template_count; i++) {
            ImageView ivTemplate = new ImageView(MainActivity.this);
            ivTemplate.setId(m_iTemplateImageViewsID + i);
            ivTemplate.setContentDescription(getString(R.string.desc));
            int img_id = this.getResources().getIdentifier("collage_template_" + i,
                    "drawable", this.getPackageName());
            ivTemplate.setImageResource(img_id);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100, 100);
            ivTemplate.setLayoutParams(layoutParams);

            ivTemplate.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // reset all the others
                    for (int i = 0; i < llTemplates.getChildCount(); i++) {
                        ImageView iv2 = (ImageView)llTemplates.getChildAt(i);
                        iv2.setColorFilter(0);
                    }

                    ImageView iv = (ImageView)v;
                    int index = iv.getId() - m_iTemplateImageViewsID;
                    iv.setColorFilter(Color.parseColor("#33B5E5"), PorterDuff.Mode.MULTIPLY);
                    CollageMaker.getInstance().moveToTheOtherCollageType(index);
                }
            });

            llTemplates.addView(ivTemplate);
        }

        // for debug
//        List<InstagramSession.ImageInfo> imageInfos =
//                InstagramApp.getInstance().getSession().getImageInfos();
        List<InstagramSession.ImageInfo> imageInfos =
                new ArrayList<InstagramSession.ImageInfo>();

        m_tvSummary = (TextView) findViewById(R.id.textView);
        m_tvSummary.setText("Image urls to load: " + imageInfos.size());
        m_tvSummary.setVisibility(View.GONE);

        m_lImages.clear();
        for (int i = 0; i < imageInfos.size(); i++) {
            m_lImages.add(new ImageData(imageInfos.get(i).standard_resolution.url,
                    imageInfos.get(i).likes_count));
        }

        class ImageComparator implements Comparator<ImageData> {
            @Override
            public int compare(ImageData o1, ImageData o2) {
                return o2.m_iLikeCount - o1.m_iLikeCount;
            }
        }

        // Let's sort images by likes count
        Collections.sort(m_lImages, new ImageComparator());

        CollageMaker.getInstance().putCollageType(CollageMaker.CollageType.CenterWithGridAround);
        CollageConfig pCollageConf = CollageMaker.getInstance().getCollageConf();

        final RelativeLayout rlCollage = (RelativeLayout)findViewById(R.id.layoutCollage);
        for (int i = 0; i < pCollageConf.getPhotoCount(); i++) {
            ImageView ivImage = new ImageView(MainActivity.this);
            ivImage.setId(m_iCollageImageViewsID + i);

            if (i < m_lImages.size()) {
                Picasso.with(MainActivity.this).load(m_lImages.get(i).m_strUrl).into(ivImage);
            } else {
                ivImage.setImageResource(R.drawable.plus_image);
            }

            ivImage.setBackgroundResource(R.drawable.collage_image_back);
            ivImage.setPadding(0, 0, 0, 0);

            PhotoPosition pPhotoPos = pCollageConf.getPhotoPos(i);

            // TODO: normal padding!
            int collage_padding = 10;
            int collage_size_x = Utils.getScreenSizeInPixels(this).x - collage_padding * 2;
            collage_padding = Utils.pixelsToDPS(this, collage_padding);
            pPhotoPos.putCoefToPixels(collage_size_x);

            RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams(pPhotoPos.getSize(), pPhotoPos.getSize());
            params.leftMargin = pPhotoPos.getX() + collage_padding;   // TODO: why this is not pixels?
            params.topMargin = pPhotoPos.getY() + collage_padding;

            ivImage.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ImageView iv = (ImageView)v;
                    int index = iv.getId() - m_iCollageImageViewsID;

                    // Start so called main activity
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            });

            rlCollage.addView(ivImage, params);
        }

//        int image_count = m_lImages.size();
//        int collage_image_count = 0;
//        ArrayList<Integer> lCollageImageCount = new ArrayList<Integer>(Arrays.asList(2, 4, 6, 12, 20));
//        for (Integer good_image_count : lCollageImageCount) {
//            if (image_count >= good_image_count)
//                collage_image_count = good_image_count;
//        }
//
//        if (collage_image_count == 0) {
//            Log.v(TAG, "Too few images for collage: " + image_count);
//            m_tvSummary.setText("Too few images for collage: " + image_count);
//            return;
//        }
//
//        Log.v(TAG, "Pick " + collage_image_count + " images for collage.");
//
//        for (int i = 0; i < (image_count - collage_image_count); i++)
//            m_lImages.remove(m_lImages.size() - 1);
//
//        int index = 0;
//        for (ImageData image : m_lImages) {
//            new DownloadImageTask(index++).execute(image.m_strUrl);
//        }
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
            m_tvSummary.setText("Image number " + m_iImageIndex + " successfully loaded");
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
            m_tvSummary.setText("All " + images_loaded + " images are successfully loaded!");
            MakeCollage();
        }
    }

    private void MakeCollage() {
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
        m_btnShare.setEnabled(true);
    }

    private void shareCollage() {
        if (m_imgCollage == null) {
            Log.v(TAG, "Error: collage isn't ready yet");
        }
        // Save file on disk and open share dialog
        m_btnShare.setEnabled(false);
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
        m_btnShare.setEnabled(true);
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
}
