package com.example.autofill.adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.example.autofill.R;
import com.example.autofill.dataClass.PasswordDataClass;
import com.example.autofill.databinding.PasswordRowBinding;

import java.util.ArrayList;
import java.util.List;

public class PasswordAdapter extends ArrayAdapter {
    private Context context;
    public List<PasswordDataClass> selectedItems = new ArrayList<>();

    public PasswordAdapter(Context context, List<PasswordDataClass> dataList){
        super(context, R.layout.password_row, dataList);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        //we need to get the view of the xml for our list item
        //And for this we need a layout inflater
        PasswordRowBinding binding;
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.password_row, null);
            binding = DataBindingUtil.bind(convertView);
            convertView.setTag(binding);
        }else{
            binding = (PasswordRowBinding) convertView.getTag();
        }

        PasswordDataClass viewData = (PasswordDataClass)getItem(position);
        binding.setPasswordData(viewData);
        View view = binding.getRoot();
        if (selectedItems.contains(viewData)){
            binding.appIcon.setBackground(context.getDrawable(R.drawable.check_mark));
        }else {
            try {
                binding.appIcon.setBackground(context.getPackageManager().
                        getApplicationIcon(viewData.subText));
            } catch (PackageManager.NameNotFoundException e) {
                binding.appIcon.setBackground(context.getResources().getDrawable(R.drawable.password,
                        context.getTheme()));
            }
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
