package com.example.autofill;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.autofill.dataClass.PasswordDataClass;
import com.example.autofill.util.CipherClass;
import com.example.autofill.util.Contract;
import com.example.autofill.util.DataModel;
import com.example.autofill.util.MasterPasswrordPrompt;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class OnSaveAutoFillActivity extends AppCompatActivity {
    DataModel dataModel;
    CipherClass cipherClass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_on_save_auto_fill);
        dataModel = new DataModel(this);
        cipherClass = new CipherClass();
        final Bundle data = getIntent().getBundleExtra("Data");
        final PasswordDataClass passwordData = (PasswordDataClass) data.getSerializable("passwordData");

        if (passwordData!=null){
            final MasterPasswrordPrompt mp = new MasterPasswrordPrompt(this,R.string.encrypt);
            mp.setOnclickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mp.getMasterPassword()==null){
                        return;
                    }
                    String maspass = mp.getMasterPassword().getText().toString();
                    try {
                        passwordData.username = cipherClass.encrypt(passwordData.username,maspass);
                        passwordData.password = cipherClass.encrypt(passwordData.password,maspass);
                        dataModel.addNewPassword(passwordData);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    }
                    mp.getAlertDialog().dismiss();
                    finish();
                }
            });
        }else {
            finish();
        }
    }
}
