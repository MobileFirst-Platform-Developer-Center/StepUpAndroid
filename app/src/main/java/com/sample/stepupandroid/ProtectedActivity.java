package com.sample.stepupandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.worklight.wlclient.api.WLAuthorizationManager;
import com.worklight.wlclient.api.WLFailResponse;
import com.worklight.wlclient.api.WLLogoutResponseListener;
import com.worklight.wlclient.api.WLResourceRequest;
import com.worklight.wlclient.api.WLResponse;
import com.worklight.wlclient.api.WLResponseListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class ProtectedActivity extends AppCompatActivity {
    private ProtectedActivity _this;
    private TextView helloTextView, errorMsgTextView;
    private Button getBalanceButton, transferFundsButton, logoutButton;
    private URI adapterPath = null;
    private BroadcastReceiver pincodeRequiredReceiver, pincodeFailureReceiver, loginRequiredReceiver;
    private Context context;
    private LocalBroadcastManager broadcastManager;

    private final String DEBUG_NAME = "ProtectedActivity";

    @Override
    protected void onStart() {
        Log.d(DEBUG_NAME, "onStart");
        super.onStart();
        broadcastManager = LocalBroadcastManager.getInstance(context);
        LocalBroadcastManager.getInstance(this).registerReceiver(pincodeRequiredReceiver, new IntentFilter(Constants.ACTION_PINCODE_REQUIRED));
        LocalBroadcastManager.getInstance(this).registerReceiver(pincodeFailureReceiver, new IntentFilter(Constants.ACTION_PINCODE_FAILURE));
        LocalBroadcastManager.getInstance(this).registerReceiver(loginRequiredReceiver, new IntentFilter(Constants.ACTION_LOGIN_REQUIRED));
    }

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

        //*****************************************
        // getBalanceButton - OnClickListener
        //*****************************************
        getBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(DEBUG_NAME, "getBalanceButton clicked");
                try {
                    adapterPath = new URI("/adapters/ResourceAdapter/balance");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                WLResourceRequest request = new WLResourceRequest(adapterPath, WLResourceRequest.GET);
                request.send(new WLResponseListener() {
                    @Override
                    public void onSuccess(WLResponse wlResponse) {
                        Log.d("Balance: ", wlResponse.getResponseText());
                        updateTextView("Balance: " + wlResponse.getResponseText());
                    }

                    @Override
                    public void onFailure(WLFailResponse wlFailResponse) {
                        Log.d("Failed to get balance: ", wlFailResponse.getErrorMsg());
                        updateTextView("Failed to get balance: " + wlFailResponse.getErrorMsg());
                    }
                });
            }
        });

        //*****************************************
        // transferFundsButton - OnClickListener
        //*****************************************
        transferFundsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(DEBUG_NAME, "transferFundsButton clicked");
                // Create an AlertDialog to enter transfer amount
                AlertDialog.Builder builder = new AlertDialog.Builder(_this);
                builder.setTitle("Enter Amount:");
                // Add input text field to the AlertDialog
                final EditText input = new EditText(_this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);
                // Set up the buttons to the AlertDialog
                builder.setPositiveButton("Transfer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Make a WLResourceRequest to the adapter's transfer endpoint
                        try {
                            adapterPath = new URI("/adapters/ResourceAdapter/transfer");
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                        WLResourceRequest request = new WLResourceRequest(adapterPath, WLResourceRequest.POST);
                        // Add the amount as a formParam to the request
                        HashMap formParams = new HashMap();
                        formParams.put("amount", input.getText().toString());
                        request.send(formParams, new WLResponseListener() {
                            @Override
                            public void onSuccess(WLResponse wlResponse) {
                                Log.d(DEBUG_NAME, "Transfer Success!");
                                updateTextView("Transfer Success!");
                            }

                            @Override
                            public void onFailure(WLFailResponse wlFailResponse) {
                                Log.d(DEBUG_NAME, "Transfer Failure: " + wlFailResponse.getErrorMsg());
                                updateTextView("Transfer Failed!");
                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                // Display the AlertDialog
                builder.show();
            }
        });

        //*****************************************
        // pincodeRequired BroadcastReceiver
        //*****************************************
        pincodeRequiredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(DEBUG_NAME, "pincodeRequiredReceiver");
                // Create an AlertDialog to enter PinCode
                AlertDialog.Builder builder = new AlertDialog.Builder(_this);
                builder.setTitle("Enter pincode:");
                // Add input text field to the AlertDialog
                final EditText input = new EditText(_this);
                input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);
                // Set up the buttons to the AlertDialog
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Send broadcast to PinCode-challenge-handler with entered pincode
                        Intent intent = new Intent();
                        intent.setAction(Constants.ACTION_PINCODE_SUBMIT_ANSWER);
                        intent.putExtra("credentials", input.getText().toString());
                        broadcastManager.sendBroadcast(intent);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                // Display the AlertDialog
                builder.show();
            }
        };

        //*****************************************
        // pincodeFailure Receiver
        //*****************************************
        pincodeFailureReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(DEBUG_NAME, "pincodeFailureReceiver");
                updateTextView("Transfer failure!");
            }
        };

        //*****************************************
        // loginRequired Receiver
        //*****************************************
        loginRequiredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                Log.d(DEBUG_NAME, "loginRequiredReceiver");
                //Open login screen
                Intent openLoginScreen = new Intent(_this, LoginActivity.class);
                _this.startActivity(openLoginScreen);
            }
        };


        //*****************************************
        // logoutButton - OnClickListener
        //*****************************************
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(DEBUG_NAME, "logoutButton clicked");

                WLAuthorizationManager.getInstance().logout("StepUpUserLogin", new WLLogoutResponseListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(DEBUG_NAME, "StepUpUserLogin->Logout Success");
                        WLAuthorizationManager.getInstance().logout("StepUpPinCode", new WLLogoutResponseListener() {
                            @Override
                            public void onSuccess() {
                                Log.d(DEBUG_NAME, "StepUpPinCode->Logout Success");
                                // Go to start screen
                                Intent openStartScreen = new Intent(_this, StartActivity.class);
                                _this.startActivity(openStartScreen);
                            }

                            @Override
                            public void onFailure(WLFailResponse wlFailResponse) {
                                Log.d(DEBUG_NAME, "StepUpPinCode->Logout Failure");
                            }
                        });
                    }

                    @Override
                    public void onFailure(WLFailResponse wlFailResponse) {
                        Log.d(DEBUG_NAME, "StepUpUserLogin->Logout Failure");
                    }
                });
            }
        });
    }

    //*****************************************
    // updateTextView
    //*****************************************
    public void updateTextView(final String str){
        Runnable run = new Runnable() {
            public void run() {
                errorMsgTextView.setText(str);
            }
        };
        this.runOnUiThread(run);
    }

    @Override
    protected void onStop() {
        Log.d(DEBUG_NAME,"onStop");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pincodeRequiredReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pincodeFailureReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(loginRequiredReceiver);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        Log.d(DEBUG_NAME,"onBackPressed");
    }
}
