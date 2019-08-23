package com.example.autofill.util;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.example.autofill.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public  class MasterPasswrordPrompt {
    private  AlertDialog alertDialog;
    public MasterPasswrordPrompt(Context context,int buttonText){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View promptView = layoutInflater.inflate(R.layout.masterdata_prompt,null);
        builder.setView(promptView);
        builder.setTitle(R.string.masterPasswordPrompt);
        builder.setPositiveButton(buttonText,null);
        alertDialog = builder.create();
        alertDialog.show();
    }
    public MasterPasswrordPrompt setOnclickListener(View.OnClickListener onClickListener){
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(onClickListener);
        return this;
    }
    public int getButtonId(){
        return alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).getId();
    }
    public AlertDialog getAlertDialog (){
        return alertDialog;
    }

    public EditText getMasterPassword(){
        EditText mp = alertDialog.findViewById(R.id.masterPassword);
        if (TextUtils.isEmpty(mp.getText().toString())){
            mp.setError("Cant be empty");
            return null;
        }
       return mp;
    }

}
