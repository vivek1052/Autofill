package com.example.autofill.fragments;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.autofill.AutofillManager;

import com.example.autofill.MainActivity;
import com.example.autofill.R;
import com.example.autofill.dataClass.MasterPasswordEncrypted;
import com.example.autofill.util.BiometricHelper;
import com.example.autofill.util.CacheFileHelper;
import com.example.autofill.util.CipherClass;
import com.example.autofill.util.DriveDataModel;
import com.example.autofill.util.JobSchedulerService;
import com.example.autofill.util.MasterPasswrordPrompt;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.services.drive.DriveScopes;

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
    private static final String GOOGLEACCOUNT = "googleAccountLinked";
    private static final String RETRIEVE_BACKUP = "RetriveFromDrive";
    private static final String RESTRICTED_PACKAGES = "restrictedPackages";
    private static final String DARK_MODE = "darkMode";

    private static final int RC_SIGN_IN = 1;
    private static final int JOB_ID = 305;
    private static final int JOB_INTERVAL_HRS = 12;


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
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.settings_menu);
        if (menuItem != null){
            menuItem.setVisible(false);
        }
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

            findPreference(RETRIEVE_BACKUP).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    retrieveFromDrive();
                    return true;
                }
            });

        }

        private void retrieveFromDrive() {
            Scope scope = new Scope(DriveScopes.DRIVE_APPDATA);
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(scope)
                    .build();
            final GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(mainActivity, gso);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            mainActivity.startActivityForResult(signInIntent, RC_SIGN_IN);
            mainActivity.setCallbackListener(new MainActivity.onCallbacks() {
                @Override
                public void onGoogleSignIn(int resultCode, Intent data) {
                    if (resultCode==mainActivity.RESULT_OK){
                        DriveDataModel driveDataModel = new DriveDataModel(mainActivity);
                        driveDataModel.selectVersion();
                    }else {
                        Snackbar.make(mainActivity.findViewById(android.R.id.content),"Authentication Failed",Snackbar.LENGTH_LONG).show();
                    }
                }
            });

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
        CacheFileHelper cFile;
        public PreferenceChangeListener(PreferenceFragmentCompat context) {
            this.prefFragContext = context;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            cFile = new CacheFileHelper(mainActivity);
            if (s.equals(FINGERPRINT)) {
                SwitchPreferenceCompat fingerprintPref = prefFragContext.findPreference(s);
                if (fingerprintPref.isChecked()) {
                    setupFingerPrint(fingerprintPref);
                }else {
                    MasterPasswordEncrypted mpe = new MasterPasswordEncrypted(new byte[]{},new byte[]{});
                    try {
                        cFile.createCachedPassword(mpe);
                        Snackbar.make(mainActivity.findViewById(android.R.id.content),"Encrypted Master password Deleted",Snackbar.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }else if (s.equals(GOOGLEACCOUNT)){
                Scope scope = new Scope(DriveScopes.DRIVE_APPDATA);
                SwitchPreferenceCompat googleAccPref = prefFragContext.findPreference(s);
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(scope)
                        .build();
                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(mainActivity, gso);
                if (googleAccPref.isChecked()){
                    signInToGoogle(mGoogleSignInClient);
                }else {
                    mGoogleSignInClient.signOut();
                    JobScheduler scheduler =
                            (JobScheduler) mainActivity.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                    scheduler.cancel(JOB_ID);
                    Snackbar.make(mainActivity.findViewById(android.R.id.content),"Auto Backup Stopped",Snackbar.LENGTH_LONG).show();
                }
            }else if (s.equals(DARK_MODE)){
                ListPreference darkMode = prefFragContext.findPreference(s);
                AppCompatDelegate.setDefaultNightMode(Integer.valueOf(darkMode.getValue()));
            }
        }

        private void signInToGoogle(GoogleSignInClient mGoogleSignInClient) {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            mainActivity.startActivityForResult(signInIntent, RC_SIGN_IN);
            mainActivity.setCallbackListener(new MainActivity.onCallbacks() {
                @Override
                public void onGoogleSignIn(int resultCode, Intent data) {
                    ComponentName componentName = new ComponentName(mainActivity, JobSchedulerService.class);
                    JobInfo jobInfo = new JobInfo.Builder(JOB_ID,componentName)
                            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                            .setPersisted(true)
                            .setPeriodic(JOB_INTERVAL_HRS*60*60*1000).build();
                    JobScheduler scheduler =
                            (JobScheduler) mainActivity.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                    scheduler.schedule(jobInfo);
                }
            });
        }

        private void setupFingerPrint(final SwitchPreferenceCompat fpPref) {
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
                                Snackbar.make(mainActivity.findViewById(android.R.id.content),"Linking Successful",Snackbar.LENGTH_LONG).show();
                            }

                            @Override
                            public void onBioMetricFailed() {
                                fpPref.setChecked(false);
                                Snackbar.make(mainActivity.findViewById(android.R.id.content),"Linking Failed",Snackbar.LENGTH_LONG).show();

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
