package crystal.tech.gimmecollage.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.ArrayList;

import crystal.tech.gimmecollage.app.R;
import crystal.tech.gimmecollage.collagemaker.CollageMaker;

/**
 * Created by prohor on 28/01/15.
 */

public class CollageTypeSelectorImageView extends ImageView {
    private Paint currentPaint;
    private ArrayList<RotatedRect> rects = new ArrayList<>();
    private int selectorIndex = -1;
    private int padding;

    // colors
    private int fillColor;
    private int fillColorSelected;
    private int strokeColor;
    private int strokeColorSelected;

    public static class RotatedRect {
        public RotatedRect(Rect r, float angle) {
            rect = r;
            this.angle = angle;
        }
        public Rect rect;
        public float angle;
    }

    public CollageTypeSelectorImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        currentPaint = new Paint();
        currentPaint.setDither(true);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);
        currentPaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.stroke_width));

        fillColor = getResources().getColor(R.color.collage_type_selector_fill);
        fillColorSelected = getResources().getColor(R.color.collage_type_selector_fill_pushed);
        strokeColor = getResources().getColor(R.color.collage_type_selector_stroke);
        strokeColorSelected = getResources().getColor(R.color.collage_type_selector_stroke_pushed);
    }

    public void putIndex(int index) {
        selectorIndex = index;
    }
    public int getIndex() {
        return selectorIndex;
    }

    public void AddRect(RotatedRect rect) {
        rects.add(rect);
    }

    public void putPadding(int padding) {
        this.padding = padding;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // translate
        canvas.translate(padding, padding);
        for (RotatedRect rotatedRect : rects) {
            // rotate
            canvas.rotate(rotatedRect.angle, rotatedRect.rect.left, rotatedRect.rect.top);

            // fill
            if (selectorIndex == CollageMaker.getInstance().getCollageTypeIndex()) {
                currentPaint.setColor(fillColorSelected);
            } else {
                currentPaint.setColor(fillColor);
            }
            currentPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(rotatedRect.rect, currentPaint);

            // stroke
            if (selectorIndex == CollageMaker.getInstance().getCollageTypeIndex()) {
                currentPaint.setColor(strokeColorSelected);
            } else {
                currentPaint.setColor(strokeColor);
            }
            currentPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(rotatedRect.rect, currentPaint);

            // rotate back
            canvas.rotate(-rotatedRect.angle, rotatedRect.rect.left, rotatedRect.rect.top);
        }
        // translate back
        canvas.translate(-padding, -padding);
    }
}