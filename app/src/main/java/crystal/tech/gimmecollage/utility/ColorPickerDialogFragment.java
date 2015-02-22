package crystal.tech.gimmecollage.utility;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import crystal.tech.gimmecollage.app.R;
import crystal.tech.gimmecollage.collagemaker.CollageMaker;

/**
 * Created by prohor on 17/02/15.
 */
public class ColorPickerDialogFragment extends DialogFragment {

    private Integer[] colors = {
            R.color.color_picker_dialog_item0,
            R.color.color_picker_dialog_item1,
            R.color.color_picker_dialog_item2,
            R.color.color_picker_dialog_item3,
            R.color.color_picker_dialog_item4,
            R.color.color_picker_dialog_item5,
            R.color.color_picker_dialog_item6,
            R.color.color_picker_dialog_item7,
            R.color.color_picker_dialog_item8,
            R.color.color_picker_dialog_item9,
            R.color.color_picker_dialog_item10,
            R.color.color_picker_dialog_item11,
            R.color.color_picker_dialog_item12,
            R.color.color_picker_dialog_item13,
            R.color.color_picker_dialog_item14,
            R.color.color_picker_dialog_item15,
            R.color.color_picker_dialog_item16,
            R.color.color_picker_dialog_item17,
            R.color.color_picker_dialog_item18,
            R.color.color_picker_dialog_item19,
            R.color.color_picker_dialog_item20,
            R.color.color_picker_dialog_item21
    };

    private int selectedColorIndex = 0;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setTitle(R.string.color_picker_dialog_title);

        View rootView = inflater.inflate(R.layout.color_picker_dialog, null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(rootView)
                // Add action buttons
                .setPositiveButton(R.string.color_picker_dialog_select, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // ok
                        CollageMaker.putBackgroundColor(colors[selectedColorIndex]);
                    }
                })
                .setNegativeButton(R.string.color_picker_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ColorPickerDialogFragment.this.getDialog().cancel();
                    }
                })
                .setIcon(R.drawable.ic_color_picker_dialog_icon);

        GridView gridview = (GridView) rootView.findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(getActivity()));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                GridView parent = (GridView) view.getParent();
                for (int j = 0; j < parent.getChildCount(); j++) {
                    View v = parent.getChildAt(j);
                    v.setBackgroundDrawable(null);
                }

                view.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.color_picker_dialog_item_selected_back));
                selectedColorIndex = i;
            }
        });

        int color_id = CollageMaker.getBackgroundColor();
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] == color_id)
                selectedColorIndex = i;
        }

        return builder.create();
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return colors.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                int size = getResources().getDimensionPixelSize(R.dimen.color_picker_dialog_item_size);
                imageView.setLayoutParams(new GridView.LayoutParams(size, size));
                int padding = getResources().getDimensionPixelSize(R.dimen.color_picker_dialog_item_space);
                imageView.setPadding(padding, padding, padding, padding);
            } else {
                imageView = (ImageView) convertView;
            }

            if (position == selectedColorIndex) {
                imageView.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.color_picker_dialog_item_selected_back));
            } else {
                imageView.setBackgroundDrawable(null);
            }

            Drawable drawable = getResources().getDrawable(R.drawable.color_picker_dialog_item);
            drawable.setColorFilter(getResources().getColor(colors[position]), PorterDuff.Mode.SRC_ATOP);

            imageView.setImageDrawable(drawable);
            return imageView;
        }
    }
}