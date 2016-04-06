package com.sample.stepupandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameInput, passwordInput;
    private Button loginButton;
    private TextView errorMsgDisplay;

    private LoginActivity _this;
    private final String DEBUG_NAME = "LoginActivity";
    private BroadcastReceiver loginErrorReceiver, loginRequiredReceiver, loginSuccessReceiver, pincodeRequiredReceiver;
    private Context context;
    private LocalBroadcastManager broadcastManager;

    @Override
    protected void onStart() {
        Log.d(DEBUG_NAME, "onStart");
        super.onStart();
        broadcastManager = LocalBroadcastManager.getInstance(context);
        LocalBroadcastManager.getInstance(this).registerReceiver(loginRequiredReceiver, new IntentFilter(Constants.ACTION_LOGIN_REQUIRED));
        LocalBroadcastManager.getInstance(this).registerReceiver(loginErrorReceiver, new IntentFilter(Constants.ACTION_LOGIN_FAILURE));
        LocalBroadcastManager.getInstance(this).registerReceiver(loginSuccessReceiver, new IntentFilter(Constants.ACTION_LOGIN_SUCCESS));
        LocalBroadcastManager.getInstance(this).registerReceiver(pincodeRequiredReceiver, new IntentFilter(Constants.ACTION_PINCODE_REQUIRED));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(DEBUG_NAME, "onCreate");
        setContentView(R.layout.activity_login);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);

        _this = this;

        //Initialize the UI elements
        usernameInput = (EditText)findViewById(R.id.usernameInput);
        passwordInput = (EditText)findViewById(R.id.passwordInput);
        errorMsgDisplay = (TextView)findViewById(R.id.errorMsg);
        loginButton = (Button)findViewById(R.id.login);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(usernameInput.getText().toString().isEmpty() || passwordInput.getText().toString().isEmpty()){
                    alertError("Username and password are required");
                }
                else{
                    JSONObject credentials = new JSONObject();
                    try {
                        credentials.put("username",usernameInput.getText().toString());
                        credentials.put("password",passwordInput.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent();
                    intent.setAction(Constants.ACTION_LOGIN);
                    intent.putExtra("credentials",credentials.toString());
                    LocalBroadcastManager.getInstance(_this).sendBroadcast(intent);
                }
            }
        });

        //Login required
        loginRequiredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                Log.d(DEBUG_NAME, "loginRequiredReceiver");
                Runnable run = new Runnable() {
                    public void run() {
                        //Set error message:
                        errorMsgDisplay.setText(intent.getStringExtra("errorMsg"));
                    }
                };
                _this.runOnUiThread(run);
            }
        };

        //Login success
        loginSuccessReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(DEBUG_NAME, "loginSuccessReceiver");
                //Go to the protected area
                //Intent openProtectedActivity = new Intent(_this, ProtectedActivity.class);
                //_this.startActivity(openProtectedActivity);
                finish();
            }
        };

        //Login error receiver
        loginErrorReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(DEBUG_NAME, "loginErrorReceiver");
                errorMsgDisplay.setText("");
                alertError(intent.getStringExtra("errorMsg"));
            }
        };

        //*****************************************
        // pincodeRequired BroadcastReceiver
        //*****************************************
        pincodeRequiredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(DEBUG_NAME, "pincodeRequiredReceiver");
                //StepUpPinCodeChallengeHandler.isPinCodeChallengeAcceptedInLoginActivity = 1;
                finish();
            }
        };
    }

    @Override
    protected void onStop() {
        Log.d(DEBUG_NAME,"onStop");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(loginErrorReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(loginRequiredReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(loginSuccessReceiver);
        super.onStop();
    }

    public void alertError(final String msg) {
        Log.d(DEBUG_NAME, "alertError");
        Runnable run = new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(_this);
                builder.setMessage(msg)
                        .setTitle("Error");
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        };
        _this.runOnUiThread(run);
    }

    @Override
    public void onBackPressed() {
        Log.d(DEBUG_NAME,"onBackPressed");
    }

}
