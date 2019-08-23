package com.example.autofill.util;

import android.content.Context;

import com.example.autofill.dataClass.PasswordDataClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class CacheFileHelper {
    private Context context;
    static public String passwordFile = "PasswordFile.ser";
    static public String cardFile = "cardFile.ser";
    static public String addressFile = "addressFile.ser";

   public CacheFileHelper(Context context){
        this.context = context;
    }

    public Object getCachedData() throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput(passwordFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object object = ois.readObject();
        return object;
    }

    public void createCachedPasswords(List<PasswordDataClass> passwordDatas) throws IOException {
           FileOutputStream fos = context.openFileOutput(passwordFile, Context.MODE_PRIVATE);
           ObjectOutputStream oos = new ObjectOutputStream(fos);
           oos.writeObject(passwordDatas);
           oos.close ();
           fos.close ();
    }
    public boolean isFileExist(String path){
       return new File(context.getFilesDir()+"/"+path).exists();
    }
}
