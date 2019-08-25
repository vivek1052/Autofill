package com.example.autofill.util;

import android.content.Context;

import com.example.autofill.dataClass.MasterPasswordEncrypted;
import com.example.autofill.dataClass.PasswordDataClass;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.List;

public class CacheFileHelper {
    private Context context;
    static public String passwordFile = "PasswordFile.ser";


   public CacheFileHelper(Context context){
        this.context = context;
    }

    public Object getCachedPassword() throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput(passwordFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        return ois.readObject();
    }

    public void createCachedPassword(MasterPasswordEncrypted mpe) throws IOException {
           FileOutputStream fos = context.openFileOutput(passwordFile, Context.MODE_PRIVATE);
           ObjectOutputStream oos = new ObjectOutputStream(fos);
           oos.writeObject(mpe);
           oos.close ();
           fos.close ();
    }
    public boolean isFileExist(String path){
       return new File(context.getFilesDir()+"/"+path).exists();
    }
}
