package com.example.autofill.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import androidx.preference.PreferenceManager;


public class Authenticate implements BiometricHelper.biometricCallBack{
    private authCallBack listener;
    private Context context;
    private static final String FINGERPRINT = "fingerPrintEnabled";

    public Authenticate(Context context,int buttontext){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;
        if (preferences.getBoolean(FINGERPRINT,false)){
            BiometricHelper biometricHelper = new BiometricHelper(context,buttontext);
            biometricHelper.setListener(this);
        }else {
            final MasterPasswrordPrompt masterPasswrordPrompt = new MasterPasswrordPrompt(context, buttontext);
            masterPasswrordPrompt.setOnclickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (masterPasswrordPrompt.getMasterPassword()==null){
                        return;
                    }
                    masterPasswrordPrompt.getAlertDialog().dismiss();
                    String massPass = masterPasswrordPrompt.getMasterPassword().getText().toString();
                    onSuccessRunOnUIThread(massPass);
                }
            });
        }
    }

    @Override
    public void onBioMetricSuccess(String masterPassword) {
        onSuccessRunOnUIThread(masterPassword);
    }

    @Override
    public void onBioMetricFailed() {
        listener.onAuthenticationFailed();
    }

    public void setListener(authCallBack listener){
        this.listener = listener;
    }

    private void onSuccessRunOnUIThread(final String mastPass){
        Activity activity = (Activity)context;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listener.onAuthenticationSuccess(mastPass);
            }
        });
    }


    public interface authCallBack{
        void onAuthenticationSuccess(String maspass);
        void onAuthenticationFailed();
    }
}
