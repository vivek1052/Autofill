package com.example.autofill;

import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.Dataset;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveInfo;
import android.view.View;
import android.view.autofill.AutofillId;
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
import java.util.List;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static android.view.autofill.AutofillManager.EXTRA_AUTHENTICATION_RESULT;

public class FillResposeActivity extends AppCompatActivity {

    AutofillId usernameNode, passwordNode, phoneNode, emailNode, cardNoNode, expiryMonthNode, expiryYearNode, nameNode, cvvNode;
    int expiryMonthIndex, expiryYearIndex;
    DataModel dataModel;
    CipherClass cipherClass;
    FillResponse.Builder fillResponseBuilder;
    String packageName;
    ArrayList<ParsedStructure> passedNodes;
    NodeParser nodeParser = new NodeParser();
    private static final String LOGIN_FORM = "LOGIN_FORM" ;
    private static final String CARD_FORM = "CARD_FORM" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        dataModel = new DataModel(this);
        cipherClass = new CipherClass();
        fillResponseBuilder = new FillResponse.Builder();
        Bundle bundle = intent.getBundleExtra("Data");
        packageName = bundle.getString("packageName");

        passedNodes = bundle.getParcelableArrayList("passedNodes");
        if (CompareStringBase(packageName, GenericStringBase.browser)) {
            try {
                URI uri = new URI(passedNodes.get(0).url);
                packageName = InternetDomainName.from(uri.getHost()).topPrivateDomain()
                        .toString().split(Pattern.quote("."))[0];
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        String formType = nodeParser.determineFormType(passedNodes);
        for (int i = 0; i < passedNodes.size(); i++) {
            ParsedStructure ps = passedNodes.get(i);
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
                    expiryMonthIndex = i;
                    break;
                case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR:
                    expiryYearNode = ps.nodeId;
                    expiryYearIndex = i;
                    break;
                case View.AUTOFILL_HINT_NAME:
                    nameNode = ps.nodeId;
                    break;
                case View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE:
                    cvvNode = ps.nodeId;
                    break;
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
            Authenticate authenticate = new Authenticate(this, R.string.decrypt);
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
                        if (cardNoNode != null) {
                            dataSetBuilder.setValue(cardNoNode,
                                    AutofillValue.forText(cD.cardNo1 + cD.cardNo2 + cD.cardNo3 + cD.cardNo4),
                                    CreatePresentation(cD.bankName, cD.cardType));
                        }
                        if (expiryMonthNode != null && expiryYearNode == null) {
                            dataSetBuilder.setValue(expiryMonthNode,
                                    AutofillValue.forText(cD.month + "/" + cD.year), CreatePresentation(cD.bankName, cD.cardType));
                        } else if (expiryMonthNode == null && expiryYearNode != null) {
                            dataSetBuilder.setValue(expiryYearNode,
                                    AutofillValue.forText(cD.month + "/" + cD.year), CreatePresentation(cD.bankName, cD.cardType));
                        } else {
                            if (expiryMonthNode != null) {
                                ArrayList<String> autoFillOptions = passedNodes.get(expiryMonthIndex).autoFillOptions;
                                if (autoFillOptions != null && autoFillOptions.size() > 0) {
                                    int ListIndex = 0;
                                    for (String ao : autoFillOptions) {
                                        if (ao.trim().contains(cD.month.trim()) || ao.toLowerCase().trim()
                                                .contains(Month.of(Integer.valueOf(cD.month)).name().toLowerCase().trim())) {
                                            ListIndex = autoFillOptions.indexOf(ao);
                                        }
                                    }
                                    dataSetBuilder.setValue(expiryMonthNode,
                                            AutofillValue.forList(ListIndex), CreatePresentation(cD.bankName, cD.cardType));
                                } else {
                                    dataSetBuilder.setValue(expiryMonthNode,
                                            AutofillValue.forText(cD.month), CreatePresentation(cD.bankName, cD.cardType));
                                }
                            }
                            if (expiryYearNode != null) {
                                ArrayList<String> autoFillOptions = passedNodes.get(expiryYearIndex).autoFillOptions;
                                int ListIndex = 0;
                                if (autoFillOptions != null && autoFillOptions.size() > 0) {
                                    for (String ao : autoFillOptions) {
                                        if (ao.trim().contains(cD.year.trim())) {
                                            ListIndex = autoFillOptions.indexOf(ao);
                                        }
                                    }
                                    dataSetBuilder.setValue(expiryYearNode,
                                            AutofillValue.forList(ListIndex), CreatePresentation(cD.bankName, cD.cardType));
                                } else {
                                    dataSetBuilder.setValue(expiryYearNode,
                                            AutofillValue.forText(cD.year), CreatePresentation(cD.bankName, cD.cardType));
                                }
                            }
                        }
                        if (nameNode != null) {
                            dataSetBuilder.setValue(nameNode,
                                    AutofillValue.forText(cD.name), CreatePresentation(cD.bankName, cD.cardType));
                        }
                        if (cvvNode != null) {
                            dataSetBuilder.setValue(cvvNode,
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

        if (usernameNode == null && emailNode != null) {
            usernameNode = emailNode;
        } else if (usernameNode == null && phoneNode != null) {
            usernameNode = phoneNode;
        }
        List<PasswordDataClass> passwordData = new ArrayList<>(dataModel.dbHelper.getAllPassword());
        final List<PasswordDataClass> validPassword = new ArrayList<>();
        for (PasswordDataClass pd : passwordData) {
            if (pd.subText.contains(packageName)) {
                validPassword.add(pd);
            }
        }

        if (usernameNode != null && passwordNode != null) {
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
                                    .setValue(usernameNode,
                                            AutofillValue.forText(pd.username), CreatePresentation(pd.serviceName, pd.username))
                                    .setValue(passwordNode,
                                            AutofillValue.forText(pd.password), CreatePresentation(pd.serviceName, pd.username))
                                    .build());
                        }
                        fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(
                                SaveInfo.SAVE_DATA_TYPE_USERNAME | SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                                new AutofillId[]{usernameNode, passwordNode}).build());
                        setFinalResult();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        finishAndRemoveTask();
                    }
                });

            } else {
                fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(
                        SaveInfo.SAVE_DATA_TYPE_USERNAME | SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                        new AutofillId[]{usernameNode, passwordNode}).build());
                setFinalResult();
            }
        } else if (passwordNode != null) {
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
                                    .setValue(passwordNode,
                                            AutofillValue.forText(pd.password), CreatePresentation(pd.serviceName, pd.username))
                                    .build());
                        }
                        fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                                new AutofillId[]{passwordNode}).build());
                        setFinalResult();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        finishAndRemoveTask();
                    }
                });
            } else {
                fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                        new AutofillId[]{passwordNode}).build());
                setFinalResult();
            }
        } else if (usernameNode != null) {
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
                                    .setValue(usernameNode, AutofillValue.forText(pd.username),
                                            CreatePresentation(pd.serviceName, pd.username)).build());
                        }
                        fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(SaveInfo.SAVE_DATA_TYPE_USERNAME,
                                new AutofillId[]{usernameNode}).build());
                        setFinalResult();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        finishAndRemoveTask();
                    }
                });
            } else {
                fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(SaveInfo.SAVE_DATA_TYPE_USERNAME,
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
