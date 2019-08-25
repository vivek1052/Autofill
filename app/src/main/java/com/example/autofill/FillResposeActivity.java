package com.example.autofill;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.Dataset;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveInfo;
import android.view.View;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.widget.RemoteViews;

import com.example.autofill.dataClass.ParsedStructure;
import com.example.autofill.dataClass.PasswordDataClass;
import com.example.autofill.util.Authenticate;
import com.example.autofill.util.CipherClass;
import com.example.autofill.util.DataModel;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static android.view.autofill.AutofillManager.EXTRA_AUTHENTICATION_RESULT;

public class FillResposeActivity extends AppCompatActivity {

    AutofillId usernameNode, passwordNode, phoneNode;
    DataModel dataModel;
    CipherClass cipherClass;
    FillResponse.Builder fillResponseBuilder;
    String packageName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        dataModel = new DataModel(this);
        cipherClass = new CipherClass();
        fillResponseBuilder = new FillResponse.Builder();
        Bundle bundle = intent.getBundleExtra("Data");
        packageName = bundle.getString("packageName");
        ArrayList<ParsedStructure> passedNodes = bundle.getParcelableArrayList("passedNodes");

        for (ParsedStructure ps : passedNodes) {
            switch (ps.autofillhint) {
                case View.AUTOFILL_HINT_USERNAME:
                    usernameNode = ps.nodeId;
                    break;
                case View.AUTOFILL_HINT_PASSWORD:
                    passwordNode = ps.nodeId;
                    break;
                case View.AUTOFILL_HINT_PHONE:
                    phoneNode = ps.nodeId;
            }
        }

        if (passwordNode!=null || usernameNode!=null || phoneNode!=null){
            Authenticate authenticate = new Authenticate(this,R.string.decrypt);
            authenticate.setListener(new Authenticate.authCallBack() {
                @Override
                public void onAuthenticationSuccess(String maspass) {
                    try {
                        fillResponse(maspass);
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

    private void fillResponse(String maspass) throws InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException {
        if (usernameNode!=null || passwordNode!=null){
            fillResponsePassword(maspass);
        }
    }

    private void fillResponsePassword(String maspass) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {

        List<PasswordDataClass> passwordData = new ArrayList<>(dataModel.dbHelper.getAllPassword());
        List<PasswordDataClass> validPassword = new ArrayList<>();
        for (PasswordDataClass pd:passwordData){
            if (pd.subText.equals(packageName)){
                validPassword.add(pd);
            }
        }

        if (usernameNode!=null && passwordNode!=null){
            if (validPassword.size()>0){
                for (PasswordDataClass pd:validPassword){
                    fillResponseBuilder.addDataset(new Dataset.Builder()
                            .setValue(usernameNode,
                                    AutofillValue.forText(cipherClass.decrypt(pd.username,maspass)),CreatePresentation(pd.serviceName))
                            .setValue(passwordNode,
                                    AutofillValue.forText(cipherClass.decrypt(pd.password,maspass)), CreatePresentation(pd.serviceName))
                            .build());
                }
                setFinalResult();
            }else {
                fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(
                        SaveInfo.SAVE_DATA_TYPE_USERNAME | SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                        new AutofillId[] {usernameNode, passwordNode}).build());
                setFinalResult();
            }
        }else if (passwordNode!=null){
            if (validPassword.size()>0){
                for (PasswordDataClass pd:validPassword){
                    fillResponseBuilder.addDataset(new Dataset.Builder()
                            .setValue(passwordNode,
                                    AutofillValue.forText(cipherClass.decrypt(pd.password,maspass)), CreatePresentation(pd.serviceName))
                            .build());
                }
                setFinalResult();
            }else {
                fillResponseBuilder.setSaveInfo(new SaveInfo.Builder( SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                        new AutofillId[] {passwordNode}).build());
                setFinalResult();
            }
        }else if (usernameNode!=null){
            if (validPassword.size()>0){
                for (PasswordDataClass pd:validPassword){
                    fillResponseBuilder.addDataset(new Dataset.Builder()
                            .setValue(usernameNode,
                                    AutofillValue.forText(cipherClass.decrypt(pd.username,maspass)),CreatePresentation(pd.serviceName))
                            .build());
                }
                setFinalResult();
            }else {
                fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(
                        SaveInfo.SAVE_DATA_TYPE_USERNAME,
                        new AutofillId[] {usernameNode}).build());
                setFinalResult();
            }
        }
    }

    private void setFinalResult() {
        FillResponse fillResponse = fillResponseBuilder.build();
        Intent replyIntent = new Intent();
        replyIntent.putExtra(EXTRA_AUTHENTICATION_RESULT, fillResponse);
        setResult(RESULT_OK, replyIntent);
        finish();
    }


    public RemoteViews CreatePresentation(String displayText) {
        RemoteViews presentation = new RemoteViews(getPackageName(), android.R.layout.simple_list_item_1);
        presentation.setTextViewText(android.R.id.text1, displayText);
        return presentation;
    }

}
