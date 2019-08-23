package com.example.autofill.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.autofill.R;
import com.example.autofill.dataClass.InstalledApps;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

public class ServiceSuggestAdapter extends ArrayAdapter {
    private Context context;
    private List<InstalledApps> installedAppsAll;
    public ServiceSuggestAdapter(Context context, List<InstalledApps> installedApps) {
        super(context, R.layout.service_suggestion_row, installedApps);
        this.context = context;
        this.installedAppsAll = new ArrayList<>(installedApps);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        //we need to get the view of the xml for our list item
        //And for this we need a layout inflater
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.service_suggestion_row, null);
        }
        InstalledApps installedApp = (InstalledApps) getItem(position);
        if (installedApp != null){
            convertView.findViewById(R.id.icon_service_suggest).setBackground(installedApp.icon);
            MaterialTextView text = convertView.findViewById(R.id.text_service_suggest);
            text.setText(installedApp.label);
        }

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return serviceFilter;
    }

    private Filter serviceFilter = new Filter() {
        FilterResults results = new FilterResults();
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<InstalledApps> suggestions = new ArrayList<>();

            if (charSequence == null || charSequence.length() == 0){
                suggestions.addAll(installedAppsAll);
            }else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for (InstalledApps ia : installedAppsAll){
                    if (ia.label.toLowerCase().trim().contains(filterPattern)){
                        suggestions.add(ia);
                    }
                }
            }
            results.values = suggestions;
            results.count = suggestions.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            clear();
            addAll((List) filterResults.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((InstalledApps)resultValue).label;
        }
    };
}
