package crystal.tech.gimmecollage.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import crystal.tech.gimmecollage.ads.Ads;
import crystal.tech.gimmecollage.analytics.LocalStatistics;
import crystal.tech.gimmecollage.analytics.GoogleAnalyticsUtils;
import crystal.tech.gimmecollage.app.view.DragDropView;
import crystal.tech.gimmecollage.floating_action_btn.FloatingActionButton;

import com.google.android.gms.analytics.GoogleAnalytics;
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


public class CollageActivity extends Fragment {

    private static final String TAG = "CollageActivity";
    private static final int INSTAGRAM_FRIEND_REQUEST = 1;
    private static final int GALLERY_REQUEST = 2;
    private ProgressDialog m_dialogProgress = null;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NewsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CollageActivity newInstance(String param1, String param2) {
        CollageActivity fragment = new CollageActivity();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public CollageActivity() {
        // Required empty public constructor
    }

    private final int m_iTemplateImageViewsID = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_collage, container, false);

        if (!InstagramAPI.initialized()) {
            InstagramAPI.init(getActivity(), ApplicationData.CLIENT_ID, ApplicationData.CLIENT_SECRET,
                    ApplicationData.CALLBACK_URL);
        }

        //ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);

        addCollageTypeSelectorLayout(rootView);

        addCollageLayout(rootView);
        reloadCollageImages();

        addFloatingActionButton(rootView);

        return rootView;
    }

    private int mInstagramSelctedFriendID = -1;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (Settings.showAds) {
            if (Ads.ShowInterstitial()) {
                GoogleAnalyticsUtils.SendEvent(getActivity(),
                        R.string.ga_event_category_see_interstitial_back_to_main_activity,
                        R.string.ga_event_action_see_interstitial_back_to_main_activity,
                        R.string.ga_event_label_see_interstitial_back_to_main_activity);
            }
        }

        switch (requestCode) {
            case INSTAGRAM_FRIEND_REQUEST:
                if (data != null) {
                    int friendID = data.getIntExtra("intSelectedFriendID", 0);
                    if (friendID != mInstagramSelctedFriendID)
                        reloadCollageImages();
                }
                break;
            case GALLERY_REQUEST:
                if(resultCode == Activity.RESULT_OK)
                    reloadCollageImages();
                break;
            default:
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_share:

                GoogleAnalyticsUtils.SendEvent(getActivity(),
                        R.string.ga_event_category_share_via_action_bar,
                        R.string.ga_event_action_share_via_action_bar,
                        R.string.ga_event_label_share_via_action_bar);

                shareCollage();
                break;
            case R.id.action_save:

                GoogleAnalyticsUtils.SendEvent(getActivity(),
                        R.string.ga_event_category_save_via_action_bar,
                        R.string.ga_event_action_save_via_action_bar,
                        R.string.ga_event_label_save_via_action_bar);

                saveCollageOnDisk();
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

    private void shareCollage() {
        if (m_dialogProgress == null)
            m_dialogProgress = new ProgressDialog(getActivity());
        m_dialogProgress.setTitle("Just a second");
        m_dialogProgress.setMessage("Generating collage for you...");
        m_dialogProgress.setCancelable(false);
        m_dialogProgress.show();

        Bitmap imgCollage = CollageMaker.getInstance().GenerateCollageImage();
        // Save file on disk and open share dialog
        new SaveFileTask().with(new FileSaveCallback() {
            @Override
            public void onSuccess(File file_result) {
                openShareDialog(file_result);
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

    private void openShareDialog(File fileResult) {
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

    private void addCollageTypeSelectorLayout(View rootView) {
        final LinearLayout llTemplates = (LinearLayout) rootView.findViewById(R.id.layoutTemplates);
        for (int i = 0; i < CollageMaker.CollageType.values().length; i++) {
            final int selector_size = Utils.getScrSizeInPxls(getActivity()).y / 8;
            CollageTypeSelectorImageView ivSelector =
                    new CollageTypeSelectorImageView(getActivity(), null, selector_size, i);
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
            currentPaint.setStyle(Paint.Style.STROKE);
            currentPaint.setStrokeJoin(Paint.Join.ROUND);
            currentPaint.setStrokeCap(Paint.Cap.ROUND);
            currentPaint.setStrokeWidth(Utils.dipToPixels(getActivity(), 2.0f));

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
            currentPaint.setColor(getResources().getColor(R.color.collage_type_selector_back));
            canvas.drawRect(0, 0, selectorSize, selectorSize, currentPaint);

            if (selectorIndex == CollageMaker.getInstance().getCollageTypeIndex()) {
                currentPaint.setColor(getResources().getColor(R.color.collage_type_selector_pushed));
            } else {
                currentPaint.setColor(getResources().getColor(R.color.collage_type_selector));
            }
            currentPaint.setStyle(Paint.Style.STROKE);
            for (Line l : lines) {
                canvas.drawLine(l.startX, l.startY, l.stopX, l.stopY, currentPaint);
            }
        }
    }

    private void addCollageLayout(View rootView) {
        int collage_size_x = Utils.getScrSizeInPxls(getActivity()).x - 45 * 2;
        CollageMaker collageMaker = CollageMaker.getInstance();
        collageMaker.putCollageSize(collage_size_x);
        Log.v(TAG, "init()");

        final DragDropView rlCollage = (DragDropView)rootView.findViewById(R.id.layoutCollage);
        CollageMaker.getInstance().putCollageLayout(rlCollage);
        for (int i = 0; i < CollageMaker.getInstance().getMaxImageCount(); i++) {
            RelativeLayout rl = (RelativeLayout)getActivity().getLayoutInflater().
                    inflate(R.layout.layout_collage_image, rlCollage, false);
            ImageView ivImage = (ImageView)rl.findViewById(R.id.imageView);
            final ProgressBar progressBar = (ProgressBar)rl.findViewById(R.id.progressBar);


            ivImage.setImageResource(R.drawable.ic_add_file_action);

            ivImage.setPadding(0, 0, 0, 0);
            ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

            ivImage.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ImageView iv = (ImageView)v;
//                    int index = iv.getId() - m_iCollageImageViewsID;

                    GoogleAnalyticsUtils.SendEvent(getActivity(),
                            R.string.ga_event_category_add_image,
                            R.string.ga_event_action_add_image,
                            R.string.ga_event_label_add_image);

                    showImageSourceDialog();
                }
            });
            ivImage.setOnTouchListener(rlCollage.OnTouchToDrag);
            ivImage.setOnLongClickListener(rlCollage.OnLongClick);
            rlCollage.addView(rl);
        }
        rlCollage.setBackgroundResource(R.drawable.collage_rl_back);

        CollageMaker.getInstance().InitImageViews(getActivity());
    }

    private void updateImageView(ImageView iv, final View pb, CollageMaker.ImageData img_data) {
        pb.setVisibility(View.VISIBLE);
        Picasso.with(getActivity());

        Callback on_load = new Callback() {
            @Override
            public void onSuccess() {
                if (pb != null)
                    pb.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                if (pb != null)
                    pb.setVisibility(View.GONE);
            }
        };

        switch (img_data.getSrc()) {
            case Instagram: {
                Picasso.with(getActivity())
                        .load(img_data.getUrl())
                        .into(iv, on_load);
                break;
            }
            case Gallery: {
                Picasso.with(getActivity())
                        .load(new File(img_data.getImagePath()))
                        .into(iv, on_load);
                break;
            }
        }
    }

    private void reloadCollageImages() {
        CollageMaker collageMaker = CollageMaker.getInstance();
        for (int i = 0; i < collageMaker.getImageCount(); i++) {
            RelativeLayout rl = collageMaker.getImageRL(i);
            final ImageView iv = (ImageView) rl.getChildAt(0);
            final View pb = rl.getChildAt(1);
            if (i < collageMaker.getImagesDataSize()) {
                updateImageView(iv, pb, collageMaker.getImageData(i));
            } else {
                iv.setImageResource(R.drawable.ic_add_file_action);
                pb.setVisibility(View.GONE);
            }
        }
    }

    private void addFloatingActionButton(final View rootView) {
        final FloatingActionButton ok_fab = (FloatingActionButton)rootView.findViewById(R.id.fabbutton);
        ok_fab.setColor(getResources().getColor(R.color.design_blue));
        ok_fab.setDrawable(getResources().getDrawable(R.drawable.ic_navigation_accept));
        ok_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FloatingActionButton mFab1 = (FloatingActionButton)rootView.findViewById(R.id.fabbutton0);
                mFab1.hide(!mFab1.getHidden());
                FloatingActionButton mFab2 = (FloatingActionButton)rootView.findViewById(R.id.fabbutton1);
                mFab2.hide(!mFab2.getHidden());
            }
        });

        FloatingActionButton save_fab = (FloatingActionButton)rootView.findViewById(R.id.fabbutton0);
        save_fab.setColor(getResources().getColor(R.color.design_yellow));    // maroon
        save_fab.setDrawable(getResources().getDrawable(R.drawable.ic_action_content_save));
        save_fab.setParentFAB(ok_fab);
        save_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleAnalyticsUtils.SendEvent(getActivity(),
                        R.string.ga_event_category_save_via_fab,
                        R.string.ga_event_action_save_via_fab,
                        R.string.ga_event_label_save_via_fab);

                saveCollageOnDisk();
            }
        });

        FloatingActionButton share_fab = (FloatingActionButton)rootView.findViewById(R.id.fabbutton1);
        share_fab.setColor(getResources().getColor(R.color.design_red));
        share_fab.setDrawable(getResources().getDrawable(R.drawable.ic_action_share));
        share_fab.setParentFAB(ok_fab);
        share_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                GoogleAnalyticsUtils.SendEvent(getActivity(),
                        R.string.ga_event_category_share_via_fab,
                        R.string.ga_event_action_share_via_fab,
                        R.string.ga_event_label_share_via_fab);

                shareCollage();
            }
        });
    }

    private void showImageSourceDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                getActivity());
        builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle("Select Image Source");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getActivity(),
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

                        boolean src_instagram = strName == strInstagram;

                        GoogleAnalyticsUtils.SendEventWithValue(getActivity(),
                                R.string.ga_event_category_select_img_src,
                                R.string.ga_event_action_select_img_src,
                                R.string.ga_event_label_select_img_src, src_instagram ? 0 : 1);

                        if (src_instagram) {
                            Intent intent = new Intent(getActivity(), FriendPicker.class);
                            startActivityForResult(intent, INSTAGRAM_FRIEND_REQUEST);
                        } else {
                            Intent intent = new Intent(getActivity(), GalleryPicker.class);
                            startActivityForResult(intent, GALLERY_REQUEST);
                        }
                    }
                });
        builderSingle.show();
    }

    private void saveCollageOnDisk() {
        if (m_dialogProgress == null)
            m_dialogProgress = new ProgressDialog(getActivity());
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
                Toast.makeText(getActivity(), "Collage saved to " + extStorageDirectory,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError() {

            }
        }).execute(imgCollage);
    }
}
