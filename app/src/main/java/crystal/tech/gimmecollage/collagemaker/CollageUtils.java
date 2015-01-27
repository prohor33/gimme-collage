package crystal.tech.gimmecollage.collagemaker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

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
            currentPaint.setStrokeWidth(
                    parentActivity.getResources().getDimensionPixelSize(R.dimen.stroke_width));

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
    private final int viewIdsStart = 1000;
    private void addCollageTypeSelectorLayoutImpl(final View rootView) {
        final LinearLayout llTemplates = (LinearLayout) rootView.findViewById(R.id.layoutTemplates);
        for (int i = 0; i < CollageMaker.CollageType.values().length; i++) {
            final int selector_size =
                    parentActivity.getResources().getDimensionPixelSize(R.dimen.selector_size);
            CollageTypeSelectorImageView ivSelector =
                    new CollageTypeSelectorImageView(parentActivity, null, selector_size, i);
            ivSelector.setId(viewIdsStart + i);
            CollageMaker.getInstance().DrawCollageTypeSelector(ivSelector, i, selector_size);

            ivSelector.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ImageView iv = (ImageView) v;
                    int index = iv.getId() - viewIdsStart;
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
            ImageView iv2 = (ImageView) llTemplates.getChildAt(i);
            iv2.setColorFilter(0);
        }
    }
}
