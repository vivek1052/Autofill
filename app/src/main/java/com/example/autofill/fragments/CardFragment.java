package com.example.autofill.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.autofill.MainActivity;
import com.example.autofill.R;
import com.example.autofill.adapter.CardAdapter;
import com.example.autofill.dataClass.AddressDataClass;
import com.example.autofill.dataClass.CardDataClass;
import com.example.autofill.dataClass.IdentityDataClass;
import com.example.autofill.dataClass.PasswordDataClass;
import com.example.autofill.util.Authenticate;
import com.example.autofill.util.DataUpdateCallback;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * A simple {@link Fragment} subclass.
 */
public class CardFragment extends Fragment implements DataUpdateCallback, AdapterView.OnItemClickListener {
    MainActivity mainActivity;
    CardAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_card, container, false);
        ListView listView = view.findViewById(R.id.card_ListView);
        List<CardDataClass> cardDataList = new ArrayList<>();
        for (CardDataClass cd: mainActivity.dataModel.cardData){
            cardDataList.add(new CardDataClass(cd));
        }
        adapter = new CardAdapter(mainActivity,cardDataList);
        listView.setAdapter(adapter);
        mainActivity.dataModel.addEventLister(this);
        listView.setOnItemClickListener(this);
        listView.setMultiChoiceModeListener(new onMultiSelect());
        view.findViewById(R.id.Fab_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.navController.navigate(R.id.action_homeFragment_to_newCardFragment);
            }
        });
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
    public void cardDetailUpdated(final List<CardDataClass> updatedData) {
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
    public void AddressDataUpdated(List<AddressDataClass> updatedData) {

    }

    @Override
    public void IdentityDataUpdated(List<IdentityDataClass> updatedData) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        final CardDataClass cardData =(CardDataClass) adapter.getItem(i);
        Authenticate authenticate = new Authenticate(mainActivity,R.string.decrypt);
        authenticate.setListener(new Authenticate.authCallBack() {
            @Override
            public void onAuthenticationSuccess(String mastPass) {
                try {
                    cardData.cardNo1=(mainActivity.cipherClass.decrypt((cardData.cardNo1),mastPass));
                    cardData.cardNo2=(mainActivity.cipherClass.decrypt((cardData.cardNo2),mastPass));
                    cardData.cardNo3=(mainActivity.cipherClass.decrypt((cardData.cardNo3),mastPass));
                    cardData.cardNo4=(mainActivity.cipherClass.decrypt((cardData.cardNo4),mastPass));
                    cardData.month=(mainActivity.cipherClass.decrypt((cardData.month),mastPass));
                    cardData.year=(mainActivity.cipherClass.decrypt((cardData.year),mastPass));
                    cardData.cvv=(mainActivity.cipherClass.decrypt((cardData.cvv),mastPass));
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

    class onMultiSelect implements AbsListView.MultiChoiceModeListener{
        @Override
        public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
            if (b){
                adapter.selectedItems.add((CardDataClass)adapter.getItem(i));
                adapter.notifyDataSetChanged();
                actionMode.setTitle(String.valueOf(adapter.selectedItems.size()));
            }else {
                adapter.selectedItems.remove(adapter.getItem(i));
                adapter.notifyDataSetChanged();
                actionMode.setTitle(String.valueOf(adapter.selectedItems.size()));
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater menuInflater = actionMode.getMenuInflater();
            menuInflater.inflate(R.menu.contextual_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.delete_button:
                    mainActivity.dataModel.deleteCards(adapter.selectedItems);
                    mainActivity.dataModel.triggerCardDataUpdated();
                    actionMode.finish();
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            adapter.selectedItems.clear();
            adapter.notifyDataSetChanged();
        }
    }
}
