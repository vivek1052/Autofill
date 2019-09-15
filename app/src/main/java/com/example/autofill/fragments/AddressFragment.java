package com.example.autofill.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.autofill.MainActivity;
import com.example.autofill.R;
import com.example.autofill.adapter.AddressAdapter;
import com.example.autofill.dataClass.AddressDataClass;
import com.example.autofill.dataClass.CardDataClass;
import com.example.autofill.dataClass.IdentityDataClass;
import com.example.autofill.dataClass.PasswordDataClass;
import com.example.autofill.util.DataUpdateCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddressFragment extends Fragment implements DataUpdateCallback {
    MainActivity mainActivity;
    AddressAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_address, container, false);
        ListView listView = view.findViewById(R.id.address_ListView);
        List<AddressDataClass> addressData = mainActivity.dataModel.addressData;
        adapter = new AddressAdapter(mainActivity,addressData);
        listView.setAdapter(adapter);
        view.findViewById(R.id.Fab_address).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.navController.navigate(R.id.action_address_menu_to_newAddressFragment);
            }
        });
        mainActivity.dataModel.addEventLister(this);
        return view;
    }


    @Override
    public void onAttach(Context context) {
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
    public void AddressDataUpdated(final List<AddressDataClass> updatedData) {
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
    public void IdentityDataUpdated(List<IdentityDataClass> updatedData) {

    }
}
