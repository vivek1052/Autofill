package com.example.autofill.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.example.autofill.dataClass.AddressDataClass;
import com.example.autofill.dataClass.CardDataClass;
import com.example.autofill.dataClass.InstalledApps;
import com.example.autofill.dataClass.PasswordDataClass;
import com.example.autofill.dataClass.IdentityDataClass;


import java.util.ArrayList;
import java.util.List;

public class DataModel {

    public  List<DataUpdateCallback> listeners = new ArrayList<DataUpdateCallback>();
    public  List<PasswordDataClass> passwordData = new ArrayList<PasswordDataClass>();
    public  List<CardDataClass> cardData = new ArrayList<CardDataClass>();
    public  List<AddressDataClass> addressData = new ArrayList<AddressDataClass>();
    public  List<InstalledApps> installedApps = new ArrayList<>();
    public List<IdentityDataClass> identityData = new ArrayList<>();

    private Context context;
    private DriveDataModel driveDataModel;
    public DBHelper dbHelper;

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
       addressData = dbHelper.getAllAddress();
       identityData = dbHelper.getAllIdentity();
    }

    public void addNewPassword(PasswordDataClass newPassword){
       List<PasswordDataClass> existingPass =  dbHelper.getPassword(newPassword.subText);
       if (existingPass.size()>0){
           Boolean passUpdated = false;
           for (PasswordDataClass ep:existingPass){
                if (ep.username.equals(newPassword.username)){
                    dbHelper.updatePassword(ep.id,newPassword.serviceName,newPassword.subText,
                            newPassword.username,newPassword.password);
                    passUpdated = true;
                    break;
                }
           }
           if (!passUpdated){
               dbHelper.insertPassword(newPassword.serviceName, newPassword.subText, newPassword.username,
                       newPassword.password);
           }
       }else {
           dbHelper.insertPassword(newPassword.serviceName, newPassword.subText, newPassword.username,
                   newPassword.password);
       }
    }

    public void addNewAddress(AddressDataClass newAddress){
       dbHelper.insertAddress(newAddress.name,newAddress.flatNo,newAddress.buildingName,newAddress.streetNo,
               newAddress.streetName,newAddress.locality,newAddress.city,newAddress.state,newAddress.postalCode,
               newAddress.country,newAddress.phoneNo);
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

    public void  addEventLister(DataUpdateCallback listner){
       listeners.add(listner);
    }

    public void deletePasswords(List<PasswordDataClass> toBeDeleted){
       for (int i=0; i<toBeDeleted.size();i++){
           dbHelper.deletePassword(toBeDeleted.get(i).id);
       }
    }

    public void deleteCards(List<CardDataClass> toBeDeleted){
        for (int i=0; i<toBeDeleted.size();i++){
            dbHelper.deleteCard(toBeDeleted.get(i).id);
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

    public void triggerAddressDataUpdated() {
        addressData = dbHelper.getAllAddress();
        for (int i=0;i<listeners.size();i++){
            listeners.get(i).AddressDataUpdated(addressData);
        }
    }

    public void addNewIdentity(IdentityDataClass newIdentityData) {
       dbHelper.insertIdentity(newIdentityData.identityType,newIdentityData.identityNumber);
    }

    public void triggerIdentityDataUpdated() {
       identityData = dbHelper.getAllIdentity();
        for (int i=0;i<listeners.size();i++){
            listeners.get(i).IdentityDataUpdated(identityData);
        }
    }
}
