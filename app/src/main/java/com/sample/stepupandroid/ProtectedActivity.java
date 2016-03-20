package com.sample.stepupandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by shmulikb on 14/03/16.
 */
public class ProtectedActivity extends AppCompatActivity {
    private ProtectedActivity _this;
    private TextView helloTextView, errorMsgTextView;
    private Button getBalanceButton, transferFundsButton, logoutButton;

    private final String DEBUG_NAME = "ProtectedActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(DEBUG_NAME, "onCreate");

        _this = this;
        setContentView(R.layout.activity_protected);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_with_logout_button);

        //Initialize the UI elements
        helloTextView = (TextView)findViewById(R.id.helloTextView);
        getBalanceButton = (Button)findViewById(R.id.getBalance);
        transferFundsButton = (Button)findViewById(R.id.transferFunds);
        logoutButton = (Button)findViewById(R.id.logout);
        errorMsgTextView = (TextView)findViewById(R.id.errorMsg);

        //Show the display name
        try {
            SharedPreferences preferences = _this.getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);
            JSONObject user = new JSONObject(preferences.getString(Constants.PREFERENCES_KEY_USER,null));
            helloTextView.setText("Hello " + user.getString("displayName"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(DEBUG_NAME, "getBalanceButton clicked");
            }
        });

        transferFundsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(DEBUG_NAME, "transferFundsButton clicked");
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(DEBUG_NAME, "logoutButton clicked");
            }
        });
    }
}
