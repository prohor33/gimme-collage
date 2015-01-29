package crystal.tech.gimmecollage.floating_action_btn;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.Button;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

import crystal.tech.gimmecollage.app.R;
import crystal.tech.gimmecollage.collagemaker.CollageUtils;

public class FloatingActionButton extends Button {

    private static final String TAG = "FloatingActionButton";
    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    private boolean mHidden = false;
    private boolean mUnderTheParent = false;
    /**
     * The FAB button's X position when it is displayed.
     */
    private Point mPosDisplayed = new Point(-1, -1);
    /**
     * The FAB button's X position when it is hidden.
     */
    private Point mPosHidden = new Point(-1, -1);
    private FloatingActionButton mParentFAB = null;

    public FloatingActionButton(Context context) {
        this(context, null);
    }

    public FloatingActionButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }


    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        ViewTreeObserver vto = getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                ViewTreeObserver obs = getViewTreeObserver();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }

                // Store the FAB button's displayed X position if we are not already aware of it
                if (mPosDisplayed.x == -1) {
                    mPosDisplayed.x = (int)getX();
                    mPosDisplayed.y = (int)getY();
                }

                if (mParentFAB != null) {
                    mHidden = CollageUtils.getFabCollapsed();
                    setUnderParent(mHidden);

                    mPosHidden.x = mParentFAB.getLeft();
                    mPosHidden.y = mParentFAB.getTop();

                    if (mHidden) {
                        setX(mPosHidden.x);
                        setY(mPosHidden.y);
                    }
                }
            }


        });

        WindowManager mWindowManager = (WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE);
        Display display = mWindowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mPosHidden.x = size.x;
        mPosHidden.y = size.y;

        // Outline
        ViewOutlineProvider viewOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                // Or read size directly from the view's width/height
                final int size =
                        getResources().getDimensionPixelSize(R.dimen.fab_size);
                outline.setOval(0, 0, size, size);
            }
        };
        setOutlineProvider(viewOutlineProvider);

        setClipToOutline(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isClickable())
            return false;

        if (event.getAction() == MotionEvent.ACTION_UP) {

            final float clickElevation =
                    getResources().getDimension(R.dimen.fab_elevation);
            animate().translationZ(clickElevation).withEndAction(new Runnable() {
                @Override
                public void run() {
                    animate().translationZ(0);
                }
            });
        }
        return super.onTouchEvent(event);
    }

    public void hide(boolean hide) {

        // If the hidden state is being updated
        if (mHidden != hide) {

            // Store the new hidden state
            mHidden = hide;
            CollageUtils.putFabCollapsed(mHidden);

            // Animate the FAB to it's new X position
            ObjectAnimator animatorX = ObjectAnimator.ofFloat(this,
                    "x", mHidden ? mPosHidden.x : mPosDisplayed.x).setDuration(500);
            ObjectAnimator animatorY = ObjectAnimator.ofFloat(this,
                    "y", mHidden ? mPosHidden.y : mPosDisplayed.y).setDuration(500);
            animatorX.setInterpolator(mInterpolator);

            animatorX.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (!mHidden) {
                        setUnderParent(false);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mHidden) {
                        setUnderParent(true);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {}
                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
            animatorX.start();
            animatorY.start();
        }
    }

    public void listenTo(AbsListView listView) {
        if (null != listView) {
            listView.setOnScrollListener(new DirectionScrollListener(this));
        }
    }

    public void setParentFAB(FloatingActionButton parent_fab) {
        mParentFAB = parent_fab;
    }

    public boolean getHidden() {
        return mHidden;
    }

    private void setUnderParent(boolean under_parent) {
        mUnderTheParent = under_parent;
        setClickable(!under_parent);
        invalidate();
    }
}