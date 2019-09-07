package com.example.autofill;

import android.app.assist.AssistStructure;
import android.app.assist.AssistStructure.ViewNode;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.autofill.Dataset;
import android.service.autofill.FillContext;
import android.service.autofill.FillEventHistory;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveInfo;
import android.view.View;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillManager;
import android.view.autofill.AutofillValue;
import android.widget.RemoteViews;

import androidx.appcompat.app.AppCompatActivity;

import com.example.autofill.dataClass.CardDataClass;
import com.example.autofill.dataClass.ParsedStructure;
import com.example.autofill.dataClass.PasswordDataClass;
import com.example.autofill.util.Authenticate;
import com.example.autofill.util.CipherClass;
import com.example.autofill.util.DataModel;
import com.example.autofill.util.GenericStringBase;
import com.example.autofill.util.NodeParser;
import com.google.common.net.InternetDomainName;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static android.view.autofill.AutofillManager.EXTRA_AUTHENTICATION_RESULT;

public class FillResposeActivity extends AppCompatActivity {

    AutofillId usernameNodeId, passwordNodeId, phoneNodeId, emailNodeId, cardNoNodeId, expiryMonNodeID, expiryYearNodeId, nameNodeId, cvvNodeId;
    ViewNode usernameNode, passwordNode, phoneNode, emailNode, cardNoNode, expiryMonthNode, expiryYearNode, nameNode, cvvNode;
    String packageName;
    DataModel dataModel;
    CipherClass cipherClass;
    FillRequest fillRequest;
    FillResponse.Builder fillResponseBuilder;
    NodeParser nodeParser = new NodeParser();
    Boolean isBrowser = false;
    private static final String LOGIN_FORM = "LOGIN_FORM" ;
    private static final String CARD_FORM = "CARD_FORM" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        dataModel = new DataModel(this);
        cipherClass = new CipherClass();
        fillResponseBuilder = new FillResponse.Builder();

        Bundle fillReqBundle = intent.getBundleExtra("fillReqBundle");
        fillRequest = fillReqBundle.getParcelable("FillRequest");
        String formType = fillReqBundle.getString("FormType");
        ArrayList<ParsedStructure>  passedNodes = fillReqBundle.getParcelableArrayList("PassedNodes");
        List<FillContext> fillContexts = fillRequest.getFillContexts();
        AssistStructure assistStructure = fillContexts.get(fillContexts.size()-1).getStructure();
        packageName = assistStructure.getActivityComponent().getPackageName();

        for (int i = 0; i < passedNodes.size(); i++) {
            ParsedStructure ps = passedNodes.get(i);
            switch (ps.autofillhint) {
                case View.AUTOFILL_HINT_USERNAME:
                    usernameNodeId = ps.nodeId;
                    break;
                case View.AUTOFILL_HINT_EMAIL_ADDRESS:
                    emailNodeId = ps.nodeId;
                    break;
                case View.AUTOFILL_HINT_PASSWORD:
                    passwordNodeId = ps.nodeId;
                    passwordNode = nodeParser.TraverseStructure(assistStructure,ps.nodeId);
                    break;
                case View.AUTOFILL_HINT_PHONE:
                    phoneNodeId = ps.nodeId;
                    phoneNode = nodeParser.TraverseStructure(assistStructure,ps.nodeId);
                    break;
                case View.AUTOFILL_HINT_CREDIT_CARD_NUMBER:
                    cardNoNodeId = ps.nodeId;
                    cardNoNode = nodeParser.TraverseStructure(assistStructure,ps.nodeId);
                    break;
                case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH:
                    expiryMonNodeID = ps.nodeId;
                    expiryMonthNode = nodeParser.TraverseStructure(assistStructure,ps.nodeId);
                    break;
                case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR:
                    expiryYearNodeId = ps.nodeId;
                    expiryYearNode = nodeParser.TraverseStructure(assistStructure,ps.nodeId);
                    break;
                case View.AUTOFILL_HINT_NAME:
                    nameNodeId = ps.nodeId;
                    nameNode = nodeParser.TraverseStructure(assistStructure,ps.nodeId);
                    break;
                case View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE:
                    cvvNodeId = ps.nodeId;
                    cvvNode = nodeParser.TraverseStructure(assistStructure,ps.nodeId);
                    break;
                case "UNKNOWN":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        fillResponseBuilder.setFieldClassificationIds(ps.nodeId);
                    }
            }

        }

        if (CompareStringBase(packageName, GenericStringBase.browser)) {
            try {
                if (Build.VERSION.SDK_INT<Build.VERSION_CODES.P){
                    isBrowser = false;
                    return;
                }
                isBrowser = true;
                ViewNode random = nodeParser.TraverseStructure(assistStructure,passedNodes.get(0).nodeId);
                String webUrl = random.getWebScheme()+"://"+random.getWebDomain();
                URI uri = new URI(webUrl);
                packageName = InternetDomainName.from(uri.getHost()).topPrivateDomain()
                        .toString().split(Pattern.quote("."))[0];
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        if (formType.equals(LOGIN_FORM)){
            fillResponsePassword();
        }else if (formType.equals(CARD_FORM)){
            fillResponseCard();
        }else {
            finishAndRemoveTask();
        }

    }


    private void fillResponseCard() {
        final List<CardDataClass> cardData = new ArrayList<>(dataModel.dbHelper.getAllCards());

        if (cardData.size() > 0) {
            final Authenticate authenticate = new Authenticate(this, R.string.decrypt);
            authenticate.setListener(new Authenticate.authCallBack() {
                @Override
                public void onAuthenticationSuccess(String maspass) {
                    for (CardDataClass cD : cardData) {
                        try {
                            cD.cardNo1 = cipherClass.decrypt(cD.cardNo1, maspass);
                            cD.cardNo2 = cipherClass.decrypt(cD.cardNo2, maspass);
                            cD.cardNo3 = cipherClass.decrypt(cD.cardNo3, maspass);
                            cD.cardNo4 = cipherClass.decrypt(cD.cardNo4, maspass);
                            cD.month = cipherClass.decrypt(cD.month, maspass);
                            cD.year = cipherClass.decrypt(cD.year, maspass);
                            cD.cvv = cipherClass.decrypt(cD.cvv, maspass);
                        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                            e.printStackTrace();
                            finishAndRemoveTask();
                        }

                        Dataset.Builder dataSetBuilder = new Dataset.Builder();
                        if (cardNoNodeId != null) {
                            dataSetBuilder.setValue(cardNoNodeId,
                                    AutofillValue.forText(cD.cardNo1 + cD.cardNo2 + cD.cardNo3 + cD.cardNo4),
                                    CreatePresentation(cD.bankName, cD.cardType));
                        }
                        if (expiryMonNodeID != null && expiryYearNodeId == null) {
                            dataSetBuilder.setValue(expiryMonNodeID,
                                    AutofillValue.forText(cD.month + "/" + cD.year), CreatePresentation(cD.bankName, cD.cardType));
                        } else if (expiryMonNodeID == null && expiryYearNodeId != null) {
                            dataSetBuilder.setValue(expiryYearNodeId,
                                    AutofillValue.forText(cD.month + "/" + cD.year), CreatePresentation(cD.bankName, cD.cardType));
                        } else {
                            if (expiryMonNodeID != null) {
                                if (expiryMonthNode.getAutofillOptions()!=null&&
                                        Arrays.asList(expiryMonthNode.getAutofillOptions()).size()>0) {
                                    List<CharSequence> autoFillOptions = Arrays.asList(expiryMonthNode.getAutofillOptions());
                                    int ListIndex=0;
                                    for (CharSequence AO : autoFillOptions) {
                                        String ao = AO.toString();
                                        if (ao.trim().contains(cD.month.trim()) || ao.toLowerCase().trim()
                                                .contains(Month.of(Integer.valueOf(cD.month)).name().toLowerCase().trim())) {
                                            ListIndex = autoFillOptions.indexOf(ao);
                                        }
                                    }
                                    dataSetBuilder.setValue(expiryMonNodeID,
                                            AutofillValue.forList(ListIndex), CreatePresentation(cD.bankName, cD.cardType));
                                } else {
                                    dataSetBuilder.setValue(expiryMonNodeID,
                                            AutofillValue.forText(cD.month), CreatePresentation(cD.bankName, cD.cardType));
                                }
                            }
                            if (expiryYearNodeId != null) {
                                if (expiryYearNode.getAutofillOptions()!=null&&
                                        Arrays.asList(expiryYearNode.getAutofillOptions()).size()>0) {
                                    List<CharSequence> autoFillOptions = Arrays.asList(expiryYearNode.getAutofillOptions());
                                    int ListIndex = 0;
                                    for (CharSequence AO : autoFillOptions) {
                                        String ao = AO.toString();
                                        if (ao.trim().contains(cD.year.trim())) {
                                            ListIndex = autoFillOptions.indexOf(ao);
                                        }
                                    }
                                    dataSetBuilder.setValue(expiryYearNodeId,
                                            AutofillValue.forList(ListIndex), CreatePresentation(cD.bankName, cD.cardType));
                                } else {
                                    dataSetBuilder.setValue(expiryYearNodeId,
                                            AutofillValue.forText(cD.year), CreatePresentation(cD.bankName, cD.cardType));
                                }
                            }
                        }
                        if (nameNodeId != null) {
                            dataSetBuilder.setValue(nameNodeId,
                                    AutofillValue.forText(cD.name), CreatePresentation(cD.bankName, cD.cardType));
                        }
                        if (cvvNodeId != null) {
                            dataSetBuilder.setValue(cvvNodeId,
                                    AutofillValue.forText(cD.cvv), CreatePresentation(cD.bankName, cD.cardType));
                        }
                        fillResponseBuilder.addDataset(dataSetBuilder.build());
                    }
                    setFinalResult();
                }

                @Override
                public void onAuthenticationFailed() {
                    finishAndRemoveTask();
                }
            });
        } else {
            finishAndRemoveTask();
        }
    }

    private void fillResponsePassword() {

        if (usernameNodeId == null && emailNodeId != null) {
            usernameNodeId = emailNodeId;
        } else if (usernameNodeId == null && phoneNodeId != null) {
            usernameNodeId = phoneNodeId;
        }
        List<PasswordDataClass> passwordData = new ArrayList<>(dataModel.dbHelper.getAllPassword());
        final List<PasswordDataClass> validPassword = new ArrayList<>();
        for (PasswordDataClass pd : passwordData) {
            if (pd.subText.contains(packageName)) {
                validPassword.add(pd);
            }
        }

        if (usernameNodeId != null && passwordNodeId != null) {
            if (validPassword.size() > 0) {
                Authenticate authenticate = new Authenticate(this, R.string.decrypt);
                authenticate.setListener(new Authenticate.authCallBack() {
                    @Override
                    public void onAuthenticationSuccess(String maspass) {
                        for (PasswordDataClass pd : validPassword) {
                            try {
                                pd.username = cipherClass.decrypt(pd.username, maspass);
                                pd.password = cipherClass.decrypt(pd.password, maspass);
                            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                                e.printStackTrace();
                                finishAndRemoveTask();
                            }

                            fillResponseBuilder.addDataset(new Dataset.Builder()
                                    .setValue(usernameNodeId,
                                            AutofillValue.forText(pd.username), CreatePresentation(pd.serviceName, pd.username))
                                    .setValue(passwordNodeId,
                                            AutofillValue.forText(pd.password), CreatePresentation(pd.serviceName, pd.username))
                                    .build());
                        }
                        Bundle clientState = new Bundle();
                        clientState.putParcelable("userNameId", usernameNodeId);
                        clientState.putParcelable("passwordId", passwordNodeId);
                        SaveInfo.Builder saveInfoBuilder = new SaveInfo.Builder(
                                SaveInfo.SAVE_DATA_TYPE_USERNAME | SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                                new AutofillId[]{usernameNodeId, passwordNodeId});
                        if (isBrowser){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                saveInfoBuilder.setTriggerId(passwordNodeId)
                                        .setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE);
                            }else {
                                saveInfoBuilder.setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE);
                            }
                        }
                        fillResponseBuilder.setSaveInfo(saveInfoBuilder.build()).setClientState(clientState);
                        setFinalResult();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        finishAndRemoveTask();
                    }
                });

            } else {
                Bundle clientState = new Bundle();
                clientState.putParcelable("userNameId", usernameNodeId);
                clientState.putParcelable("passwordId", passwordNodeId);
                SaveInfo.Builder saveInfoBuilder = new SaveInfo.Builder(
                        SaveInfo.SAVE_DATA_TYPE_USERNAME | SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                        new AutofillId[]{usernameNodeId, passwordNodeId});
                if (isBrowser){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        saveInfoBuilder.setTriggerId(passwordNodeId)
                                .setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE);
                    }else {
                        saveInfoBuilder.setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE);
                    }
                }
                fillResponseBuilder.setSaveInfo(saveInfoBuilder.build()).setClientState(clientState);
                setFinalResult();
            }
        } else if (passwordNodeId != null) {
            if (validPassword.size() > 0) {
                Authenticate authenticate = new Authenticate(this, R.string.decrypt);
                authenticate.setListener(new Authenticate.authCallBack() {
                    @Override
                    public void onAuthenticationSuccess(String maspass) {
                        for (PasswordDataClass pd : validPassword) {
                            try {
                                pd.username = cipherClass.decrypt(pd.username, maspass);
                                pd.password = cipherClass.decrypt(pd.password, maspass);
                            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                                e.printStackTrace();
                                finishAndRemoveTask();
                            }
                            fillResponseBuilder.addDataset(new Dataset.Builder()
                                    .setValue(passwordNodeId,
                                            AutofillValue.forText(pd.password), CreatePresentation(pd.serviceName, pd.username))
                                    .build());
                        }
                        Bundle clientState = new Bundle();
                        clientState = fillRequest.getClientState();
                        assert clientState != null;
                        usernameNodeId = clientState.getParcelable("userNameId");
                        clientState.putParcelable("passwordId", passwordNodeId);
                        if (usernameNodeId!=null){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(
                                        SaveInfo.SAVE_DATA_TYPE_USERNAME | SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                                        new AutofillId[]{usernameNodeId, passwordNodeId}).setTriggerId(passwordNodeId)
                                        .setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE).build())
                                        .setClientState(clientState);
                            }else {
                                fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(
                                        SaveInfo.SAVE_DATA_TYPE_USERNAME | SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                                        new AutofillId[]{usernameNodeId, passwordNodeId}).setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE).build())
                                        .setClientState(clientState);
                            }
                        }else {
                            fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                                    new AutofillId[]{passwordNodeId}).build()).setClientState(clientState);
                        }
                        setFinalResult();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        finishAndRemoveTask();
                    }
                });
            } else {
                Bundle clientState = new Bundle();
                clientState = fillRequest.getClientState();
                assert clientState != null;
                usernameNodeId = clientState.getParcelable("userNameId");
                clientState.putParcelable("passwordId", passwordNodeId);
                if (usernameNodeId!=null){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(
                                SaveInfo.SAVE_DATA_TYPE_USERNAME | SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                                new AutofillId[]{usernameNodeId, passwordNodeId}).setTriggerId(passwordNodeId)
                                .setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE).build())
                                .setClientState(clientState);
                    }else {
                        fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(
                                SaveInfo.SAVE_DATA_TYPE_USERNAME | SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                                new AutofillId[]{usernameNodeId, passwordNodeId}).setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE).build())
                                .setClientState(clientState);
                    }
                }else {
                    fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                            new AutofillId[]{passwordNodeId}).build()).setClientState(clientState);
                }
                setFinalResult();
            }
        } else if (usernameNodeId != null) {
            if (validPassword.size() > 0) {
                Authenticate authenticate = new Authenticate(this, R.string.decrypt);
                authenticate.setListener(new Authenticate.authCallBack() {
                    @Override
                    public void onAuthenticationSuccess(String maspass) {
                        for (PasswordDataClass pd : validPassword) {
                            try {
                                pd.username = cipherClass.decrypt(pd.username, maspass);
                                pd.password = cipherClass.decrypt(pd.password, maspass);
                            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                                e.printStackTrace();
                                finishAndRemoveTask();
                            }
                            fillResponseBuilder.addDataset(new Dataset.Builder()
                                    .setValue(usernameNodeId, AutofillValue.forText(pd.username),
                                            CreatePresentation(pd.serviceName, pd.username)).build());
                        }
                        Bundle clientState = new Bundle();
                        clientState.putParcelable("userNameId", usernameNodeId);
                        fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(SaveInfo.SAVE_DATA_TYPE_USERNAME,
                                new AutofillId[]{usernameNodeId}).build()).setClientState(clientState);
                        setFinalResult();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        finishAndRemoveTask();
                    }
                });
            } else {
                Bundle clientState = new Bundle();
                clientState.putParcelable("userNameId", usernameNodeId);
                fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(SaveInfo.SAVE_DATA_TYPE_USERNAME,
                        new AutofillId[]{usernameNodeId}).build()).setClientState(clientState);
                setFinalResult();
            }
        }
    }

    private void setFinalResult() {
        FillResponse fillResponse = fillResponseBuilder.build();
        Intent replyIntent = new Intent();
        replyIntent.putExtra(EXTRA_AUTHENTICATION_RESULT, fillResponse);
        setResult(RESULT_OK, replyIntent);
        finishAndRemoveTask();
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
