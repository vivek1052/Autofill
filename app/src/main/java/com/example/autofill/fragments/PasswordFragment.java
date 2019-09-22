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
import com.example.autofill.adapter.PasswordAdapter;
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
public class PasswordFragment extends Fragment implements DataUpdateCallback {
    MainActivity mainActivity;
    PasswordAdapter adapter;
    ListView listView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_password, container, false);
        listView = view.findViewById(R.id.password_ListView);
        List<PasswordDataClass> passwordData = mainActivity.dataModel.passwordData;
        adapter = new PasswordAdapter(mainActivity,passwordData);
        listView.setAdapter(adapter);
        mainActivity.dataModel.addEventLister(this);
        listView.setMultiChoiceModeListener(new onMultiSelect());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("passwordData",(PasswordDataClass)adapterView.getItemAtPosition(i));
                mainActivity.navController.navigate(R.id.action_homeFragment_to_displayPasswordFragment,
                        bundle);
            }
        });
        view.findViewById(R.id.Fab_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.navController.navigate(R.id.action_homeFragment_to_newPasswordFragment);
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
    public void passwordDataUpdated(final List<PasswordDataClass> updatedData) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.clear();
                adapter.addAll(updatedData);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void cardDetailUpdated(List<CardDataClass> updatedData) {

    }

    @Override
    public void AddressDataUpdated(List<AddressDataClass> updatedData) {

    }

    @Override
    public void IdentityDataUpdated(List<IdentityDataClass> updatedData) {

    }


    class onMultiSelect implements AbsListView.MultiChoiceModeListener{
        @Override
        public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
            if (b){
                adapter.selectedItems.add((PasswordDataClass)adapter.getItem(i));
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
                    mainActivity.dataModel.deletePasswords(adapter.selectedItems);
                    mainActivity.dataModel.triggerPasswordDataUpdated();
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
