package com.sample.stepupandroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by shmulikb on 14/03/16.
 */
public class ProtectedActivity extends AppCompatActivity {
    private TextView helloTextView, errorMsgTextView;
    private Button getBalanceButton, transferFundsButton;

    private final String DEBUG_NAME = "ProtectedActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(DEBUG_NAME, "onCreate");

        setContentView(R.layout.activity_protected);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        //Initialize the UI elements
        helloTextView = (TextView)findViewById(R.id.HelloTextView);
        getBalanceButton = (Button)findViewById(R.id.getBalance);
        transferFundsButton = (Button)findViewById(R.id.transferFunds);
        errorMsgTextView = (TextView)findViewById(R.id.errorMsg);
        helloTextView.setText("Hello User");
        errorMsgTextView.setText("Errors here...");

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
    }
}
