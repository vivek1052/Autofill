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
import com.example.autofill.dataClass.AddressDataClass;
import com.example.autofill.databinding.AddressRowBinding;


import java.util.List;

public class AddressAdapter extends ArrayAdapter {

    private List<AddressDataClass> dataList;
    private Context context;

    public AddressAdapter(Context context, List<AddressDataClass> dataList) {
        super(context, R.layout.address_row, dataList);
        this.dataList = dataList;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        AddressRowBinding binding;
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.address_row, null);
            binding = DataBindingUtil.bind(convertView);
            convertView.setTag(binding);
        }else{
            binding = (AddressRowBinding) convertView.getTag();
        }
        binding.setAddressData(dataList.get(position));
        return binding.getRoot();
    }
}
