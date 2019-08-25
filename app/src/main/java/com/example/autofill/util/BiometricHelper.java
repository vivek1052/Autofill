package com.example.autofill.util;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.biometric.BiometricPrompt.AuthenticationCallback;
import androidx.fragment.app.FragmentActivity;

import com.example.autofill.MainActivity;
import com.example.autofill.dataClass.MasterPasswordEncrypted;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class BiometricHelper extends AuthenticationCallback{
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private BiometricPrompt biometricPrompt;
    private biometricCallBack listner;
    private CacheFileHelper cacheFileHelper;
    private CipherClass cipherClass;

    public BiometricHelper(Context context, int buttonText){
        cacheFileHelper = new CacheFileHelper(context);
        cipherClass = new CipherClass();
        String title = context.getResources().getString(buttonText);
        biometricPrompt = new BiometricPrompt((FragmentActivity) context, executor,this);
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(title).setNegativeButtonText("Password")
                .build();
        biometricPrompt.authenticate(promptInfo);
    }

    @Override
    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        listner.onBioMetricFailed();
    }

    @Override
    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        String masspass;
        try {
            MasterPasswordEncrypted mp = (MasterPasswordEncrypted) cacheFileHelper.getCachedPassword();
            masspass = cipherClass.decryptMasterPassword(mp, cipherClass.getKeyStoreKey());
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException | UnrecoverableEntryException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        listner.onBioMetricSuccess(masspass);
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        listner.onBioMetricFailed();
    }

    public void setListener(biometricCallBack listner){
        this.listner = listner;
    }

    public interface biometricCallBack{
         void onBioMetricSuccess(String masterPassword);
         void onBioMetricFailed();
    }
}
