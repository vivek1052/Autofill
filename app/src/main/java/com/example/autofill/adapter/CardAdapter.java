package com.example.autofill.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.databinding.DataBindingUtil;

import com.example.autofill.R;
import com.example.autofill.dataClass.CardDataClass;
import com.example.autofill.databinding.CardRowBinding;

import java.util.ArrayList;
import java.util.List;

public class CardAdapter extends ArrayAdapter {
    private Context context;
    public List<CardDataClass> selectedItems = new ArrayList<>();
    public CardAdapter(Context context, List<CardDataClass> cardData) {
        super(context,R.layout.card_row, cardData);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CardRowBinding binding;
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.card_row,null);
            binding = DataBindingUtil.bind(convertView);
            convertView.setTag(binding);
        }else{
            binding = (CardRowBinding) convertView.getTag();
        }
        CardDataClass viewData = (CardDataClass)getItem(position);

        if (selectedItems.contains(viewData)){
            binding.getRoot().setBackgroundColor(R.attr.colorPrimary);
        }else {
            binding.getRoot().setBackgroundColor(0x00000000);
        }
        binding.setCardData(viewData);
        return binding.getRoot();
    }
}
