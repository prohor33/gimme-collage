package crystal.games.gimmecollage.collagemaker;

import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.HashMap;
import java.util.Map;

import crystal.games.gimmecollage.app.MainActivity;
import crystal.games.gimmecollage.app.Utils;

/**
 * Created by prohor on 04/10/14.
 */
public class CollageMaker {

    private static final String TAG = "CollageMaker";

    public static synchronized CollageMaker getInstance() {
        if (m_pInstance == null) {
            m_pInstance = new CollageMaker();
        }
        return m_pInstance;
    }

    public CollageConfig getCollageConf() {
        return getCollageConf(m_eType);
    }
    public CollageConfig getCollageConf(CollageType eType) {
        if (!m_mCollages.containsKey(eType)) {
            Log.v(TAG, "Error: no such a collage: " + eType);
            return null;
        }
        return m_mCollages.get(eType);
    }

    public void putCollageType(CollageType type) {
        m_eType = type;
    }

    public void MoveToTheOtherCollageType(int type_index) {
        if (type_index < 0 || type_index >= CollageType.values().length) {
            Log.v(TAG, "Error: wrong collage type index = " + type_index);
            return;
        }
        moveToTheOtherCollageType(CollageMaker.CollageType.values()[type_index]);
    }

    public void moveToTheOtherCollageType(CollageType type) {
        putCollageType(type);
        UpdateCollageRestructuring();
    }

    public void setCollageLayout(RelativeLayout collage_layout) {
        m_rlCollage = collage_layout;
    }

    public void putCollageSize(int collage_size) {
        this.m_iCollageSize = collage_size;
    }
    public void putCollagePadding(int collage_padding) {
        this.m_iCollagePadding = collage_padding;
    }

    public int getMaxImageCount() {
        int max_count = -1;
        for (CollageType type : CollageType.values()) {
            int count = getCollageConf(type).getPhotoCount();
            max_count = count > max_count ? count : max_count;
        }
        return max_count;
    }

    public void UpdateCollageRestructuring() {
        if (m_rlCollage == null)
            return;
        if (getCollageConf().getPhotoCount() > m_rlCollage.getChildCount()) {
            Log.v(TAG, "Error: too few image views");
            return;
        }

        // TODO: should we do this each frame?
        PrepareImages();
        RunAnimImageViews();
    }

    public void InitImageViews() {
        for (int i = 0; i < m_rlCollage.getChildCount() &&
                i < getCollageConf().getPhotoCount(); i++) {
            final ImageView iv = (ImageView) m_rlCollage.getChildAt(i);

            PhotoPosition pPhotoPos = getCollageConf().getPhotoPos(i);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) iv.getLayoutParams();
            params.height = (int) (m_iCollageSize * pPhotoPos.getSize());
            params.width = (int) (m_iCollageSize * pPhotoPos.getSize());
            params.leftMargin = (int) (m_iCollageSize * pPhotoPos.getX()) + m_iCollagePadding;
            params.topMargin = (int) (m_iCollageSize * pPhotoPos.getY()) + m_iCollagePadding;
            iv.setLayoutParams(params);
        }
    }

    public void DrawCollageTypeSelector(MainActivity.CollageTypeSelectorImageView ivSelector,
                                        int index, int size) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
        ivSelector.setLayoutParams(layoutParams);
        if (index < 0 || index >= m_mCollages.size())
            return;
        final int selector_padding = size / 20;
        size -= selector_padding * 2;
        CollageConfig config = getCollageConf(CollageMaker.CollageType.values()[index]);
        for (int i = 0; i < config.getPhotoCount(); i++) {
            PhotoPosition photo_pos = config.getPhotoPos(i);
            int s_x = (int)(size * photo_pos.getX()) + selector_padding;
            int s_y = (int)(size * photo_pos.getY()) + selector_padding;
            int e_x = s_x + (int)(size * photo_pos.getSize());
            int e_y = s_y + (int)(size * photo_pos.getSize());
            ivSelector.AddLine(new MainActivity.Line(s_x, s_y, e_x, s_y));
            ivSelector.AddLine(new MainActivity.Line(e_x, s_y, e_x, e_y));
            ivSelector.AddLine(new MainActivity.Line(e_x, e_y, s_x, e_y));
            ivSelector.AddLine(new MainActivity.Line(s_x, e_y, s_x, s_y));
        }
    }

    public enum CollageType {
        Grid,
        CenterWithGridAround
    };


    private CollageType m_eType = CollageType.Grid;
    private Map<CollageType, CollageConfig> m_mCollages;
    private RelativeLayout m_rlCollage = null;
    private int m_iCollageSize;
    private int m_iCollagePadding = 10;

    private static CollageMaker m_pInstance;
    private CollageMaker() {
        m_eType = CollageType.Grid; // by default
        m_mCollages = new HashMap<CollageType, CollageConfig>();
        for (CollageType type : CollageType.values()) {
            m_mCollages.put(type, new CollageConfig(type));
        }

        this.m_iCollageSize = 100;  // in pixels
    }



    private void PrepareImages() {
        for (int i = 0; i < m_rlCollage.getChildCount(); i++) {
            ImageView iv = (ImageView)m_rlCollage.getChildAt(i);

            if (i < getCollageConf().getPhotoCount()) {
                iv.setVisibility(ImageView.VISIBLE);
            } else {
                iv.setVisibility(ImageView.GONE);
            }
        }
    }

    private void RunAnimImageViews() {
        for (int i = 0; i < m_rlCollage.getChildCount() &&
                i < getCollageConf().getPhotoCount(); i++) {
            final ImageView iv = (ImageView)m_rlCollage.getChildAt(i);

            PhotoPosition pPhotoPos = getCollageConf().getPhotoPos(i);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)iv.getLayoutParams();
            final int new_h = (int) (m_iCollageSize * pPhotoPos.getSize());
            final int new_w = (int) (m_iCollageSize * pPhotoPos.getSize());
            final int new_left = (int) (m_iCollageSize * pPhotoPos.getX()) + m_iCollagePadding;
            final int new_top = (int) (m_iCollageSize * pPhotoPos.getY()) + m_iCollagePadding;
//            Log.v(TAG, "RunAnimImageViews()");
            float coef_w = (float)new_w / params.width;
            float coef_h = (float)new_h / params.height;
            int delta_left = new_left - params.leftMargin;
            int delta_top = new_top - params.topMargin;
            final int anim_duration = 500;
            TranslateAnimation translateAnimation = new TranslateAnimation(
                    Animation.ABSOLUTE, 0.0f,
                    Animation.ABSOLUTE, delta_left,
                    Animation.ABSOLUTE, 0.0f,
                    Animation.ABSOLUTE, delta_top);
            translateAnimation.setDuration(anim_duration);
            translateAnimation.setRepeatCount(0);

            Animation scaleAnimation = new ScaleAnimation(
                    1.0f, coef_w,
                    1.0f, coef_h,
                    Animation.RELATIVE_TO_SELF, (float)delta_left / params.width,
                    Animation.RELATIVE_TO_SELF, (float)delta_top / params.height);
            scaleAnimation.setDuration(anim_duration);
            scaleAnimation.setRepeatCount(0);

            AnimationSet animSet = new AnimationSet(true);
            animSet.setFillEnabled(true);
            animSet.setFillAfter(true);

            animSet.setAnimationListener(new TranslateAnimation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) { }

                @Override
                public void onAnimationRepeat(Animation animation) { }

                @Override
                public void onAnimationEnd(Animation animation)
                {
                    iv.clearAnimation();
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)iv.getLayoutParams();
                    params.width = new_w;
                    params.height = new_h;
                    params.topMargin = new_top;
                    params.leftMargin = new_left;
                    iv.setLayoutParams(params);
                }
            });

            animSet.addAnimation(translateAnimation);
            animSet.addAnimation(scaleAnimation);
            iv.startAnimation(animSet);
        }
    }


}

