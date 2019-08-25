package com.example.autofill.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.autofill.AutofillManager;

import com.example.autofill.MainActivity;
import com.example.autofill.R;
import com.example.autofill.dataClass.MasterPasswordEncrypted;
import com.example.autofill.util.BiometricHelper;
import com.example.autofill.util.CacheFileHelper;
import com.example.autofill.util.CipherClass;
import com.example.autofill.util.MasterPasswrordPrompt;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {
    static MainActivity mainActivity;
    private static final String FINGERPRINT = "fingerPrintEnabled";
    private static final String AUTOFILL_SER = "autoFillService";
    private static final String ACCESSIBILITY_SER = "accessibilityService";

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        getFragmentManager().beginTransaction().add(
                R.id.setting_frameLayout, new settingsPreferenceFrag()).commit();
        PreferenceManager.setDefaultValues(mainActivity, R.xml.settings_preference, false);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    public static class settingsPreferenceFrag extends PreferenceFragmentCompat {
        private PreferenceChangeListener preferenceChangeListener = new PreferenceChangeListener(this);

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_preference, rootKey);
            if (mainActivity.getSystemService(AutofillManager.class).hasEnabledAutofillServices()) {
                findPreference(AUTOFILL_SER).setSummary("Enabled");
            } else {
                findPreference(AUTOFILL_SER).setSummary("Disabled");
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(preferenceChangeListener);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        }
    }

    public static class PreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        private PreferenceFragmentCompat prefFragContext;

        public PreferenceChangeListener(PreferenceFragmentCompat context) {
            this.prefFragContext = context;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            if (s.equals(FINGERPRINT)) {
                SwitchPreferenceCompat fingerprintPref = prefFragContext.findPreference(s);
                if (fingerprintPref.isChecked()) {
                    setupFingerPrint(fingerprintPref);
                }
            }
        }

        private void setupFingerPrint(final SwitchPreferenceCompat fpPref) {
            final CacheFileHelper cFile = new CacheFileHelper(mainActivity);
            final CipherClass cc = new CipherClass();
            final MasterPasswrordPrompt mp = new MasterPasswrordPrompt(mainActivity, R.string.linkWithFP);
            mp.setOnclickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mp.getMasterPassword() == null) {
                        return;
                    }
                    mp.getAlertDialog().dismiss();
                    String massPass = mp.getMasterPassword().getText().toString();
                    try {
                        MasterPasswordEncrypted mpe = cc.encryptMasterPassword(massPass, cc.generateKeyStoreKey());
                        cFile.createCachedPassword(mpe);
                        BiometricHelper bmh = new BiometricHelper(mainActivity, R.string.linkWithFP);
                        bmh.setListener(new BiometricHelper.biometricCallBack() {
                            @Override
                            public void onBioMetricSuccess(String masterPassword) {
                                fpPref.setChecked(true);
                            }

                            @Override
                            public void onBioMetricFailed() {
                                fpPref.setChecked(false);
                            }
                        });
                    } catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | KeyStoreException | CertificateException | IOException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | InvalidKeyException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
