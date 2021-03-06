package crystal.tech.gimmecollage.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.widget.Toast;

/**
 * Created by prohor on 08/10/14.
 */
public class Utils {
    public static final int ADD_PICTURES_REQUEST = 1;  // The request code

    public static boolean checkInternetConnection(Activity activity) {
        ConnectivityManager cm =
                (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static void notifyNoConnection(Context context) {
        Toast.makeText(context, "No internet connection.", Toast.LENGTH_SHORT).show();
    }

    public static boolean checkAndNotifyConnection(Activity activity) {
        boolean connected = checkInternetConnection(activity);
        if(!connected) notifyNoConnection(activity);
        return connected;
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

    public static ProgressDialog createProgressDialog(Context context) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage("Loading...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        return dialog;
    }

    public static void spawnAddImagesActivity(Activity activity) {
        // Spawn ImageSourceActivity.
        Intent intent = new Intent(activity, ImageSourceActivity.class);
        activity.startActivityForResult(intent, ADD_PICTURES_REQUEST);
    }
}
