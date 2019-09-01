package com.example.autofill.fragments;

import android.animation.AnimatorSet;
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
import com.example.autofill.databinding.FragmentHomeBinding;
import com.google.android.material.animation.AnimatorSetCompat;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private MainActivity mainActivity;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final FragmentHomeBinding binding = DataBindingUtil.inflate(inflater,R.layout.fragment_home,container,false);
        startCountAnimation(mainActivity.dataModel.passwordData.size(),
                new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        binding.setPasswordCnt(valueAnimator.getAnimatedValue().toString());
                    }
                }).start();
        startCountAnimation(mainActivity.dataModel.cardData.size(),
                new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        binding.setCardCnt(valueAnimator.getAnimatedValue().toString());
                    }
                }).start();

        startCountAnimation(mainActivity.dataModel.addressData.size(),
                new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        binding.setAddressCnt(valueAnimator.getAnimatedValue().toString());
                    }
                }).start();
        return binding.getRoot();
    }

    private ValueAnimator startCountAnimation(int upTo,ValueAnimator.AnimatorUpdateListener listener) {
        ValueAnimator animator = new ValueAnimator();
        animator.setIntValues(0,upTo);
        animator.setDuration(1000);
        animator.addUpdateListener(listener);
        return animator;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity)context;
    }

}
