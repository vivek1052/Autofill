package com.example.autofill.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.example.autofill.dataClass.AddressDataClass;
import com.example.autofill.dataClass.CardDataClass;
import com.example.autofill.dataClass.InstalledApps;
import com.example.autofill.dataClass.PasswordDataClass;


import java.util.ArrayList;
import java.util.List;

public class DataModel {

    public  List<CustomEvents> listeners = new ArrayList<CustomEvents>();
    public  List<PasswordDataClass> passwordData = new ArrayList<PasswordDataClass>();
    public  List<CardDataClass> cardData = new ArrayList<CardDataClass>();
    public  List<AddressDataClass> addressData = new ArrayList<AddressDataClass>();
    public  List<InstalledApps> installedApps = new ArrayList<>();

    private Context context;
    private DriveDataModel driveDataModel;
    private DBHelper dbHelper;

   public DataModel(Context context){
       this.context = context;
       driveDataModel = new DriveDataModel(context);
       dbHelper = new DBHelper(context);
    }

    public void retrieveAllData(){
       //Get Local cached data
       passwordData = dbHelper.getAllPassword();
       cardData = dbHelper.getAllCards();
       installedApps = getInstalledApps();

    }

    public void addNewPassword(PasswordDataClass newPassword){
       dbHelper.insertPassword(newPassword.serviceName, newPassword.subText, newPassword.username,
                newPassword.password);
    }

    public void addNewCard(CardDataClass cardData){
       dbHelper.insertCard(cardData);
    }

    public void triggerPasswordDataUpdated(){
        passwordData = dbHelper.getAllPassword();
        for (int i=0;i<listeners.size();i++){
            listeners.get(i).passwordDataUpdated(passwordData);
        }
    }

    public void triggerCardDataUpdated(){
       cardData = dbHelper.getAllCards();
        for (int i=0;i<listeners.size();i++){
            listeners.get(i).cardDetailUpdated(cardData);
        }
    }

    public void  addEventLister(CustomEvents listner){
       listeners.add(listner);
    }

    public void deletePasswords(List<PasswordDataClass> toBeDeleted){
       for (int i=0; i<toBeDeleted.size();i++){
           dbHelper.deletePassword(toBeDeleted.get(i).id);
       }
    }

    public List<InstalledApps> getInstalledApps(){
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> l = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<InstalledApps> list = new ArrayList<>();
        for (ApplicationInfo ai : l){
            try {
                list.add(new InstalledApps(pm.getApplicationLabel(ai).toString(),
                        ai.packageName,pm.getApplicationIcon(ai.packageName)));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}
