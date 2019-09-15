package com.example.autofill.fragments;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.autofill.MainActivity;
import com.example.autofill.R;
import com.example.autofill.dataClass.CardDataClass;
import com.example.autofill.dataClass.IdentityDataClass;
import com.example.autofill.databinding.FragmentNewIdentityBinding;
import com.example.autofill.util.Authenticate;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewIdentityFragment extends Fragment implements View.OnClickListener{
    MainActivity mainActivity;
    FragmentNewIdentityBinding binding;
    private static final String AADHAAR_CARD = "Aadhaar Card";
    private static final String DRIVING_LICENCE = "Driving Licence";
    private static final String PAN_CARD = "Pan Card";
    private static final String PASSPORT = "Passport";
    private static final String UAN_NUMBER = "UAN Number";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_identity, container, false);
        View view = binding.getRoot();
        ArrayAdapter<CharSequence> IdentityTypeAdapter = ArrayAdapter.createFromResource(mainActivity,
                R.array.identity_types, android.R.layout.simple_spinner_item);
        IdentityTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.identityType.setAdapter(IdentityTypeAdapter);
        binding.identityType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String identityType = (String) adapterView.getItemAtPosition(i);
                if (identityType.equals(AADHAAR_CARD)){
                    binding.identityIcon.setBackground(mainActivity.getResources().getDrawable(R.drawable.ic_aadhaar_logo,
                            mainActivity.getTheme()));
                }else if (identityType.equals(DRIVING_LICENCE)){
                    binding.identityIcon.setBackground(mainActivity.getResources().getDrawable(R.drawable.ic_driver_license,
                            mainActivity.getTheme()));
                }else if (identityType.equals(PAN_CARD)){
                    binding.identityIcon.setBackground(mainActivity.getResources().getDrawable(R.drawable.pancard,
                            mainActivity.getTheme()));
                }else if (identityType.equals(PASSPORT)){
                    binding.identityIcon.setBackground(mainActivity.getResources().getDrawable(R.drawable.ic_passport,
                            mainActivity.getTheme()));
                }else if (identityType.equals(UAN_NUMBER)){
                    binding.identityIcon.setBackground(mainActivity.getResources().getDrawable(R.drawable.uan,
                            mainActivity.getTheme()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        binding.identitySave.setOnClickListener(this);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity)context;
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==binding.identitySave.getId()){
            Authenticate authenticate = new Authenticate(mainActivity,R.string.encrypt);
            authenticate.setListener(new Authenticate.authCallBack() {
                @Override
                public void onAuthenticationSuccess(String mastPass) {
                    IdentityDataClass newIdentityData = null;
                    try {
                        String identityType = binding.identityType.getSelectedItem().toString();
                        String identityNum = (mainActivity.cipherClass.encrypt(binding.identityNumber.getText().toString(), mastPass));
                        newIdentityData = new IdentityDataClass(0,identityType,identityNum);
                    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                        e.printStackTrace();
                    }
                    mainActivity.dataModel.addNewIdentity(newIdentityData);
                    mainActivity.dataModel.triggerIdentityDataUpdated();
                    mainActivity.navController.navigateUp();
                }

                @Override
                public void onAuthenticationFailed() { }
            });
        }
        }
    }

