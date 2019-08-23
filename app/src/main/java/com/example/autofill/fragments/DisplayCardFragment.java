package com.example.autofill.fragments;


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
import com.example.autofill.dataClass.CardDataClass;
import com.example.autofill.databinding.FragmentDisplayCardBinding;
import com.example.autofill.util.MasterPasswrordPrompt;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DisplayCardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DisplayCardFragment extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "cardData";
    FragmentDisplayCardBinding binding=null;
    MainActivity mainActivity;
    // TODO: Rename and change types of parameters
    private CardDataClass cardData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            CardDataClass cd = (CardDataClass) getArguments().getSerializable(ARG_PARAM1);
            cardData = new CardDataClass(cd);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_display_card, container, false);
        binding.setCardData(cardData);
        View view = binding.getRoot();
        binding.cardDecrypt.setOnClickListener(this);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity)context;
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==binding.cardDecrypt.getId()){
           final MasterPasswrordPrompt prompt = new MasterPasswrordPrompt(mainActivity,R.string.decrypt);
            prompt.setOnclickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (prompt.getMasterPassword()==null){
                        return;
                    }
                    String mastPass = prompt.getMasterPassword().getText().toString();
                    try {
                        cardData.cardNo1=(mainActivity.cipherClass.decrypt((cardData.cardNo1),mastPass));
                        cardData.cardNo2=(mainActivity.cipherClass.decrypt((cardData.cardNo2),mastPass));
                        cardData.cardNo3=(mainActivity.cipherClass.decrypt((cardData.cardNo3),mastPass));
                        cardData.cardNo4=(mainActivity.cipherClass.decrypt((cardData.cardNo4),mastPass));
                        cardData.month=(mainActivity.cipherClass.decrypt((cardData.month),mastPass));
                        cardData.year=(mainActivity.cipherClass.decrypt((cardData.year),mastPass));
                        cardData.cvv=(mainActivity.cipherClass.decrypt((cardData.cvv),mastPass));
                        binding.setCardData(cardData);
                        binding.cardDecrypt.setVisibility(View.GONE);
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

    public String iToS(int i){
        return Integer.toString(i);
    }

    public int sToI(String s){
        return Integer.valueOf(s);
    }
}
