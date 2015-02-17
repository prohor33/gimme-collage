package crystal.tech.gimmecollage.utility;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by kisame on 11.02.15.
 */
public class ImageLoader {

    private final String TAG = "ImageLoader";
    private Context mContext;

    public ImageLoader(Context context) {
        mContext = context;
    }

    /**
     * Loads a thumbnail bitmap for an image in android system with specified Id.
     * @param origId
     * @param imageView
     */
    public void loadThumbnail(long origId, ImageView imageView) {
        loadThumbnail(origId, imageView, null);
    }

    public void loadThumbnail(long origId, ImageView imageView, Target target) {
        if (cancelPotentialDownload(origId, imageView)) {
            BitmapDownloaderTask task = new BitmapDownloaderTask(imageView, target);
            DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task, target);
            imageView.setImageDrawable(downloadedDrawable);
            imageView.setMinimumHeight(156);
            task.execute(origId);
        }
    }

    /**
     * Returns true if the current download has been canceled or if there was no download in
     * progress on this image view.
     * Returns false if the download in progress deals with the same url. The download is not
     * stopped in that case.
     */
    private static boolean cancelPotentialDownload(long origId, ImageView imageView) {
        BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            long bitmapId = bitmapDownloaderTask.origId;
            if (bitmapId != origId) {
                bitmapDownloaderTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }

    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active download task (if any) associated with this imageView.
     * null if there is no such task.
     */
    private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable)drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

    Bitmap downloadBitmap(long origId) {
        return MediaStore.Images.Thumbnails.getThumbnail(
                mContext.getContentResolver(),
                origId,
                MediaStore.Images.Thumbnails.MINI_KIND,
                null);
    }

    /**
     * The actual AsyncTask that will asynchronously download the image.
     */
    class BitmapDownloaderTask extends AsyncTask<Long, Void, Bitmap> {
        private long origId;
        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<Target> targetReference;
        private boolean targetOriented;
        private String thumbnailPath;

        public BitmapDownloaderTask(ImageView imageView, Target target) {
            imageViewReference = new WeakReference<ImageView>(imageView);
            targetReference = new WeakReference<Target>(target);
            targetOriented = target != null;
            thumbnailPath = null;
        }

        /**
         * Actual download method.
         */
        @Override
        protected Bitmap doInBackground(Long... params) {
            origId = params[0];
            thumbnailPath = getThumbnailPath(origId);
            if(thumbnailPath != null) {
                return null;
            }
            return downloadBitmap(origId);
        }

        /**
         * Once the image is downloaded, associates it to the imageView
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            ImageView imageView = imageViewReference.get();
            if(imageView != null) {
                BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                if (this == bitmapDownloaderTask) {
                    if(targetOriented) {
                        // have target (used for collage and pull)
                        Target target = targetReference.get();
                        if(thumbnailPath == null) {
                            target.onBitmapLoaded(bitmap, null);
                        } else {
                            Picasso.with(mContext).load(new File(thumbnailPath)).into(target);
                        }
                    } else {
                        if(thumbnailPath == null) {
                            imageView.setImageBitmap(bitmap);
                        } else {
                            Picasso.with(mContext).load(new File(thumbnailPath)).into(imageView);
                        }
                    }
                }
            }
        }
    }

    /**
     * Method for getting image thumbnail uri.
     */
    String getThumbnailPath(long imageId) {
        String path = null;
        Cursor cursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(
                mContext.getContentResolver(),
                imageId,
                MediaStore.Images.Thumbnails.MINI_KIND,
                new String[]{MediaStore.Images.Thumbnails.DATA});
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                // There are thumbnail in system.
                cursor.moveToFirst();
                int column_index = cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA);
                path = cursor.getString(column_index);
                if (!(new File(path).exists())) {
                    Log.e(TAG, "Path doesn't exist: " + path);
                    path = null;
                }
            }
            cursor.close();
        }
        return path;
    }


    /**
     * A fake Drawable that will be attached to the imageView while the download is in progress.
     *
     * <p>Contains a reference to the actual download task, so that a download task can be stopped
     * if a new binding is required, and makes sure that only the last started download process can
     * bind its result, independently of the download finish order.</p>
     */
    static class DownloadedDrawable extends ColorDrawable {
        private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

        public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask, Target target) {
            super(Color.TRANSPARENT);
            bitmapDownloaderTaskReference =
                    new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
        }

        public BitmapDownloaderTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }
    }

}
