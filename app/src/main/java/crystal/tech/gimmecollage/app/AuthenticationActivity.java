package crystal.tech.gimmecollage.app;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.Toast;

import crystal.tech.gimmecollage.instagram_api.InstagramAPI;


public class AuthenticationActivity extends ActionBarActivity {

    private static final String TAG = "AuthenticationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        WebView webView = (WebView) findViewById(R.id.webView);
        InstagramAPI.Listener authListener = new InstagramAPI.Listener() {
            @Override
            public void onSuccess() {
                Toast.makeText(AuthenticationActivity.this, "Authentication successful",
                        Toast.LENGTH_SHORT).show();
                AuthenticationActivity.this.setResult(RESULT_OK);
                AuthenticationActivity.this.finish();
            }

            @Override
            public void onFail(String error) {
                Log.e(TAG, "Error: " + error);
                Utils.checkAndNotifyConnection(AuthenticationActivity.this);
                AuthenticationActivity.this.finish();
            }
        };

        InstagramAPI.with(authListener).startAuthentication(webView);
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
        /*
        if (id == R.id.action_settings) {
            return true;
        }
        */
        return super.onOptionsItemSelected(item);
    }
}
