package com.sample.stepupandroid;
/**
 * Copyright 2016 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.worklight.wlclient.api.WLClient;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameInput, passwordInput;
    private Button loginButton;
    private TextView errorMsgDisplay;

    private LoginActivity _this;
    private final String DEBUG_NAME = "LoginActivity";
    private BroadcastReceiver loginErrorReceiver, loginRequiredReceiver, loginSuccessReceiver, pincodeRequiredReceiver;

    //********************************
    // onStart
    //********************************
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(DEBUG_NAME, "onStart");
        LocalBroadcastManager.getInstance(this).registerReceiver(loginRequiredReceiver, new IntentFilter(Constants.ACTION_LOGIN_REQUIRED));
        LocalBroadcastManager.getInstance(this).registerReceiver(loginErrorReceiver, new IntentFilter(Constants.ACTION_LOGIN_FAILURE));
        LocalBroadcastManager.getInstance(this).registerReceiver(loginSuccessReceiver, new IntentFilter(Constants.ACTION_LOGIN_SUCCESS));
        LocalBroadcastManager.getInstance(this).registerReceiver(pincodeRequiredReceiver, new IntentFilter(Constants.ACTION_PINCODE_REQUIRED));
    }

    //********************************
    // onCreate
    //********************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _this = this;
        Log.d(DEBUG_NAME, "onCreate");


        setContentView(R.layout.activity_login);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);

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

        //********************************
        // loginRequiredReceiver
        //********************************
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

        //********************************
        // loginSuccessReceiver
        //********************************
        loginSuccessReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(DEBUG_NAME, "loginSuccessReceiver");

                if(isTaskRoot()){
                    //First time, go to protected area
                    Intent openProtectedScreen = new Intent(_this, ProtectedActivity.class);
                    _this.startActivity(openProtectedScreen);
                } else{
                    //Other times, go "back" to wherever you came from
                    finish();
                }
            }
        };

        //********************************
        // loginErrorReceiver
        //********************************
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
                Intent cancelChallengeIntent = new Intent();
                cancelChallengeIntent.setAction(Constants.ACTION_PINCODE_CANCEL);
                LocalBroadcastManager.getInstance(_this).sendBroadcast(cancelChallengeIntent);
            }
        };
    }

    //********************************
    // onPause
    //********************************
    @Override
    protected void onPause() {
        Log.d(DEBUG_NAME, "onPause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(loginErrorReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(loginRequiredReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(loginSuccessReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pincodeRequiredReceiver);
        super.onPause();
    }

    //********************************
    // alertError
    //********************************
    public void alertError(final String msg) {
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

    //********************************
    // onBackPressed
    //********************************
    @Override
    public void onBackPressed() {
        Log.d(DEBUG_NAME,"onBackPressed");
    }

}
