package crystal.games.gimmecollage.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;

/**
 * Created by prohor on 08/10/14.
 */
public class Utils {
    public static int pixelsToDPS(Context context, float pixels) {
        final float density = context.getResources().getDisplayMetrics().density;
        return (int) (pixels * density + 0.5f);
    }
    public static int DPSToPixels(Context context, float dps) {
        final float density = context.getResources().getDisplayMetrics().density;
        return (int) (dps * (density / 160.0f));
    }

    public static Point getScreenSizeInPixels(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        Point size = new Point(width, height);
        return size;
    }
}