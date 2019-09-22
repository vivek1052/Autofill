package com.example.autofill.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.autofill.MainActivity;
import com.example.autofill.R;
import com.example.autofill.dataClass.AddressDataClass;
import com.example.autofill.databinding.FragmentNewAddressBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewAddressFragment extends Fragment implements View.OnClickListener{
    private MainActivity mainActivity = null;
    FragmentNewAddressBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_address, container, false);
        binding.addressSave.setOnClickListener(this);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.settings_menu);
        if (menuItem != null){
            menuItem.setVisible(false);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
       mainActivity = (MainActivity)context;
    }

    @Override
    public void onClick(View view) {
        AddressDataClass newAddress = new AddressDataClass(0,binding.name.getText().toString(),
                binding.flatNo.getText().toString(),binding.buildingName.getText().toString(),
                binding.streetNo.getText().toString(),binding.streetName.getText().toString(),
                binding.locality.getText().toString(),binding.city.getText().toString(),
                binding.state.getText().toString(),binding.pinCode.getText().toString(),
                binding.country.getText().toString(),binding.phone.getText().toString());
        mainActivity.dataModel.addNewAddress(newAddress);
        mainActivity.dataModel.triggerAddressDataUpdated();
        mainActivity.navController.navigateUp();
    }
}
