package crystal.tech.gimmecollage.collagemaker;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import crystal.tech.gimmecollage.app.R;
import crystal.tech.gimmecollage.floating_action_btn.FloatingActionButton;

/**
 * Created by prohor on 26/01/15.
 */
public class CollageUtils {

    private final String TAG = "CollageUtils";

    private static CollageUtils instance;
    private boolean fabCollapsed = true;
    private Activity parentActivity = null;
    private View rootView = null;

    public static synchronized CollageUtils getInstance() {
        if (instance == null) {
            instance = new CollageUtils();
        }
        return instance;
    }

    public static void Init(Activity activity, View root_view) {
        getInstance().parentActivity = activity;
        getInstance().rootView = root_view;
    }

    public void saveCollageOnDisk() {
        Log.w(TAG, "saveCollageOnDisk() not implemented yet");
    }

    public void shareCollage() {
        Log.w(TAG, "shareCollage() not implemented yet");
    }

    public static void putFabCollapsed(boolean x) {
        getInstance().fabCollapsed = x;
    }
    public static boolean getFabCollapsed() {
        return  getInstance().fabCollapsed;
    }

    public static void addFloatingActionButtons(View rootView) {
        getInstance().addFloatingActionButtonsImpl(rootView);
    }
    private void addFloatingActionButtonsImpl(final View rootView) {
        final FloatingActionButton ok_fab = (FloatingActionButton)rootView.findViewById(R.id.fabbutton0);
        ok_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FloatingActionButton mFab1 = (FloatingActionButton)rootView.findViewById(R.id.fabbutton1);
                mFab1.hide(!mFab1.getHidden());
                FloatingActionButton mFab2 = (FloatingActionButton)rootView.findViewById(R.id.fabbutton2);
                mFab2.hide(!mFab2.getHidden());
            }
        });

        FloatingActionButton save_fab = (FloatingActionButton)rootView.findViewById(R.id.fabbutton1);
        save_fab.setParentFAB(ok_fab);
        save_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CollageMaker.saveCollageOnDisk();
            }
        });

        FloatingActionButton share_fab = (FloatingActionButton)rootView.findViewById(R.id.fabbutton2);
        share_fab.setParentFAB(ok_fab);
        share_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CollageMaker.shareCollage();
            }
        });
    }

    public static void addCollageTypeSelectorLayout(View rootView) {
        getInstance().addCollageTypeSelectorLayoutImpl(rootView);
    }
    private void addCollageTypeSelectorLayoutImpl(final View rootView) {
        final LinearLayout llTemplates = (LinearLayout) rootView.findViewById(R.id.layoutTemplates);
        for (int i = 0; i < CollageMaker.CollageType.values().length; i++) {
            final int selector_size =
                    parentActivity.getResources().getDimensionPixelSize(R.dimen.selector_size);

            CollageTypeSelectorImageView ivSelector =
                    (CollageTypeSelectorImageView)parentActivity.getLayoutInflater().inflate(
                            R.layout.layout_collage_type_selector, llTemplates, false);
            ivSelector.putIndex(i);
            CollageMaker.getInstance().DrawCollageTypeSelector(ivSelector, i, selector_size);

            ivSelector.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CollageTypeSelectorImageView iv = (CollageTypeSelectorImageView) v;
                    int index = iv.getIndex();
                    CollageMaker.getInstance().changeCollageType(index);
                }
            });

            llTemplates.addView(ivSelector);
        }
    }

    public void updateCollageTypeSelectors(){
        final LinearLayout llTemplates = (LinearLayout) rootView.findViewById(R.id.layoutTemplates);
        // reset all the others
        for (int i = 0; i < llTemplates.getChildCount(); i++) {
            ImageView iv = (ImageView)llTemplates.getChildAt(i);
            iv.setColorFilter(0);
            if (i == CollageMaker.getInstance().getCollageTypeIndex()) {
                final float clickElevation =
                        parentActivity.getResources().getDimension(R.dimen.selector_elevation);
                iv.animate().translationZ(clickElevation);
                continue;
            }
            iv.setTranslationZ(0);
        }
    }
}
