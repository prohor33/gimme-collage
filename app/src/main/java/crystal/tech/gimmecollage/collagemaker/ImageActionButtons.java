package crystal.tech.gimmecollage.collagemaker;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import crystal.tech.gimmecollage.app.R;
import crystal.tech.gimmecollage.app.view.GestureRelativeLayout;

/**
 * Created by prohor on 07/02/15.
 */
public class ImageActionButtons {

    public final String TAG = "ImageActionButtons";

    private Activity collageActivity = null;
    private View rootView = null;
    private GestureRelativeLayout rlCollage = null;
    private RelativeLayout floatingBtnsRL = null;
    private View buttonTR = null;
    private View buttonTL = null;
    private View buttonBR = null;
    private View buttonBL = null;
    private FrameLayout selectedFL = null;
    private boolean isVisible = false;

    public void init(Activity collage_activity, View root_view) {
        collageActivity = collage_activity;
        rootView = root_view;
        rlCollage = (GestureRelativeLayout) rootView.findViewById(R.id.rlCollage);
        floatingBtnsRL = (RelativeLayout) rootView.findViewById(R.id.image_action_buttons_rl);

        final float defaultElevation =
                collageActivity.getResources().getDimension(R.dimen.image_action_def_elevation);
        buttonTR = floatingBtnsRL.findViewById(R.id.image_action_rotate_right_btn);
        buttonTL = floatingBtnsRL.findViewById(R.id.image_action_rotate_left_btn);
        buttonBR = floatingBtnsRL.findViewById(R.id.image_action_accept_btn);
        buttonBL = floatingBtnsRL.findViewById(R.id.image_action_settings_btn);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            buttonTR.setElevation(defaultElevation);
            buttonTL.setElevation(defaultElevation);
            buttonBR.setElevation(defaultElevation);
            buttonBL.setElevation(defaultElevation);
        }

        buttonTR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRotateRight(selectedFL);
            }
        });
        buttonTL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRotateLeft(selectedFL);
            }
        });
        buttonBR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAccept(view);
            }
        });
        buttonBL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSettings(view);
            }
        });
    }

//    pass FL view here
    public void showOnView(View view) {
        selectedFL = (FrameLayout) view;
        isVisible = true;

        FrameLayout collageParent = (FrameLayout) rlCollage.getParent();

        final int fl_padding =
                collageActivity.getResources().getDimensionPixelSize(R.dimen.image_action_rl_padding);
        final int buttons_size =
                collageActivity.getResources().getDimensionPixelSize(R.dimen.image_action_btn_size);
        final int slash_outside = buttons_size / 2 + fl_padding;

        RelativeLayout.LayoutParams parentLParams =
                (RelativeLayout.LayoutParams) floatingBtnsRL.getLayoutParams();
        parentLParams.leftMargin = view.getLeft() + rlCollage.getLeft() + collageParent.getLeft() - slash_outside;
        parentLParams.topMargin = view.getTop() + rlCollage.getTop() + collageParent.getTop() - slash_outside;
        parentLParams.width = view.getWidth() + slash_outside * 2;
        parentLParams.height = view.getHeight() + slash_outside * 2;
        floatingBtnsRL.setLayoutParams(parentLParams);
        floatingBtnsRL.setVisibility(View.VISIBLE);

        RelativeLayout.LayoutParams btnTRParams =
                (RelativeLayout.LayoutParams) buttonTR.getLayoutParams();
        btnTRParams.leftMargin =  parentLParams.width - 2 * fl_padding - buttons_size;
        btnTRParams.topMargin = 0;
        buttonTR.setLayoutParams(btnTRParams);

        RelativeLayout.LayoutParams btnBRParams =
                (RelativeLayout.LayoutParams) buttonBR.getLayoutParams();
        btnBRParams.leftMargin = parentLParams.width - 2 * fl_padding - buttons_size;
        btnBRParams.topMargin = parentLParams.height - 2 * fl_padding - buttons_size;
        buttonBR.setLayoutParams(btnBRParams);

        animateAppearance(buttonTR);
        animateAppearance(buttonTL);
        animateAppearance(buttonBR);
        animateAppearance(buttonBL);
    }

    public void hide() {
        isVisible = false;
        floatingBtnsRL.setVisibility(View.GONE);
    }

    public boolean isVisible() {
        return isVisible;
    }

    // private members only ========================

    private void animateAppearance(View button) {
        button.setScaleX(0.1f);
        button.setScaleY(0.1f);

        button.animate().scaleX(1.0f).start();
        button.animate().scaleY(1.0f).start();
    }

    private void onRotateRight(FrameLayout frameLayout) {
        ImageView iv = (ImageView) frameLayout.findViewById(R.id.ivMain);
        int collageIndex = CollageMaker.getIndexByFLView(frameLayout);
        CollageUtils.rotateImage(iv, ImageStorage.getCollageImage(collageIndex), 90.0f);
    }

    private void onRotateLeft(FrameLayout frameLayout) {
        ImageView iv = (ImageView) frameLayout.findViewById(R.id.ivMain);
        int collageIndex = CollageMaker.getIndexByFLView(frameLayout);
        CollageUtils.rotateImage(iv, ImageStorage.getCollageImage(collageIndex), -90.0f);
    }

    private void onSettings(View v) {
        Log.w(TAG, "onSettings() not implemented yet");
    }

    private void onAccept(View v) {
        CollageMaker.deselectAllViews();
    }
}
