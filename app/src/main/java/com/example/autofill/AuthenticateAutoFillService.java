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
import com.example.autofill.util.CipherClass;
import com.example.autofill.util.DataModel;
import com.example.autofill.util.MasterPasswrordPrompt;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static android.view.autofill.AutofillManager.EXTRA_AUTHENTICATION_RESULT;

public class AuthenticateAutoFillService extends AppCompatActivity {
    AutofillId usernameNode, passwordNode;
    DataModel dataModel;
    CipherClass cipherClass;
    List<PasswordDataClass> validPassword = new ArrayList<>();
    String maspass;
    FillResponse.Builder fillResponseBuilder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_authenticate_auto_fill_service);
        Intent intent = getIntent();
        dataModel = new DataModel(this);
        cipherClass = new CipherClass();
        fillResponseBuilder = new FillResponse.Builder();
        Bundle bundle = intent.getBundleExtra("Data");
        String packageName = bundle.getString("packageName");
        ArrayList<ParsedStructure> passedNodes = bundle.getParcelableArrayList("passedNodes");

        for (ParsedStructure ps : passedNodes) {
            switch (ps.autofillhint) {
                case View.AUTOFILL_HINT_USERNAME:
                    usernameNode = ps.nodeId;
                    break;
                case View.AUTOFILL_HINT_PASSWORD:
                    passwordNode = ps.nodeId;
                    break;
            }
        }

        if (passwordNode!=null && usernameNode!=null){
            List<PasswordDataClass> passwordData = new ArrayList<>(dataModel.dbHelper.getAllPassword());
            for (PasswordDataClass pd:passwordData){
                if (pd.subText.equals(packageName)){
                    validPassword.add(pd);
                }
            }
            if (validPassword.size()>0){
                final MasterPasswrordPrompt mp = new MasterPasswrordPrompt(this,R.string.decrypt);
                mp.setOnclickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mp.getMasterPassword()==null){
                            return;
                        }
                        maspass = mp.getMasterPassword().getText().toString();
                        try {
                            fillResponsePassword();
                        } catch (IllegalBlockSizeException e) {
                            e.printStackTrace();
                        } catch (InvalidKeyException e) {
                            e.printStackTrace();
                        } catch (BadPaddingException e) {
                            e.printStackTrace();
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (NoSuchPaddingException e) {
                            e.printStackTrace();
                        }
                        mp.getAlertDialog().dismiss();
                    }
                });
            }else {
                fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(
                        SaveInfo.SAVE_DATA_TYPE_USERNAME | SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                        new AutofillId[] {usernameNode, passwordNode}).build());
                setFinalResult();
            }

        }

    }

    private void fillResponsePassword() throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {

        for (PasswordDataClass pd:validPassword){
            fillResponseBuilder.addDataset(new Dataset.Builder()
                    .setValue(usernameNode,
                            AutofillValue.forText(cipherClass.decrypt(pd.username,maspass)),CreatePresentation(pd.serviceName))
                    .setValue(passwordNode,
                            AutofillValue.forText(cipherClass.decrypt(pd.password,maspass)), CreatePresentation(pd.serviceName))
                    .build());
        }
        setFinalResult();
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
