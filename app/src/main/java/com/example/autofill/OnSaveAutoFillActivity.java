package com.example.autofill;

import androidx.appcompat.app.AppCompatActivity;

import android.app.assist.AssistStructure;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.autofill.FillContext;
import android.service.autofill.SaveRequest;
import android.text.TextUtils;
import android.view.View;
import android.view.autofill.AutofillId;

import com.example.autofill.dataClass.ParsedStructure;
import com.example.autofill.dataClass.PasswordDataClass;
import com.example.autofill.util.Authenticate;
import com.example.autofill.util.CipherClass;
import com.example.autofill.util.DataModel;
import com.example.autofill.util.NodeParser;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class OnSaveAutoFillActivity extends AppCompatActivity {
    DataModel dataModel;
    CipherClass cipherClass;
    String packageName;
    String appName;
    AutofillId usernameId,passwordId;
    AssistStructure.ViewNode usernameNode, passwordNode;
    NodeParser nodeParser = new NodeParser();
    PasswordDataClass newPassword = new PasswordDataClass(0,"","","","");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataModel = new DataModel(this);
        cipherClass = new CipherClass();
        Bundle saveReqBundle = getIntent().getBundleExtra("saveReqBundle");
        SaveRequest saveRequest = saveReqBundle.getParcelable("saveRequest");
        List<FillContext> fillContextList = saveRequest.getFillContexts();
        packageName = fillContextList.get(fillContextList.size()-1).getStructure()
        .getActivityComponent().getPackageName();
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(packageName,0);
            appName = getPackageManager().getApplicationLabel(ai).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Bundle clientState = saveRequest.getClientState();
        usernameId = clientState.getParcelable("userNameId");
        passwordId = clientState.getParcelable("passwordId");

        if (fillContextList.size()>1){
            if (usernameId!=null){
                usernameNode = nodeParser.TraverseStructure(fillContextList.get(0).getStructure(),usernameId);
            }
            if (passwordId!=null){
                passwordNode = nodeParser.TraverseStructure(fillContextList.get(1)
                        .getStructure(),passwordId);
            }
        }else {
            if (usernameId!=null){
                usernameNode = nodeParser.TraverseStructure(fillContextList.get(fillContextList.size()-1)
                        .getStructure(),usernameId);
            }
            if (passwordId!=null){
                passwordNode = nodeParser.TraverseStructure(fillContextList.get(fillContextList.size()-1)
                        .getStructure(),passwordId);
            }
        }

        if (!TextUtils.isEmpty(usernameNode.getText()) && !TextUtils.isEmpty(passwordNode.getText())){
            Authenticate authenticate = new Authenticate(this,R.string.encrypt);
            authenticate.setListener(new Authenticate.authCallBack() {
                @Override
                public void onAuthenticationSuccess(String maspass) {
                    try {
                        saveData(maspass);
                    } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException e) {
                        e.printStackTrace();
                        finishAndRemoveTask();
                    }
                }

                @Override
                public void onAuthenticationFailed() {
                    finishAndRemoveTask();
                }
            });
        }else {finishAndRemoveTask();}

    }

    private void saveData(String maspass) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {

            newPassword.serviceName = appName;
            newPassword.subText = packageName;
            newPassword.username = cipherClass.encrypt(usernameNode.getText().toString(),maspass);
            newPassword.password = cipherClass.encrypt(passwordNode.getText().toString(),maspass);
            dataModel.addNewPassword(newPassword);
            finishAndRemoveTask();
    }
}
