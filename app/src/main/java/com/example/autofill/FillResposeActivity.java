package com.example.autofill;

import android.app.assist.AssistStructure;
import android.app.assist.AssistStructure.ViewNode;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.autofill.Dataset;
import android.service.autofill.FillContext;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveInfo;
import android.text.TextUtils;
import android.view.View;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.widget.RemoteViews;

import androidx.appcompat.app.AppCompatActivity;

import com.example.autofill.dataClass.AddressDataClass;
import com.example.autofill.dataClass.CardDataClass;
import com.example.autofill.dataClass.IdentityDataClass;
import com.example.autofill.dataClass.ParsedStructure;
import com.example.autofill.dataClass.PasswordDataClass;
import com.example.autofill.util.Authenticate;
import com.example.autofill.util.CipherClass;
import com.example.autofill.util.Contract;
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

    private static final String LOGIN_FORM = "LOGIN_FORM";
    private static final String CARD_FORM = "CARD_FORM";
    private static final String ADDRESS_FORM = "ADDRESS_FORM";
    private static final String IDENTITY_FORM = "IDENTITY_FORM";
    private static final String HINT_CITY = "CITY";
    private static final String HINT_STATE = "STATE";
    private static final String HINT_LOCALITY = "LOCALITY";
    private static final String HINT_FLAT_NO = "FLAT_NO";
    private static final String HINT_BUILDING_NAME = "BUILDING_NAME";
    private static final String HINT_STREET_NO = "STREET_NO";
    private static final String HINT_STREET_NAME = "STREET_NAME";
    private static final String HINT_COUNTRY = "COUNTRY";
    private static final String HINT_AADHAAR = "AADHAAR";
    private static final String HINT_PAN_CARD = "PAN_CARD";
    private static final String HINT_PASSPORT = "PASSPORT";
    private static final String HINT_DRIVING_LICENSE = "DRIVING_LICENSE";
    private static final String HINT_UAN_NUMBER = "UAN_NUMBER" ;
    private static final String AADHAAR_CARD = "Aadhaar Card";
    private static final String DRIVING_LICENCE = "Driving Licence";
    private static final String PAN_CARD = "Pan Card";
    private static final String PASSPORT = "Passport";
    private static final String UAN_NUMBER = "UAN Number";
    AutofillId usernameNodeId, passwordNodeId, phoneNodeId, emailNodeId, cardNoNodeId,
            expiryMonNodeID, expiryYearNodeId, nameNodeId, cvvNodeId, aadhaarNodeId, drivingNodeId,
            passportNodeId,panCardNodeId, uanNumberNodeId;
    String packageName;
    DataModel dataModel;
    CipherClass cipherClass;
    FillRequest fillRequest;
    FillResponse.Builder fillResponseBuilder;
    NodeParser nodeParser = new NodeParser();
    ArrayList<ParsedStructure> passedNodes;
    AssistStructure assistStructure;
    Boolean isBrowser = false;

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
        passedNodes = fillReqBundle.getParcelableArrayList("PassedNodes");
        List<FillContext> fillContexts = fillRequest.getFillContexts();
        assistStructure = fillContexts.get(fillContexts.size() - 1).getStructure();
        packageName = assistStructure.getActivityComponent().getPackageName();

        if (CompareStringBase(packageName, GenericStringBase.browser)) {
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    isBrowser = false;
                    return;
                }
                isBrowser = true;
                ViewNode random = nodeParser.TraverseStructure(assistStructure, passedNodes.get(0).nodeId);
                String webUrl = random.getWebScheme() + "://" + random.getWebDomain();
                URI uri = new URI(webUrl);
                packageName = InternetDomainName.from(uri.getHost()).topPrivateDomain()
                        .toString().split(Pattern.quote("."))[0];
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        if (formType.equals(LOGIN_FORM)) {
            fillResponsePassword();
        } else if (formType.equals(CARD_FORM)) {
            fillResponseCard();
        } else if (formType.equals(ADDRESS_FORM)) {
            fillResponseAddress();
        } else if (formType.equals(IDENTITY_FORM)) {
            fillResponseIdentity();
        } else {
            finishAndRemoveTask();
        }

    }

    private void fillResponseIdentity() {
        for (ParsedStructure pn: passedNodes){
            switch (pn.autofillhint){
                case HINT_AADHAAR:
                    aadhaarNodeId = pn.nodeId;
                    break;
                case HINT_DRIVING_LICENSE:
                    drivingNodeId = pn.nodeId;
                    break;
                case HINT_PASSPORT:
                    passportNodeId = pn.nodeId;
                    break;
                case HINT_PAN_CARD:
                    panCardNodeId = pn.nodeId;
                    break;
                case HINT_UAN_NUMBER:
                    uanNumberNodeId = pn.nodeId;
                    break;
            }
        }

        if (aadhaarNodeId!=null && nodeParser.TraverseStructure(assistStructure,aadhaarNodeId).isFocused()){
            final List<IdentityDataClass> identityData = dataModel.dbHelper.getIdentity(AADHAAR_CARD);
            if (identityData.size() > 0){
                final Authenticate authenticate = new Authenticate(this, R.string.decrypt);
                authenticate.setListener(new Authenticate.authCallBack() {
                    @Override
                    public void onAuthenticationSuccess(String maspass) {
                        for (IdentityDataClass id: identityData){
                            try {
                                id.identityNumber = cipherClass.decrypt(id.identityNumber,maspass);
                            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                                e.printStackTrace();
                            }
                            fillResponseBuilder.addDataset(new Dataset.Builder().setValue(aadhaarNodeId,
                                    AutofillValue.forText(id.identityNumber),CreatePresentation(id.identityType,
                                            id.identityNumber)).build());
                        }
                        setFinalResult();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        finishAndRemoveTask();
                    }
                });
            }else {
                finishAndRemoveTask();
            }
        }else if (drivingNodeId!=null && nodeParser.TraverseStructure(assistStructure,drivingNodeId).isFocused()){
            final List<IdentityDataClass> identityData = dataModel.dbHelper.getIdentity(DRIVING_LICENCE);
            if (identityData.size() > 0){
                final Authenticate authenticate = new Authenticate(this, R.string.decrypt);
                authenticate.setListener(new Authenticate.authCallBack() {
                    @Override
                    public void onAuthenticationSuccess(String maspass) {
                        for (IdentityDataClass id: identityData){
                            try {
                                id.identityNumber = cipherClass.decrypt(id.identityNumber,maspass);
                            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                                e.printStackTrace();
                            }
                            fillResponseBuilder.addDataset(new Dataset.Builder().setValue(drivingNodeId,
                                    AutofillValue.forText(id.identityNumber),CreatePresentation(id.identityType,
                                            id.identityNumber)).build());
                        }
                        setFinalResult();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        finishAndRemoveTask();
                    }
                });
            }else {
                finishAndRemoveTask();
            }
        }else if (passportNodeId!=null && nodeParser.TraverseStructure(assistStructure,passportNodeId).isFocused()){
            final List<IdentityDataClass> identityData = dataModel.dbHelper.getIdentity(PASSPORT);
            if (identityData.size() > 0){
                final Authenticate authenticate = new Authenticate(this, R.string.decrypt);
                authenticate.setListener(new Authenticate.authCallBack() {
                    @Override
                    public void onAuthenticationSuccess(String maspass) {
                        for (IdentityDataClass id: identityData){
                            try {
                                id.identityNumber = cipherClass.decrypt(id.identityNumber,maspass);
                            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                                e.printStackTrace();
                            }
                            fillResponseBuilder.addDataset(new Dataset.Builder().setValue(passportNodeId,
                                    AutofillValue.forText(id.identityNumber),CreatePresentation(id.identityType,
                                            id.identityNumber)).build());
                        }
                        setFinalResult();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        finishAndRemoveTask();
                    }
                });
            }else {
                finishAndRemoveTask();
            }
        }else if (panCardNodeId!=null && nodeParser.TraverseStructure(assistStructure,panCardNodeId).isFocused()){
            final List<IdentityDataClass> identityData = dataModel.dbHelper.getIdentity(PAN_CARD);
            if (identityData.size() > 0){
                final Authenticate authenticate = new Authenticate(this, R.string.decrypt);
                authenticate.setListener(new Authenticate.authCallBack() {
                    @Override
                    public void onAuthenticationSuccess(String maspass) {
                        for (IdentityDataClass id: identityData){
                            try {
                                id.identityNumber = cipherClass.decrypt(id.identityNumber,maspass);
                            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                                e.printStackTrace();
                            }
                            fillResponseBuilder.addDataset(new Dataset.Builder().setValue(panCardNodeId,
                                    AutofillValue.forText(id.identityNumber),CreatePresentation(id.identityType,
                                            id.identityNumber)).build());
                        }
                        setFinalResult();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        finishAndRemoveTask();
                    }
                });
            }else {
                finishAndRemoveTask();
            }
        }else if (uanNumberNodeId!=null && nodeParser.TraverseStructure(assistStructure,uanNumberNodeId).isFocused()){
            final List<IdentityDataClass> identityData = dataModel.dbHelper.getIdentity(UAN_NUMBER);
            if (identityData.size() > 0){
                final Authenticate authenticate = new Authenticate(this, R.string.decrypt);
                authenticate.setListener(new Authenticate.authCallBack() {
                    @Override
                    public void onAuthenticationSuccess(String maspass) {
                        for (IdentityDataClass id: identityData){
                            try {
                                id.identityNumber = cipherClass.decrypt(id.identityNumber,maspass);
                            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                                e.printStackTrace();
                            }
                            fillResponseBuilder.addDataset(new Dataset.Builder().setValue(uanNumberNodeId,
                                    AutofillValue.forText(id.identityNumber),CreatePresentation(id.identityType,
                                            id.identityNumber)).build());
                        }
                        setFinalResult();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        finishAndRemoveTask();
                    }
                });
            }else {
                finishAndRemoveTask();
            }
        }
    }

    private void fillResponseAddress() {
        List<AddressDataClass> addressData = dataModel.dbHelper.getAllAddress();
        if (addressData.size() > 0) {
            for (AddressDataClass ad : addressData) {
                Dataset.Builder dataSetBuilder = new Dataset.Builder();
                for (ParsedStructure ps : passedNodes) {
                    ViewNode currentNode = nodeParser.TraverseStructure(assistStructure, ps.nodeId);
                    if (currentNode.getAutofillOptions() != null && currentNode.getAutofillOptions().length > 0) {
                        List<CharSequence> autofillOptions = Arrays.asList(currentNode.getAutofillOptions());
                        String addressLine = getAddressLine(ps, ad, autofillOptions);
                        if (addressLine != null) {
                            dataSetBuilder.setValue(ps.nodeId,
                                    AutofillValue.forList(Integer.valueOf(addressLine)), CreatePresentation(ad.name,
                                            ad.locality + " " + ad.city));
                        }
                    } else {
                        String addressLine = getAddressLine(ps, ad, new ArrayList<CharSequence>());
                        if (addressLine != null) {
                            dataSetBuilder.setValue(ps.nodeId,
                                    AutofillValue.forText(addressLine), CreatePresentation(ad.name,
                                            ad.locality + " " + ad.city));
                        }
                    }
                }
                fillResponseBuilder.addDataset(dataSetBuilder.build());
            }
            setFinalResult();
        } else {
            finishAndRemoveTask();
        }
    }

    private String getAddressLine(ParsedStructure ps, AddressDataClass ad, List<CharSequence> autofillOptions) {
        String addressLine = "";
        String separator = "";
        for (ParsedStructure pn : passedNodes) {
            if (pn.nodeId.equals(ps.nodeId)) {
                switch (pn.autofillhint) {
                    case View.AUTOFILL_HINT_NAME:
                        addressLine = addressLine + separator + ad.name;
                        break;
                    case HINT_FLAT_NO:
                        addressLine = addressLine + separator + ad.flatNo;
                        break;
                    case HINT_BUILDING_NAME:
                        addressLine = addressLine + separator + ad.buildingName;
                        break;
                    case HINT_STREET_NO:
                        addressLine = addressLine + separator + ad.streetNo;
                        break;
                    case HINT_STREET_NAME:
                        addressLine = addressLine + separator + ad.streetName;
                        break;
                    case HINT_LOCALITY:
                        addressLine = addressLine + separator + ad.locality;
                        break;
                    case HINT_CITY:
                        addressLine = addressLine + separator + ad.city;
                        break;
                    case HINT_STATE:
                        if (autofillOptions.size() > 0) {
                            int ListIndex = 0;
                            for (CharSequence AO : autofillOptions) {
                                String ao = AO.toString().toLowerCase().trim();
                                if (ao.equals(ad.state.toLowerCase().trim())) {
                                    ListIndex = autofillOptions.indexOf(AO);
                                    break;
                                }
                            }
                            return String.valueOf(ListIndex);
                        } else {
                            addressLine = addressLine + separator + ad.state;
                        }
                        break;
                    case View.AUTOFILL_HINT_POSTAL_CODE:
                        addressLine = addressLine + separator + ad.postalCode;
                        break;
                    case HINT_COUNTRY:
                        if (autofillOptions.size() > 0) {
                            int ListIndex = 0;
                            for (CharSequence AO : autofillOptions) {
                                String ao = AO.toString().toLowerCase().trim();
                                if (ao.equals(ad.country.toLowerCase().trim())) {
                                    ListIndex = autofillOptions.indexOf(AO);
                                    break;
                                }
                            }
                            return String.valueOf(ListIndex);
                        } else {
                            addressLine = addressLine + separator + ad.country;
                        }
                        break;
                    case View.AUTOFILL_HINT_PHONE:
                        addressLine = addressLine + separator + ad.phoneNo;
                        break;
                }
                separator = ", ";
            }
        }
        addressLine = addressLine.trim();
        if (TextUtils.isEmpty(addressLine)) {
            return null;
        }
        return addressLine;
    }


    private void fillResponseCard() {
        for (int i = 0; i < passedNodes.size(); i++) {
            ParsedStructure ps = passedNodes.get(i);
            switch (ps.autofillhint) {
                case View.AUTOFILL_HINT_CREDIT_CARD_NUMBER:
                    cardNoNodeId = ps.nodeId;
                    break;
                case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH:
                    expiryMonNodeID = ps.nodeId;
                    break;
                case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR:
                    expiryYearNodeId = ps.nodeId;
                    break;
                case View.AUTOFILL_HINT_NAME:
                    nameNodeId = ps.nodeId;
                    break;
                case View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE:
                    cvvNodeId = ps.nodeId;
                    break;
            }

        }

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
                                    CreatePresentation(cD.bankName, cD.cardType+"-"+cD.cardNo4));
                        }

                        if (expiryMonNodeID != null) {
                            ViewNode expiryMonthNode = nodeParser.TraverseStructure(assistStructure, expiryMonNodeID);
                            if (expiryMonthNode.getAutofillOptions() != null &&
                                    Arrays.asList(expiryMonthNode.getAutofillOptions()).size() > 0) {
                                List<CharSequence> autoFillOptions = Arrays.asList(expiryMonthNode.getAutofillOptions());
                                String month = getCardExpiry(expiryMonNodeID, cD, autoFillOptions);
                                if (month != null) {
                                    dataSetBuilder.setValue(expiryMonNodeID,
                                            AutofillValue.forList(Integer.valueOf(month)), CreatePresentation(cD.bankName, cD.cardType+"-"+cD.cardNo4));
                                }
                            } else {
                                String month = getCardExpiry(expiryMonNodeID, cD, new ArrayList<CharSequence>());
                                if (month != null) {
                                    dataSetBuilder.setValue(expiryMonNodeID,
                                            AutofillValue.forText(month), CreatePresentation(cD.bankName, cD.cardType+"-"+cD.cardNo4));
                                }
                            }
                        }
                        if (expiryYearNodeId != null) {
                            ViewNode expiryYearNode = nodeParser.TraverseStructure(assistStructure, expiryYearNodeId);
                            if (expiryYearNode.getAutofillOptions() != null &&
                                    Arrays.asList(expiryYearNode.getAutofillOptions()).size() > 0) {
                                List<CharSequence> autoFillOptions = Arrays.asList(expiryYearNode.getAutofillOptions());
                                String year = getCardExpiry(expiryYearNodeId, cD, autoFillOptions);
                                if (year != null) {
                                    dataSetBuilder.setValue(expiryYearNodeId,
                                            AutofillValue.forList(Integer.valueOf(year)), CreatePresentation(cD.bankName, cD.cardType+"-"+cD.cardNo4));
                                }
                            } else {
                                String year = getCardExpiry(expiryYearNodeId, cD, new ArrayList<CharSequence>());
                                if (year != null) {
                                    dataSetBuilder.setValue(expiryYearNodeId,
                                            AutofillValue.forText(year), CreatePresentation(cD.bankName, cD.cardType+"-"+cD.cardNo4));
                                }
                            }
                        }

                        if (nameNodeId != null) {
                            dataSetBuilder.setValue(nameNodeId,
                                    AutofillValue.forText(cD.name), CreatePresentation(cD.bankName, cD.cardType+"-"+cD.cardNo4));
                        }
                        if (cvvNodeId != null) {
                            dataSetBuilder.setValue(cvvNodeId,
                                    AutofillValue.forText(cD.cvv), CreatePresentation(cD.bankName, cD.cardType+"-"+cD.cardNo4));
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

    private String getCardExpiry(AutofillId nodeId, CardDataClass cD, List<CharSequence> autoFillOptions) {
        String expiryDate = "";
        String separator = "";
        for (ParsedStructure pn : passedNodes) {
            if (pn.nodeId.equals(nodeId)) {
                switch (pn.autofillhint) {
                    case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH:
                        if (autoFillOptions.size() > 0) {
                            int ListIndex = 0;
                            for (CharSequence AO : autoFillOptions) {
                                String ao = AO.toString().toLowerCase().trim();
                                if (ao.contains(cD.month.trim()) ||
                                        ao.contains(Month.of(Integer.valueOf(cD.month)).name().toLowerCase().trim())) {
                                    ListIndex = autoFillOptions.indexOf(AO);
                                    break;
                                }
                            }
                            return String.valueOf(ListIndex);
                        } else {
                            expiryDate = expiryDate + separator + cD.month;
                        }
                        break;
                    case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR:
                        if (autoFillOptions.size() > 0) {
                            int ListIndex = 0;
                            for (CharSequence AO : autoFillOptions) {
                                String ao = AO.toString().trim();
                                if (ao.contains(cD.year.trim())) {
                                    ListIndex = autoFillOptions.indexOf(AO);
                                    break;
                                }
                            }
                            return String.valueOf(ListIndex);
                        } else {
                            expiryDate = expiryDate + separator + cD.year;
                        }
                        break;
                }
                separator = "/";
            }
        }
        if (TextUtils.isEmpty(expiryDate)) {
            return null;
        } else {
            return expiryDate;
        }
    }

    private void fillResponsePassword() {

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
                    break;
                case View.AUTOFILL_HINT_PHONE:
                    phoneNodeId = ps.nodeId;
                    break;
            }

        }

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
                        if (isBrowser) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                saveInfoBuilder.setTriggerId(passwordNodeId)
                                        .setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE);
                            } else {
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
                if (isBrowser) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        saveInfoBuilder.setTriggerId(passwordNodeId)
                                .setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE);
                    } else {
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
                        if (usernameNodeId != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(
                                        SaveInfo.SAVE_DATA_TYPE_USERNAME | SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                                        new AutofillId[]{usernameNodeId, passwordNodeId}).setTriggerId(passwordNodeId)
                                        .setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE).build())
                                        .setClientState(clientState);
                            } else {
                                fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(
                                        SaveInfo.SAVE_DATA_TYPE_USERNAME | SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                                        new AutofillId[]{usernameNodeId, passwordNodeId}).setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE).build())
                                        .setClientState(clientState);
                            }
                        } else {
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
                if (usernameNodeId != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(
                                SaveInfo.SAVE_DATA_TYPE_USERNAME | SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                                new AutofillId[]{usernameNodeId, passwordNodeId}).setTriggerId(passwordNodeId)
                                .setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE).build())
                                .setClientState(clientState);
                    } else {
                        fillResponseBuilder.setSaveInfo(new SaveInfo.Builder(
                                SaveInfo.SAVE_DATA_TYPE_USERNAME | SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                                new AutofillId[]{usernameNodeId, passwordNodeId}).setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE).build())
                                .setClientState(clientState);
                    }
                } else {
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
