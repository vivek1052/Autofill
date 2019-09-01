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

import com.example.autofill.dataClass.CardDataClass;
import com.example.autofill.dataClass.ParsedStructure;
import com.example.autofill.dataClass.PasswordDataClass;
import com.example.autofill.util.Authenticate;
import com.example.autofill.util.CipherClass;
import com.example.autofill.util.DataModel;
import com.example.autofill.util.GenericStringBase;
import com.google.common.net.InternetDomainName;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static android.view.autofill.AutofillManager.EXTRA_AUTHENTICATION_RESULT;

public class FillResposeActivity extends AppCompatActivity {

    AutofillId usernameNode, passwordNode, phoneNode, emailNode, cardNoNode, expiryMonthNode, expiryYearNode, nameNode, cvvNode;
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
        if (CompareStringBase(packageName, GenericStringBase.browser)){
            try {
                URI uri = new URI(passedNodes.get(0).url);
                packageName = InternetDomainName.from(uri.getHost()).topPrivateDomain()
                        .toString().split(Pattern.quote("."))[0];
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        for (ParsedStructure ps : passedNodes) {
            switch (ps.autofillhint) {
                case View.AUTOFILL_HINT_USERNAME:
                    usernameNode = ps.nodeId;
                    break;
                case View.AUTOFILL_HINT_EMAIL_ADDRESS:
                    emailNode = ps.nodeId;
                    break;
                case View.AUTOFILL_HINT_PASSWORD:
                    passwordNode = ps.nodeId;
                    break;
                case View.AUTOFILL_HINT_PHONE:
                    phoneNode = ps.nodeId;
                    break;
                case View.AUTOFILL_HINT_CREDIT_CARD_NUMBER:
                    cardNoNode = ps.nodeId;
                    break;
                case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH:
                    expiryMonthNode = ps.nodeId;
                    break;
                case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR:
                    expiryYearNode = ps.nodeId;
                    break;
                case View.AUTOFILL_HINT_NAME:
                    nameNode = ps.nodeId;
                    break;
                case View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE:
                    cvvNode = ps.nodeId;
                    break;
            }
        }

        if (passwordNode != null || usernameNode != null || phoneNode != null || emailNode != null
        || cardNoNode != null || nameNode != null || cvvNode != null) {
            Authenticate authenticate = new Authenticate(this, R.string.decrypt);
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
        } else {
            finish();
        }
    }

    private void fillResponse(String maspass) throws InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException {
        if (usernameNode != null || passwordNode != null) {
            fillResponsePassword(maspass);
        }else if (cardNoNode != null || nameNode != null || cvvNode != null){
            fillResponseCard(maspass);
        }
    }

    private void fillResponseCard(String maspass) {
        List<CardDataClass> cardData = new ArrayList<>(dataModel.dbHelper.getAllCards());

        for (CardDataClass cD : cardData){
            try {
                cD.cardNo1 = cipherClass.decrypt(cD.cardNo1,maspass);
                cD.cardNo2 = cipherClass.decrypt(cD.cardNo2,maspass);
                cD.cardNo3 = cipherClass.decrypt(cD.cardNo3,maspass);
                cD.cardNo4 = cipherClass.decrypt(cD.cardNo4,maspass);
                cD.month = cipherClass.decrypt(cD.month,maspass);
                cD.year = cipherClass.decrypt(cD.year,maspass);
                cD.cvv = cipherClass.decrypt(cD.cvv,maspass);
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

            Dataset.Builder dataSetBuilder = new Dataset.Builder();
            if (cardNoNode != null){
                dataSetBuilder.setValue(cardNoNode,
                        AutofillValue.forText(cD.cardNo1+ cD.cardNo2+cD.cardNo3+cD.cardNo4),
                        CreatePresentation(cD.bankName,cD.cardType));
            }
            if (expiryMonthNode != null && expiryYearNode == null){
                dataSetBuilder.setValue(expiryMonthNode,
                        AutofillValue.forText(cD.month+ cD.year),CreatePresentation(cD.bankName, cD.cardType));
            }else if (expiryMonthNode == null && expiryYearNode != null){
                dataSetBuilder.setValue(expiryYearNode,
                        AutofillValue.forText(cD.month+ cD.year),CreatePresentation(cD.bankName, cD.cardType));
            }else {
                if (expiryMonthNode != null){
                    dataSetBuilder.setValue(expiryMonthNode,
                            AutofillValue.forText(cD.month),CreatePresentation(cD.bankName, cD.cardType));
                }
                if (expiryYearNode!=null){
                    dataSetBuilder.setValue(expiryYearNode,
                            AutofillValue.forText(cD.year),CreatePresentation(cD.bankName, cD.cardType));
                }
            }
            if (nameNode!=null){
                dataSetBuilder.setValue(nameNode,
                        AutofillValue.forText(cD.name),CreatePresentation(cD.bankName, cD.cardType));
            }
            if (cvvNode!=null){
                dataSetBuilder.setValue(cvvNode,
                        AutofillValue.forText(cD.cvv),CreatePresentation(cD.bankName, cD.cardType));
            }
            fillResponseBuilder.addDataset(dataSetBuilder.build());
        }
        setFinalResult();
    }

    private void fillResponsePassword(String maspass) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {

        if (usernameNode == null && emailNode !=null){
            usernameNode = emailNode;
        }else if (usernameNode == null && phoneNode != null){
            usernameNode = phoneNode;
        }
        List<PasswordDataClass> passwordData = new ArrayList<>(dataModel.dbHelper.getAllPassword());
        List<PasswordDataClass> validPassword = new ArrayList<>();
        for (PasswordDataClass pd : passwordData) {
            if (pd.subText.contains(packageName)) {
                validPassword.add(pd);
            }
        }

        if (usernameNode != null && passwordNode != null) {
            if (validPassword.size() > 0) {
                for (PasswordDataClass pd : validPassword) {
                    pd.username = cipherClass.decrypt(pd.username, maspass);
                    pd.password = cipherClass.decrypt(pd.password, maspass);
                    fillResponseBuilder.addDataset(new Dataset.Builder()
                            .setValue(usernameNode,
                                    AutofillValue.forText(pd.username), CreatePresentation(pd.serviceName, pd.username))
                            .setValue(passwordNode,
                                    AutofillValue.forText(pd.password), CreatePresentation(pd.serviceName, pd.username))
                            .build());
                }
                setFinalResult();
            } else {
                fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(
                        SaveInfo.SAVE_DATA_TYPE_USERNAME | SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                        new AutofillId[]{usernameNode, passwordNode}).build());
                setFinalResult();
            }
        } else if (passwordNode != null) {
            if (validPassword.size() > 0) {
                for (PasswordDataClass pd : validPassword) {
                    pd.username = cipherClass.decrypt(pd.username, maspass);
                    pd.password = cipherClass.decrypt(pd.password, maspass);
                    fillResponseBuilder.addDataset(new Dataset.Builder()
                            .setValue(passwordNode,
                                    AutofillValue.forText(pd.password), CreatePresentation(pd.serviceName, pd.username))
                            .build());
                }
                setFinalResult();
            } else {
                fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                        new AutofillId[]{passwordNode}).build());
                setFinalResult();
            }
        } else if (usernameNode != null) {
            if (validPassword.size() > 0) {
                for (PasswordDataClass pd : validPassword) {
                    pd.username = cipherClass.decrypt(pd.username, maspass);
                    pd.password = cipherClass.decrypt(pd.password, maspass);
                    fillResponseBuilder.addDataset(new Dataset.Builder()
                            .setValue(usernameNode,
                                    AutofillValue.forText(pd.username), CreatePresentation(pd.serviceName, pd.username))
                            .build());
                }
                setFinalResult();
            } else {
                fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(
                        SaveInfo.SAVE_DATA_TYPE_USERNAME,
                        new AutofillId[]{usernameNode}).build());
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


    public RemoteViews CreatePresentation(String displayText, String subText) {
        RemoteViews presentation = new RemoteViews(getPackageName(), R.layout.simple_list_2_item);
        presentation.setTextViewText(R.id.text1, displayText);
        presentation.setTextViewText(R.id.text2, subText);
        return presentation;
    }

    public boolean CompareStringBase(String source, String[] target) {
        if (source == null) {
            return false;
        }
        for (int i = 0; i < target.length; i++) {
            if (source.toLowerCase().trim().contains(target[i].toLowerCase().trim())) {
                return true;
            }
        }
        return false;
    }

}
