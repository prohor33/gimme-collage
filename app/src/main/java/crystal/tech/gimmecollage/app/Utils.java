package crystal.tech.gimmecollage.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;

/**
 * Created by prohor on 08/10/14.
 */
public class Utils {
    public static int pixelsToDip(Context context, float pixels) {
        final float density = context.getResources().getDisplayMetrics().density;
        return (int) (pixels * density + 0.5f);
    }
    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    public static Point getScrSizeInPxls(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        Point size = new Point(width, height);
        return size;
    }

    public static boolean checkInternetConnection(Activity activity) {
        ConnectivityManager cm =
                (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static void showAlertNoConnection(Activity activity) {
        AlertDialog.Builder builderInner = new AlertDialog.Builder(activity);
        builderInner.setTitle("No connection");
        builderInner.setMessage("Please check your internet connection");
        builderInner.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {
                        dialog.dismiss();
                    }
                });
        builderInner.show();
    }
}
