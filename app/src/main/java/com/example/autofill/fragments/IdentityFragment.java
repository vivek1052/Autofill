package com.example.autofill.fragments;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.autofill.MainActivity;
import com.example.autofill.R;
import com.example.autofill.adapter.IdentityAdapter;
import com.example.autofill.dataClass.AddressDataClass;
import com.example.autofill.dataClass.CardDataClass;
import com.example.autofill.dataClass.IdentityDataClass;
import com.example.autofill.dataClass.PasswordDataClass;
import com.example.autofill.util.Authenticate;
import com.example.autofill.util.DataUpdateCallback;

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
public class IdentityFragment extends Fragment implements DataUpdateCallback, AdapterView.OnItemClickListener {
    MainActivity mainActivity = null;
    ListView listView;
    IdentityAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_identity, container, false);
        listView = view.findViewById(R.id.identity_ListView);
        List<IdentityDataClass> identityData = new ArrayList<>();
        for (IdentityDataClass id:mainActivity.dataModel.identityData){
            identityData.add(new IdentityDataClass(id));
        }
        adapter = new IdentityAdapter(mainActivity,identityData);
        listView.setAdapter(adapter);
        mainActivity.dataModel.addEventLister(this);
        view.findViewById(R.id.Fab_identity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.navController.navigate(R.id.action_home_menu_to_newIdentityFragment);
            }
        });
        listView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity)context;
    }

    @Override
    public void passwordDataUpdated(List<PasswordDataClass> updatedData) {
        
    }

    @Override
    public void cardDetailUpdated(List<CardDataClass> updatedData) {

    }

    @Override
    public void AddressDataUpdated(List<AddressDataClass> updatedData) {

    }

    @Override
    public void IdentityDataUpdated(final List<IdentityDataClass> updatedData) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.clear();
                adapter.addAll(updatedData);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        final IdentityDataClass identityData =(IdentityDataClass) adapter.getItem(i);
        Authenticate authenticate = new Authenticate(mainActivity,R.string.decrypt);
        authenticate.setListener(new Authenticate.authCallBack() {
            @Override
            public void onAuthenticationSuccess(String mastPass) {
                try {
                    identityData.identityNumber=(mainActivity.cipherClass.decrypt((identityData.identityNumber),mastPass));
                    adapter.notifyDataSetChanged();
                } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAuthenticationFailed() {

            }
        });
    }
}
