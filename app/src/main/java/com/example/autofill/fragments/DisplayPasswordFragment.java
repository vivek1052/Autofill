package com.example.autofill.fragments;


import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.autofill.MainActivity;
import com.example.autofill.R;
import com.example.autofill.dataClass.PasswordDataClass;
import com.example.autofill.databinding.FragmentDisplayPasswordBinding;
import com.example.autofill.util.Contract;
import com.example.autofill.util.MasterPasswrordPrompt;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DisplayPasswordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DisplayPasswordFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "passwordData";

    // TODO: Rename and change types of parameters
    private PasswordDataClass passwordData;
    FragmentDisplayPasswordBinding binding = null;
    MainActivity mainActivity = null;
    MasterPasswrordPrompt prompt = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            Commented because it was acting as two wAY binding between parent and destination fragment
//            passwordData = (PasswordDataClass) getArguments().getSerializable(ARG_PARAM1);
            passwordData = new PasswordDataClass((PasswordDataClass) getArguments().getSerializable(ARG_PARAM1));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_display_password,container,false);
        binding.setPasswordData(passwordData);
        try {
            binding.appIcon.setBackground(mainActivity.getPackageManager().getApplicationIcon(passwordData.subText));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        binding.passwordDecrypt.setOnClickListener(this);
        return binding.getRoot();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity)context;
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.passwordDecrypt){
            prompt = new MasterPasswrordPrompt(mainActivity,R.string.decrypt);
            prompt.setOnclickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (prompt.getMasterPassword()==null){
                        return;
                    }
                    String mastPass = prompt.getMasterPassword().getText().toString();
                    try {
                        passwordData.username = mainActivity.cipherClass.decrypt(passwordData.username,mastPass);
                        passwordData.password = mainActivity.cipherClass.decrypt(passwordData.password,mastPass);
                        binding.setPasswordData(passwordData);
                        binding.passwordDecrypt.setVisibility(View.GONE);
                        prompt.getAlertDialog().dismiss();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
