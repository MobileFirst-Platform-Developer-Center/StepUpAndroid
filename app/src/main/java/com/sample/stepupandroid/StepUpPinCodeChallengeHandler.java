package com.sample.stepupandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


import com.worklight.wlclient.api.WLClient;
import com.worklight.wlclient.api.challengehandler.WLChallengeHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class StepUpPinCodeChallengeHandler extends WLChallengeHandler {
    private static String securityCheckName = "StepUpPinCode";
    private String errorMsg = "";
    private Context context;
    private LocalBroadcastManager broadcastManager;

    //********************************
    // Constructor
    //********************************
    public StepUpPinCodeChallengeHandler() {
        super(securityCheckName);
        context = WLClient.getInstance().getContext();
        broadcastManager = LocalBroadcastManager.getInstance(context);

        // Receive login requests
        broadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String credentials = intent.getStringExtra("credentials");
                submitAnswer(credentials);
            }
        },new IntentFilter(Constants.ACTION_PINCODE_SUBMIT_ANSWER));
    }

    //********************************
    // createAndRegister
    //********************************
    public static StepUpPinCodeChallengeHandler createAndRegister(){
        StepUpPinCodeChallengeHandler challengeHandler = new StepUpPinCodeChallengeHandler();
        WLClient.getInstance().registerChallengeHandler(challengeHandler);
        return challengeHandler;
    }

    //********************************
    // submitAnswer
    //********************************
    public void submitAnswer(String credentials) {
        Log.d(securityCheckName, "submitAnswer");
        try {
            submitChallengeAnswer(new JSONObject().put("pin", credentials));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //********************************
    // handleChallenge
    //********************************
    @Override
    public void handleChallenge(JSONObject jsonObject) {
        Log.d(securityCheckName, "handleChallenge");
        String hint = null;
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_PINCODE_REQUIRED);
        intent.putExtra("errorMsg", jsonObject.toString());
        broadcastManager.sendBroadcast(intent);

    }

    //********************************
    // handleSuccess
    //********************************
    @Override
    public void handleSuccess(JSONObject identity) {
        super.handleSuccess(identity);
        Log.d(securityCheckName, "handleSuccess");
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_PINCODE_SUCCESS);
        broadcastManager.sendBroadcast(intent);
    }

    //********************************
    // handleFailure
    //********************************
    @Override
    public void handleFailure(JSONObject error) {
        super.handleFailure(error);
        Log.d(securityCheckName, "handleFailure");
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_PINCODE_FAILURE);
        intent.putExtra("errorMsg",error.toString());
        broadcastManager.sendBroadcast(intent);
    }
}
