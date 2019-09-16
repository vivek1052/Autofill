package com.example.autofill.fragments;


import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.autofill.MainActivity;
import com.example.autofill.R;
import com.example.autofill.adapter.ServiceSuggestAdapter;
import com.example.autofill.dataClass.InstalledApps;
import com.example.autofill.dataClass.PasswordDataClass;
import com.example.autofill.databinding.FragmentNewPasswordBinding;
import com.example.autofill.util.Authenticate;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewPasswordFragment extends Fragment implements View.OnClickListener,
        AdapterView.OnItemClickListener {
    MainActivity mainActivity;
    FragmentNewPasswordBinding binding;
    List<InstalledApps> installedApps;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragmente
        installedApps = new ArrayList<>(mainActivity.dataModel.installedApps);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_password, container, false);
        View view = binding.getRoot();
        binding.service.setAdapter(new ServiceSuggestAdapter(mainActivity, installedApps));
        binding.service.setOnItemClickListener(this);
        binding.passwordSave.setOnClickListener(this);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == binding.passwordSave.getId()) {
            if (TextUtils.isEmpty(binding.service.getText().toString())) {
                binding.service.setError("Cant be empty");
                return;
            }
            Authenticate authenticate = new Authenticate(mainActivity,R.string.encrypt);
            authenticate.setListener(new Authenticate.authCallBack() {
                @Override
                public void onAuthenticationSuccess(String mastPass) {
                    String encUsername = null;
                    String encPassword = null;
                    try {
                        encUsername = mainActivity.cipherClass.encrypt(binding.username.getText().toString(),
                                mastPass);
                        encPassword = mainActivity.cipherClass.encrypt(binding.password.getText().toString(),
                                mastPass);
                    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                        e.printStackTrace();
                    }
                    PasswordDataClass newPassword = new PasswordDataClass(1, binding.service.getText().toString(),
                            binding.subtext.getText().toString(), encUsername, encPassword);
                    mainActivity.dataModel.addNewPassword(newPassword);
                    mainActivity.dataModel.triggerPasswordDataUpdated();
                    mainActivity.navController.navigateUp();
                }

                @Override
                public void onAuthenticationFailed() {

                }
            });
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        InstalledApps installedApp = (InstalledApps) adapterView.getItemAtPosition(i);
        if (installedApp != null) {
            binding.appIcon.setBackground(installedApp.icon);
            binding.subtext.setText(installedApp.packageName);
        }
    }
}
