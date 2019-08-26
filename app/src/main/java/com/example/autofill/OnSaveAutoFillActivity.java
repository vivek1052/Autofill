package com.example.autofill;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.autofill.AutofillId;

import com.example.autofill.dataClass.ParsedStructure;
import com.example.autofill.dataClass.PasswordDataClass;
import com.example.autofill.util.Authenticate;
import com.example.autofill.util.CipherClass;
import com.example.autofill.util.Contract;
import com.example.autofill.util.DataModel;
import com.example.autofill.util.MasterPasswrordPrompt;

import org.w3c.dom.Text;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class OnSaveAutoFillActivity extends AppCompatActivity {
    DataModel dataModel;
    CipherClass cipherClass;
    String packageName;
    String appName;
    String password, phoneNo, usename;
    PasswordDataClass newPassword = new PasswordDataClass(0,"","","","");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataModel = new DataModel(this);
        cipherClass = new CipherClass();
        final Bundle bundle = getIntent().getBundleExtra("Data");
        packageName = bundle.getString("packageName");
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(packageName,0);
            appName = getPackageManager().getApplicationLabel(ai).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ArrayList<ParsedStructure> passedNodes = bundle.getParcelableArrayList("passedNodes");

        for (ParsedStructure ps : passedNodes) {
            switch (ps.autofillhint) {
                case View.AUTOFILL_HINT_USERNAME:
                    usename = ps.text;
                    break;
                case View.AUTOFILL_HINT_PASSWORD:
                    password = ps.text;
                    break;
                case View.AUTOFILL_HINT_PHONE:
                    phoneNo = ps.text;
            }
        }

        if (!TextUtils.isEmpty(usename) || !TextUtils.isEmpty(password) || !TextUtils.isEmpty(phoneNo)){
            Authenticate authenticate = new Authenticate(this,R.string.encrypt);
            authenticate.setListener(new Authenticate.authCallBack() {
                @Override
                public void onAuthenticationSuccess(String maspass) {
                    try {
                        saveData(maspass);
                    } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException e) {
                        e.printStackTrace();
                        finish();
                    }
                }

                @Override
                public void onAuthenticationFailed() {
                    finish();
                }
            });
        }else {finish();}

    }

    private void saveData(String maspass) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        if (!TextUtils.isEmpty(usename) || !TextUtils.isEmpty(password)){
            newPassword.serviceName = appName;
            newPassword.subText = packageName;
            newPassword.username = cipherClass.encrypt(usename,maspass);
            newPassword.password = cipherClass.encrypt(password,maspass);
            dataModel.addNewPassword(newPassword);
        }
        finish();
    }
}
