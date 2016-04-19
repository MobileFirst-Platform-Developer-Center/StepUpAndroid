package com.sample.stepupandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.worklight.wlclient.api.WLAccessTokenListener;
import com.worklight.wlclient.api.WLAuthorizationManager;
import com.worklight.wlclient.api.WLClient;
import com.worklight.wlclient.api.WLFailResponse;
import com.worklight.wlclient.api.WLLoginResponseListener;
import com.worklight.wlclient.api.challengehandler.WLChallengeHandler;
import com.worklight.wlclient.auth.AccessToken;

import org.json.JSONException;
import org.json.JSONObject;


public class StepUpUserLoginChallengeHandler extends WLChallengeHandler {
    private static String securityCheckName = "StepUpUserLogin";
    private String errorMsg = "";
    private Context context;
    private boolean isChallenged = false;
    private LocalBroadcastManager broadcastManager;

    //********************************
    // Constructor
    //********************************
    public StepUpUserLoginChallengeHandler() {
        super(securityCheckName);
        Log.d(securityCheckName, "constructor");
        context = WLClient.getInstance().getContext();
        broadcastManager = LocalBroadcastManager.getInstance(context);

        //Reset the current user
        SharedPreferences preferences = context.getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(Constants.PREFERENCES_KEY_USER);
        editor.commit();

        // Receive login requests
        broadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    JSONObject credentials = new JSONObject(intent.getStringExtra("credentials"));
                    login(credentials);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },new IntentFilter(Constants.ACTION_LOGIN));
    }

    //********************************
    // createAndRegister
    //********************************
    public static StepUpUserLoginChallengeHandler createAndRegister(){
        Log.d(securityCheckName, "createAndRegister");
        StepUpUserLoginChallengeHandler challengeHandler = new StepUpUserLoginChallengeHandler();
        WLClient.getInstance().registerChallengeHandler(challengeHandler);
        return challengeHandler;
    }

    //********************************
    // login
    //********************************
    public void login(JSONObject credentials){
        Log.d(securityCheckName, "login");
        if(isChallenged){
            submitChallengeAnswer(credentials);
        }
        else{
            WLAuthorizationManager.getInstance().login(securityCheckName, credentials, new WLLoginResponseListener() {
                @Override
                public void onSuccess() {
                    Log.d(securityCheckName, "Login Success");
                }

                @Override
                public void onFailure(WLFailResponse wlFailResponse) {
                    Log.d(securityCheckName, "Login Failure");
                }
            });
        }
    }

    //********************************
    // handleChallenge
    //********************************
    @Override
    public void handleChallenge(JSONObject jsonObject) {
        Log.d(securityCheckName, "Challenge Received:\n"+ jsonObject.toString());
        isChallenged = true;
        try {
            if(jsonObject.isNull("errorMsg")){
                errorMsg = "";
            }
            else{
                errorMsg = jsonObject.getString("errorMsg");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_LOGIN_REQUIRED);
        intent.putExtra("errorMsg", errorMsg);
        broadcastManager.sendBroadcast(intent);
    }

    //********************************
    // handleSuccess
    //********************************
    @Override
    public void handleSuccess(JSONObject identity) {
        super.handleSuccess(identity);
        Log.d(securityCheckName, "handleSuccess");
        isChallenged = false;
        try {
            //Save the current user
            SharedPreferences preferences = context.getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.PREFERENCES_KEY_USER, identity.getJSONObject("user").toString());
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_LOGIN_SUCCESS);
        broadcastManager.sendBroadcast(intent);
    }

    //********************************
    // handleFailure
    //********************************
    @Override
    public void handleFailure(JSONObject error) {
        super.handleFailure(error);
        Log.d(securityCheckName, "handleFailure");
        isChallenged = false;
        if(error.isNull("failure")){
            errorMsg = "Failed to login. Please try again later.";
        }
        else {
            try {
                errorMsg = error.getString("failure");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_LOGIN_FAILURE);
        intent.putExtra("errorMsg",errorMsg);
        broadcastManager.sendBroadcast(intent);
        Log.d(securityCheckName, "handleFailure");
    }
}
