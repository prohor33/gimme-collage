package crystal.games.gimmecollage.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ImageProcessor extends Activity {

    private static final String TAG = "ImageProcessor";

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

    private List<ImageData> lImages = new ArrayList<ImageData>();
    private TextView tvSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_processor);

        Bundle bundle = this.getIntent().getExtras();
        String[] images_array = bundle.getStringArray("images_array");
        int[] image_like_count_array = bundle.getIntArray("image_like_count_array");

        tvSummary = (TextView) findViewById(R.id.textView);
        tvSummary.setText("Image urls to load: " + images_array.length);


        if (images_array.length != image_like_count_array.length) {
            Log.v(TAG, "Error: different array length");
            return;
        }

        lImages.clear();
        for (int i = 0; i < images_array.length; i++) {
            lImages.add(new ImageData(images_array[i], image_like_count_array[i]));
        }

        class ImageComparator implements Comparator<ImageData> {
            @Override
            public int compare(ImageData o1, ImageData o2) {
                return o1.m_iLikeCount - o2.m_iLikeCount;
            }
        }

        // Let's sort images by likes count
        Collections.sort(lImages, new ImageComparator());

        int image_count = lImages.size();
        int collage_image_count = 0;
        ArrayList<Integer> lCollageImageCount = new ArrayList<Integer>(Arrays.asList(2, 4, 6, 12, 20));
        for (Integer good_image_count : lCollageImageCount) {
            if (image_count >= good_image_count)
                collage_image_count = good_image_count;
        }

        if (collage_image_count == 0) {
            Log.v(TAG, "Too few images for collage: " + image_count);
            tvSummary.setText("Too few images for collage: " + image_count);
            return;
        }

        Log.v(TAG, "Pick " + collage_image_count + " images for collage.");

        for (int i = 0; i < (image_count - collage_image_count); i++)
            lImages.remove(lImages.size() - 1);

        int index = 0;
        for (ImageData image : lImages) {
            new DownloadImageTask(index++).execute(image.m_strUrl);
        }
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
            tvSummary.setText("Image number " + m_iImageIndex + " successfully loaded");
            onImageLoad(m_iImageIndex, result);
        }
    }

    private void onImageLoad(int index, Bitmap image) {
        lImages.get(index).m_bmpImage = image;

        // Check if all images are loaded
        int images_loaded = 0;
        for (ImageData img : lImages) {
            images_loaded += img.m_bmpImage != null ? 1 : 0;
        }
        if (images_loaded == lImages.size()) {
            Log.v(TAG, "All images are successfully loaded!");
            tvSummary.setText("All " + images_loaded + " images are successfully loaded!");
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
        int collage_images_count = lImages.size();
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
                comboCanvas.drawBitmap(lImages.get(x + y * image_count_x).m_bmpImage,
                        img_size_x * x, img_size_y * y, null);
            }
        }

        Log.v(TAG, "Collage is successfully generated!");

        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setImageBitmap(collageImage);
    }
}
