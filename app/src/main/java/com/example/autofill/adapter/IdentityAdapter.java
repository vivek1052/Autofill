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

import java.util.List;

public class IdentityAdapter extends ArrayAdapter {
    private static final String AADHAAR_CARD = "Aadhaar Card";
    private static final String DRIVING_LICENCE = "Driving Licence";
    private static final String PAN_CARD = "Pan Card";
    private static final String PASSPORT = "Passport";
    private static final String UAN_NUMBER = "UAN Number";
    private List<IdentityDataClass> identityData;
    private Context context;

    public IdentityAdapter(Context context, List<IdentityDataClass> dataList){
        super(context, R.layout.identity_row,dataList);
        this.identityData = dataList;
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
        binding.setIdentityData(identityData.get(position));
        View view = binding.getRoot();
        if (identityData.get(position).identityType.equals(AADHAAR_CARD)){
            binding.identityIcon.setBackground(context.getResources().getDrawable(R.drawable.ic_aadhaar_logo,
                    context.getTheme()));
        }else if (identityData.get(position).identityType.equals(DRIVING_LICENCE)){
            binding.identityIcon.setBackground(context.getResources().getDrawable(R.drawable.ic_driver_license,
                    context.getTheme()));
        }else if (identityData.get(position).identityType.equals(PAN_CARD)){
            binding.identityIcon.setBackground(context.getResources().getDrawable(R.drawable.pancard,
                    context.getTheme()));
        }else if (identityData.get(position).identityType.equals(PASSPORT)){
            binding.identityIcon.setBackground(context.getResources().getDrawable(R.drawable.ic_passport,
                    context.getTheme()));
        }else if (identityData.get(position).identityType.equals(UAN_NUMBER)){
            binding.identityIcon.setBackground(context.getResources().getDrawable(R.drawable.uan,
                    context.getTheme()));
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
