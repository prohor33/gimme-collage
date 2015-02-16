package crystal.tech.gimmecollage.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import crystal.tech.gimmecollage.app.R;
import crystal.tech.gimmecollage.collagemaker.CollageMaker;

/**
 * Created by prohor on 28/01/15.
 */

public class CollageTypeSelectorImageView extends ImageView {
    private Paint currentPaint;
    private ArrayList<Line> lines = new ArrayList<Line>();
    private int selectorIndex = -1;

    static public class Line {
        float startX, startY, stopX, stopY;

        public Line(PointF s, PointF e) {
            this(s.x, s.y, e.x, e.y);
        }

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

    public CollageTypeSelectorImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        currentPaint = new Paint();
        currentPaint.setDither(true);
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);
        currentPaint.setStrokeWidth(
                getResources().getDimensionPixelSize(R.dimen.stroke_width));
    }

    public void putIndex(int index) {
        selectorIndex = index;
    }
    public int getIndex() {
        return selectorIndex;
    }

    public void AddLine(Line line) {
        lines.add(line);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

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