package crystal.games.gimmecollage.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import crystal.games.gimmecollage.instagram_api.InstagramApp;


public class MainActivity extends Activity {

    private InstagramApp mApp;
    private Button btnConnect;
    private Button btnGetImages;
    private TextView tvSummary;
    private static final String TAG = "MainActivity";

    enum Task { None, FetchFriends };
    private Task m_eNextTask = Task.None;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mApp = new InstagramApp(this, ApplicationData.CLIENT_ID,
                ApplicationData.CLIENT_SECRET, ApplicationData.CALLBACK_URL);
        mApp.setListener(auth_listener);

        tvSummary = (TextView) findViewById(R.id.textView);

        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mApp.hasAccessToken()) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(
                            MainActivity.this);
                    builder.setMessage("Disconnect from Instagram?")
                            .setCancelable(false)
                            .setPositiveButton("Yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            mApp.resetAccessToken();
                                            btnConnect.setText("Connect");
                                            tvSummary.setText("Not connected");
                                        }
                                    })
                            .setNegativeButton("No",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    final AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    m_eNextTask = Task.None;
                    mApp.authorize();
                }
            }
        });

        btnGetImages = (Button)findViewById(R.id.btnPickFriend);
        btnGetImages.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {

                if (!mApp.hasAccessToken()) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(
                            MainActivity.this);
                    builder.setMessage("You are not connected. Connect to Instagram?")
                            .setCancelable(false)
                            .setPositiveButton("Yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            m_eNextTask = Task.FetchFriends;
                                            mApp.authorize();
                                        }
                                    })
                            .setNegativeButton("No",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    final AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    runFetchingFriends();
                }
            }
        });

        if (mApp.hasAccessToken()) {
            tvSummary.setText("Connected as " + mApp.getUserName());
            btnConnect.setText("Disconnect");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    InstagramApp.APIRequestListener auth_listener = new InstagramApp.APIRequestListener() {

        @Override
        public void onSuccess() {
            tvSummary.setText("Connected as " + mApp.getUserName());
            btnConnect.setText("Disconnect");
            if (m_eNextTask == Task.FetchFriends) {
                m_eNextTask = Task.None;
                runFetchingFriends();
            }
        }

        @Override
        public void onFail(String error) {
            Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
            m_eNextTask = Task.None;
        }
    };

    InstagramApp.APIRequestListener friends_load_listener = new InstagramApp.APIRequestListener() {

        @Override
        public void onSuccess() {
            Log.v(TAG, "Friends info successfully loaded!");
            Intent intent = new Intent(MainActivity.this, FriendPicker.class);
            Bundle bundle = new Bundle();
            bundle.putStringArray("friends_array", mApp.getFriendsNames());
            intent.putExtras(bundle);

            startActivity(intent);
            Log.v(TAG, "Start FriendPicker activity");
        }

        @Override
        public void onFail(String error) {
            Log.v(TAG, "Failed to load friends info");
            Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
        }
    };

    private void runFetchingFriends() {
        mApp.setListener(friends_load_listener);
        mApp.fetchFriends();
    }
}
