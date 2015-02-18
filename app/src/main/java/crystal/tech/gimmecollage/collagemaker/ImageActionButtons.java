package crystal.tech.gimmecollage.collagemaker;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import crystal.tech.gimmecollage.app.R;
import crystal.tech.gimmecollage.app.view.GestureRelativeLayout;
import crystal.tech.gimmecollage.floating_action_btn.FloatingActionButton;

/**
 * Created by prohor on 07/02/15.
 */
public class ImageActionButtons {

    public final String TAG = "ImageActionButtons";

    private Activity collageActivity = null;
    private View rootView = null;
    private GestureRelativeLayout rlCollage = null;
    private RelativeLayout floatingBtnsRL = null;
    private FloatingActionButton buttonTR = null;
    private FloatingActionButton buttonTL = null;
    private FloatingActionButton buttonBR = null;
    private FloatingActionButton buttonBL = null;
    private FrameLayout selectedFL = null;
    private boolean isVisible = false;
    private boolean disabled = false;

    public void init(Activity collage_activity, View root_view) {
        collageActivity = collage_activity;
        rootView = root_view;
        rlCollage = (GestureRelativeLayout) rootView.findViewById(R.id.rlCollage);
        floatingBtnsRL = (RelativeLayout) rootView.findViewById(R.id.image_action_buttons_rl);

        final float defaultElevation =
                collageActivity.getResources().getDimension(R.dimen.image_action_def_elevation);
        buttonTR = (FloatingActionButton) floatingBtnsRL.findViewById(R.id.image_action_rotate_right_btn);
        buttonTL = (FloatingActionButton) floatingBtnsRL.findViewById(R.id.image_action_rotate_left_btn);
        buttonBR = (FloatingActionButton) floatingBtnsRL.findViewById(R.id.image_action_accept_btn);
        buttonBL = (FloatingActionButton) floatingBtnsRL.findViewById(R.id.image_action_settings_btn);

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

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            buttonTR.setColor(collageActivity.getResources().getColor(R.color.image_action_btn_rotate_left_color));
            buttonTR.setDrawable(collageActivity.getResources().getDrawable(R.drawable.ic_action_btn_rotate_right));
            buttonTL.setColor(collageActivity.getResources().getColor(R.color.image_action_btn_rotate_left_color));
            buttonTL.setDrawable(collageActivity.getResources().getDrawable(R.drawable.ic_action_button_rotate_left));
            buttonBR.setColor(collageActivity.getResources().getColor(R.color.image_action_btn_accept_color));
            buttonBR.setDrawable(collageActivity.getResources().getDrawable(R.drawable.ic_action_btn_ok));
            buttonBL.setColor(collageActivity.getResources().getColor(R.color.image_action_btn_settings_color));
            buttonBL.setDrawable(collageActivity.getResources().getDrawable(R.drawable.ic_action_btn_palette));
        }
    }

//    pass FL view here
    public void showOnView(View view) {
        if (disabled)
            return;

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

    public void putDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean getDisabled() {
        return disabled;
    }

    public void onRotateLeft(FrameLayout frameLayout) {
        ImageView iv = (ImageView) frameLayout.findViewById(R.id.ivMain);
        int collageIndex = CollageMaker.getIndexByFLView(frameLayout);
        CollageUtils.rotateImage(iv, ImageStorage.getCollageImage(collageIndex), -90.0f);
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

    private void onSettings(View v) {
        CollageUtils.showColorPickerDialog();
    }

    private void onAccept(View v) {
        CollageMaker.deselectAllViews();
    }
}
