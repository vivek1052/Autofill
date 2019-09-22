package com.example.autofill.fragments;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.autofill.MainActivity;
import com.example.autofill.R;
import com.example.autofill.dataClass.CardDataClass;
import com.example.autofill.dataClass.PasswordDataClass;
import com.example.autofill.databinding.FragmentNewCardBinding;
import com.example.autofill.util.Authenticate;
import com.example.autofill.util.MasterPasswrordPrompt;
import com.google.android.gms.auth.api.Auth;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewCardFragment extends Fragment implements View.OnClickListener{
    MainActivity mainActivity = null;
    FragmentNewCardBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_card, container, false);
        View view = binding.getRoot();
        binding.cardSave.setOnClickListener(this);
        ArrayAdapter<CharSequence> cardTypeAdapter = ArrayAdapter.createFromResource(mainActivity,
                R.array.card_types, android.R.layout.simple_spinner_item);
        cardTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.cardType.setAdapter(cardTypeAdapter);
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
        mainActivity = (MainActivity)context;
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==binding.cardSave.getId()){
            Authenticate authenticate = new Authenticate(mainActivity,R.string.encrypt);
            authenticate.setListener(new Authenticate.authCallBack() {
                @Override
                public void onAuthenticationSuccess(String mastPass) {
                    CardDataClass newCardData = null;
                    try {
                        String cardNo_1 = (mainActivity.cipherClass.encrypt(binding.cardNo1.getText().toString(), mastPass));
                        String cardNo_2 = (mainActivity.cipherClass.encrypt(binding.cardNo2.getText().toString(), mastPass));
                        String cardNo_3 = (mainActivity.cipherClass.encrypt(binding.cardNo3.getText().toString(), mastPass));
                        String cardNo_4 = (mainActivity.cipherClass.encrypt(binding.cardNo4.getText().toString(), mastPass));
                        String month = (mainActivity.cipherClass.encrypt(binding.month.getText().toString(), mastPass));
                        String year = (mainActivity.cipherClass.encrypt(binding.year.getText().toString(), mastPass));
                        String cvv = (mainActivity.cipherClass.encrypt(binding.cvv.getText().toString(), mastPass));
                        newCardData = new CardDataClass(1, binding.bankName.getText().toString(),
                                binding.cardType.getSelectedItem().toString(), binding.holderName.getText().toString(),
                                cardNo_1, cardNo_2, cardNo_3, cardNo_4, month, year, cvv);
                    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                        e.printStackTrace();
                    }
                    mainActivity.dataModel.addNewCard(newCardData);
                    mainActivity.dataModel.triggerCardDataUpdated();
                    mainActivity.navController.navigateUp();
                }

                @Override
                public void onAuthenticationFailed() { }
            });
        }
    }
}
