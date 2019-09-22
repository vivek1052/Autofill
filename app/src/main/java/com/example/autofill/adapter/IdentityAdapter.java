package com.example.autofill.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.example.autofill.R;
import com.example.autofill.dataClass.IdentityDataClass;
import com.example.autofill.databinding.IdentityRowBinding;

import java.util.ArrayList;
import java.util.List;

public class IdentityAdapter extends ArrayAdapter {
    private static final String AADHAAR_CARD = "Aadhaar Card";
    private static final String DRIVING_LICENCE = "Driving Licence";
    private static final String PAN_CARD = "Pan Card";
    private static final String PASSPORT = "Passport";
    private static final String UAN_NUMBER = "UAN Number";
    public List<IdentityDataClass> selectedItems = new ArrayList<>();
    private Context context;

    public IdentityAdapter(Context context, List<IdentityDataClass> dataList){
        super(context, R.layout.identity_row,dataList);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        IdentityRowBinding binding;
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.identity_row, null);
            binding = DataBindingUtil.bind(convertView);
            convertView.setTag(binding);
        }else{
            binding = (IdentityRowBinding) convertView.getTag();
        }
        IdentityDataClass viewData = (IdentityDataClass)getItem(position);
        binding.setIdentityData(viewData);
        View view = binding.getRoot();
        if (selectedItems.contains(viewData)){
            binding.identityIcon.setBackground(context.getDrawable(R.drawable.check_mark));
        }else if (viewData.identityType.equals(AADHAAR_CARD)){
            binding.identityIcon.setBackground(context.getDrawable(R.drawable.ic_aadhaar_logo));
        }else if (viewData.identityType.equals(DRIVING_LICENCE)){
            binding.identityIcon.setBackground(context.getDrawable(R.drawable.ic_driver_license));
        }else if (viewData.identityType.equals(PAN_CARD)){
            binding.identityIcon.setBackground(context.getDrawable(R.drawable.pancard));
        }else if (viewData.identityType.equals(PASSPORT)){
            binding.identityIcon.setBackground(context.getDrawable(R.drawable.ic_passport));
        }else if (viewData.identityType.equals(UAN_NUMBER)){
            binding.identityIcon.setBackground(context.getDrawable(R.drawable.uan));
        }
        return view;
    }

    @Override
    public boolean areAllItemsEnabled()
    {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isEnabled(int arg0)
    {
        // TODO Auto-generated method stub
        return true;
    }
}
