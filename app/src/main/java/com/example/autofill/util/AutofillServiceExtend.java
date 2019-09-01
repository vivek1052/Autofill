package com.example.autofill.util;

import android.app.PendingIntent;
import android.app.assist.AssistStructure;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.service.autofill.AutofillService;
import android.service.autofill.FillCallback;
import android.service.autofill.FillRequest;
import android.service.autofill.FillContext;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveRequest;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.autofill.AutofillId;
import android.widget.RemoteViews;

import com.example.autofill.FillResposeActivity;
import com.example.autofill.OnSaveAutoFillActivity;
import com.example.autofill.dataClass.ParsedStructure;

import java.util.ArrayList;
import java.util.List;


public class AutofillServiceExtend extends AutofillService {

    @Override
    public void onFillRequest(FillRequest fillRequest, CancellationSignal cancellationSignal, final FillCallback fillCallback) {
        List<FillContext> context = fillRequest.getFillContexts();
        AssistStructure structure = context.get(context.size() - 1).getStructure();
        String packageName = structure.getActivityComponent().getPackageName();

        if (CompareStringBase(packageName, GenericStringBase.restrictedPackages)) {
            return;
        }

        List<viewNodeDataClass> passedNodes = traverseStructure(structure);
        if (passedNodes.size()==0){
            return;
        }

        AutofillId[] autofillId = new AutofillId[passedNodes.size()];
        ArrayList<ParsedStructure> parsedPassedNodes = new ArrayList<>();
        for (int i = 0; i < passedNodes.size(); i++) {
            autofillId[i] = passedNodes.get(i).viewNode.getAutofillId();
            parsedPassedNodes.add(new ParsedStructure(passedNodes.get(i).viewNode.getAutofillId(),
                    passedNodes.get(i).autoFillHint,String.valueOf(passedNodes.get(i).viewNode.getText()),
                    passedNodes.get(i).viewNode.getWebScheme()+"://"+passedNodes.get(i).viewNode.getWebDomain()));
        }
        RemoteViews authPresentation = new RemoteViews(getPackageName(), android.R.layout.simple_list_item_1);
        authPresentation.setTextViewText(android.R.id.text1, "Autofill Master Password");
        Intent authIntent = new Intent(this, FillResposeActivity.class);

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("passedNodes", parsedPassedNodes);
        bundle.putString("packageName", packageName);

        authIntent.putExtra("Data", bundle);
        IntentSender intentSender = PendingIntent.getActivity(
                this,
                1001,
                authIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
        ).getIntentSender();

        FillResponse fillResponse = new FillResponse.Builder()
                .setAuthentication(autofillId, intentSender, authPresentation)
                .build();
        fillCallback.onSuccess(fillResponse);
    }

    @Override
    public void onSaveRequest(SaveRequest saveRequest, SaveCallback saveCallback) {
        // Get the structure from the request
        List<FillContext> context = saveRequest.getFillContexts();
        AssistStructure structure = context.get(context.size() - 1).getStructure();
        List<viewNodeDataClass> passedNodes = traverseStructure(structure);
        if (passedNodes.size()==0){
            return;
        }
        String packageName = structure.getActivityComponent().getPackageName();
        ArrayList<ParsedStructure> parsedPassedNodes = new ArrayList<>();
        for (int i = 0; i < passedNodes.size(); i++) {
            parsedPassedNodes.add(new ParsedStructure(passedNodes.get(i).viewNode.getAutofillId(),
                    passedNodes.get(i).autoFillHint,passedNodes.get(i).viewNode.getText().toString(),
                    passedNodes.get(i).viewNode.getWebScheme()+"://"+passedNodes.get(i).viewNode.getWebDomain()));
        }

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("passedNodes", parsedPassedNodes);
        bundle.putString("packageName", packageName);

        Intent saveIntent = new Intent(this, OnSaveAutoFillActivity.class);
        saveIntent.putExtra("Data",bundle);
        IntentSender intentSender = PendingIntent.getActivity(
                this,
                1001,
                saveIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
        ).getIntentSender();
        saveCallback.onSuccess(intentSender);
    }


    public List<viewNodeDataClass> traverseStructure(AssistStructure structure) {
        List<viewNodeDataClass> passedNode = new ArrayList<>();
        int nodes = structure.getWindowNodeCount();

        for (int i = 0; i < nodes; i++) {
            AssistStructure.WindowNode windowNode = structure.getWindowNodeAt(i);
            AssistStructure.ViewNode viewNode = windowNode.getRootViewNode();
            passedNode.addAll(traverseNode(viewNode));
        }
        return passedNode;
    }

    public List<viewNodeDataClass> traverseNode(AssistStructure.ViewNode viewNode) {
        List<viewNodeDataClass> passedNodes = new ArrayList<>();
        if (String.valueOf(viewNode.getClassName()).contains("EditText")) {
            String hint = viewNode.getHint();
            String entryId = viewNode.getIdEntry();
            if (viewNode.getAutofillHints() != null && viewNode.getAutofillHints().length > 0) {
                passedNodes.add(new viewNodeDataClass(viewNode, viewNode.getAutofillHints()[0]));
            } else if (CompareStringBase(hint, GenericStringBase.password) || CompareStringBase(entryId,GenericStringBase.password)) {
                passedNodes.add(new viewNodeDataClass(viewNode, View.AUTOFILL_HINT_PASSWORD));
            } else if (CompareStringBase(hint, GenericStringBase.username) || CompareStringBase(entryId,GenericStringBase.username)) {
                passedNodes.add(new viewNodeDataClass(viewNode, View.AUTOFILL_HINT_USERNAME));
            } else if (CompareStringBase(hint, GenericStringBase.email) || CompareStringBase(entryId,GenericStringBase.email)) {
                passedNodes.add(new viewNodeDataClass(viewNode, View.AUTOFILL_HINT_EMAIL_ADDRESS));
            } else if (CompareStringBase(hint, GenericStringBase.phone) || CompareStringBase(entryId,GenericStringBase.phone)) {
                passedNodes.add(new viewNodeDataClass(viewNode, View.AUTOFILL_HINT_PHONE));
            } else if (CompareStringBase(hint, GenericStringBase.cardNo) || CompareStringBase(entryId,GenericStringBase.cardNo)) {
                passedNodes.add(new viewNodeDataClass(viewNode, View.AUTOFILL_HINT_CREDIT_CARD_NUMBER));
            } else if (CompareStringBase(hint, GenericStringBase.expiryMonth) || CompareStringBase(entryId,GenericStringBase.expiryMonth)) {
                passedNodes.add(new viewNodeDataClass(viewNode, View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH));
            } else if (CompareStringBase(hint, GenericStringBase.expiryYear) || CompareStringBase(entryId,GenericStringBase.expiryYear)) {
                passedNodes.add(new viewNodeDataClass(viewNode, View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR));
            } else if (CompareStringBase(hint, GenericStringBase.holderName) || CompareStringBase(entryId,GenericStringBase.holderName)) {
                passedNodes.add(new viewNodeDataClass(viewNode, View.AUTOFILL_HINT_NAME));
            } else if (CompareStringBase(hint, GenericStringBase.cvv) || CompareStringBase(entryId,GenericStringBase.cvv)) {
                passedNodes.add(new viewNodeDataClass(viewNode, View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE));
            } else if (viewNode.getInputType() == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                    viewNode.getInputType() == InputType.TYPE_NUMBER_VARIATION_PASSWORD ||
                    viewNode.getInputType() == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD) {
                passedNodes.add(new viewNodeDataClass(viewNode, View.AUTOFILL_HINT_PASSWORD));
            } else if (viewNode.getInputType() == InputType.TYPE_CLASS_PHONE ||
                    viewNode.getInputType() == InputType.TYPE_TEXT_VARIATION_PHONETIC) {
                passedNodes.add(new viewNodeDataClass(viewNode, View.AUTOFILL_HINT_PHONE));
            } else {
                passedNodes.add(new viewNodeDataClass(viewNode, "UNKNOWN"));
            }
        }

        String headerText = null;
        for (int i = 0; i < viewNode.getChildCount(); i++) {
            AssistStructure.ViewNode childNode = viewNode.getChildAt(i);
            if (String.valueOf(childNode.getClassName()).contains("TextView")){
                headerText = childNode.getText().toString();
            }
            List<viewNodeDataClass> tempPassedNodes = new ArrayList<>(traverseNode(childNode));
            for (viewNodeDataClass pn:tempPassedNodes){
                if (pn.autoFillHint.equals("UNKNOWN")){
                    tempPassedNodes.remove(pn);
                    if (!TextUtils.isEmpty(headerText)){
                        if (CompareStringBase(headerText,GenericStringBase.username)){
                            tempPassedNodes.add(new viewNodeDataClass(pn.viewNode,View.AUTOFILL_HINT_USERNAME));
                        }else if (CompareStringBase(headerText,GenericStringBase.cardNo)){
                            tempPassedNodes.add(new viewNodeDataClass(pn.viewNode,View.AUTOFILL_HINT_CREDIT_CARD_NUMBER));
                        }
                    }
                }
            }
            passedNodes.addAll(tempPassedNodes);
        }
        return passedNodes;
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

    class viewNodeDataClass{
        AssistStructure.ViewNode viewNode;
        String autoFillHint;
        public viewNodeDataClass(AssistStructure.ViewNode viewNode, String autoFillHint){
            this.autoFillHint = autoFillHint;
            this.viewNode = viewNode;
        }
    }

}
