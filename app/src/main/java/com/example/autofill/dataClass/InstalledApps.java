package com.example.autofill.dataClass;

import android.graphics.drawable.Drawable;

public class InstalledApps {
    public String label;
    public Drawable icon;
    public String packageName;
    public InstalledApps(String label, String packageName, Drawable icon){
        this.label = label;
        this.packageName = packageName;
        this.icon = icon;
    }
}
