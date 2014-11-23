package crystal.games.gimmecollage.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.Toast;

import crystal.games.gimmecollage.instagram_api.InstagramAPI;


public class AuthenticationActivity extends ActionBarActivity {

    private static final String TAG = "AuthenticationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        WebView webView = (WebView) findViewById(R.id.webView);
        InstagramAPI.Listener authListener = new InstagramAPI.Listener() {
            @Override
            public void onSuccess() {
                Toast.makeText(AuthenticationActivity.this, "Success", Toast.LENGTH_LONG).show();
                AuthenticationActivity.this.finish();
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(AuthenticationActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        };

        if (checkInternetConnection()) {
            InstagramAPI.with(authListener).startAuthentication(webView);
        } else {
            ShowAlertNoConnection();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.authentication, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkInternetConnection() {
        ConnectivityManager conMgr = (ConnectivityManager) AuthenticationActivity.this.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo i = conMgr.getActiveNetworkInfo();
        if (i == null)
            return false;
        if (!i.isConnected())
            return false;
        if (!i.isAvailable())
            return false;
        return true;
    }

    private void ShowAlertNoConnection() {
        AlertDialog.Builder builderInner = new AlertDialog.Builder(
                AuthenticationActivity.this);
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
