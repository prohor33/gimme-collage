package crystal.tech.gimmecollage.floating_action_btn;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
    private final Paint mButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mDrawablePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap mBitmap;
    private int mColor;

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

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FloatingActionButton);
        mColor = a.getColor(R.styleable.FloatingActionButton_fab_color, Color.WHITE);
        mButtonPaint.setStyle(Paint.Style.FILL);
        mButtonPaint.setColor(mColor);
        float radius, dx, dy;
        radius = a.getFloat(R.styleable.FloatingActionButton_shadowRadius, 10.0f);
        dx = a.getFloat(R.styleable.FloatingActionButton_shadowDx, 0.0f);
        dy = a.getFloat(R.styleable.FloatingActionButton_shadowDy, 3.5f);
        int color = a.getInteger(R.styleable.FloatingActionButton_shadowColor, Color.argb(100, 0, 0, 0));
        mButtonPaint.setShadowLayer(radius, dx, dy, color);

        Drawable drawable = a.getDrawable(R.styleable.FloatingActionButton_drawable);
        if (null != drawable) {
            mBitmap = ((BitmapDrawable) drawable).getBitmap();
        }
        setWillNotDraw(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if(Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                setBackgroundDrawable(null);
            } else {
                setBackground(null);
            }
        }


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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Outline
            ViewOutlineProvider viewOutlineProvider = new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, getWidth(), getHeight());
                }
            };
            setOutlineProvider(viewOutlineProvider);
            setClipToOutline(true);
        }
    }

    public static int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }


    public void setColor(int color) {
        mColor = color;
        mButtonPaint.setColor(mColor);
        invalidate();
    }

    public void setDrawable(Drawable drawable) {
        mBitmap = ((BitmapDrawable) drawable).getBitmap();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.onDraw(canvas);
            return;
        } else {
            if (mUnderTheParent)
                return;
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, (float) (getWidth() / 2.6), mButtonPaint);
            if (null != mBitmap) {
                canvas.drawBitmap(mBitmap, (getWidth() - mBitmap.getWidth()) / 2,
                        (getHeight() - mBitmap.getHeight()) / 2, mDrawablePaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isClickable())
            return false;

        int color;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            color = mColor;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final float clickElevation =
                        getResources().getDimension(R.dimen.fab_elevation);
                animate().translationZ(clickElevation).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        animate().translationZ(0);
                    }
                });
            }
        } else {
            color = darkenColor(mColor);
        }
        mButtonPaint.setColor(color);
        invalidate();

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