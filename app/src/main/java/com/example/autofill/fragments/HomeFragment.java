package com.example.autofill.fragments;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.autofill.MainActivity;
import com.example.autofill.R;
import com.example.autofill.adapter.TabAdapter;
import com.example.autofill.dataClass.AddressDataClass;
import com.example.autofill.dataClass.CardDataClass;
import com.example.autofill.dataClass.IdentityDataClass;
import com.example.autofill.dataClass.PasswordDataClass;
import com.example.autofill.databinding.FragmentHomeBinding;
import com.example.autofill.util.DataUpdateCallback;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements DataUpdateCallback {
    private TabAdapter adapter;
    private MainActivity mainActivity;
    private FragmentHomeBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainActivity.dataModel.addEventLister(this);
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_home,container,false);
        binding.viewPager.setAdapter(adapter);
        binding.tabLayout.setupWithViewPager(binding.viewPager);
        setTabIconAndBadge();
        return binding.getRoot();
    }

    private void setTabIconAndBadge() {
        binding.tabLayout.getTabAt(0).setIcon(R.drawable.password)
                .getOrCreateBadge().setNumber(mainActivity.dataModel.passwordData.size());
        binding.tabLayout.getTabAt(1).setIcon(R.drawable.card)
                .getOrCreateBadge().setNumber(mainActivity.dataModel.cardData.size());
        binding.tabLayout.getTabAt(2).setIcon(R.drawable.ic_perm_identity)
                .getOrCreateBadge().setNumber(mainActivity.dataModel.identityData.size());
        binding.tabLayout.getTabAt(3).setIcon(R.drawable.address)
                .getOrCreateBadge().setNumber(mainActivity.dataModel.addressData.size());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity)context;
        adapter = new TabAdapter(getChildFragmentManager());
        adapter.addTab(new PasswordFragment(),getResources().getString(R.string.password));
        adapter.addTab(new CardFragment(),getResources().getString(R.string.card));
        adapter.addTab(new IdentityFragment(),getResources().getString(R.string.identity));
        adapter.addTab(new AddressFragment(),getResources().getString(R.string.address));
    }

    @Override
    public void passwordDataUpdated(List<PasswordDataClass> updatedData) {
        setTabIconAndBadge();
    }

    @Override
    public void cardDetailUpdated(List<CardDataClass> updatedData) {
        setTabIconAndBadge();
    }

    @Override
    public void AddressDataUpdated(List<AddressDataClass> updatedData) {
        setTabIconAndBadge();
    }

    @Override
    public void IdentityDataUpdated(List<IdentityDataClass> updatedData) {
        setTabIconAndBadge();
    }
}
