package com.example.autofill.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.autofill.MainActivity;
import com.example.autofill.R;
import com.example.autofill.adapter.CardAdapter;
import com.example.autofill.dataClass.AddressDataClass;
import com.example.autofill.dataClass.CardDataClass;
import com.example.autofill.dataClass.PasswordDataClass;
import com.example.autofill.util.CustomEvents;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class CardFragment extends Fragment implements CustomEvents {
    MainActivity mainActivity;
    CardAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_card, container, false);
        ListView listView = view.findViewById(R.id.card_ListView);
        List<CardDataClass> cardDataList = mainActivity.dataModel.cardData;
        adapter = new CardAdapter(mainActivity,cardDataList);
        listView.setAdapter(adapter);
        mainActivity.dataModel.addEventLister(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("cardData",(CardDataClass)adapterView.getItemAtPosition(i));
                mainActivity.navController.navigate(R.id.action_card_menu_to_displayCardFragment,
                        bundle);
            }
        });
        view.findViewById(R.id.Fab_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.navController.navigate(R.id.action_card_menu_to_newCardFragment);
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
}
