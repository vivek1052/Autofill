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

import java.util.List;

public class PasswordAdapter extends ArrayAdapter {
    private List<PasswordDataClass> dataList;
    private Context context;

    public PasswordAdapter(Context context, List<PasswordDataClass> dataList){
        super(context, R.layout.password_row, dataList);
        this.dataList = dataList;
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
        binding.setPasswordData(dataList.get(position));
        View view = binding.getRoot();
        try {
            view.findViewById(R.id.appIcon).setBackground(context.getPackageManager().
                    getApplicationIcon(dataList.get(position).subText));
        } catch (PackageManager.NameNotFoundException e) {
            view.findViewById(R.id.appIcon).setBackground(context.getResources().getDrawable(R.drawable.password,
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
