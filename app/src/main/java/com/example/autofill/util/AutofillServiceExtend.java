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
import android.util.Pair;
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
        if (passedNodes.size() == 0) {
            return;
        }

        AutofillId[] autofillId = new AutofillId[passedNodes.size()];
        ArrayList<ParsedStructure> parsedPassedNodes = new ArrayList<>();
        for (int i = 0; i < passedNodes.size(); i++) {
            autofillId[i] = passedNodes.get(i).viewNode.getAutofillId();
            parsedPassedNodes.add(new ParsedStructure(passedNodes.get(i).viewNode.getAutofillId(),
                    passedNodes.get(i).autoFillHint, String.valueOf(passedNodes.get(i).viewNode.getText()),
                    passedNodes.get(i).viewNode.getWebScheme() + "://" + passedNodes.get(i).viewNode.getWebDomain(),
                    passedNodes.get(i).viewNode.getAutofillOptions()));
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
        if (passedNodes.size() == 0) {
            return;
        }
        String packageName = structure.getActivityComponent().getPackageName();
        ArrayList<ParsedStructure> parsedPassedNodes = new ArrayList<>();
        for (int i = 0; i < passedNodes.size(); i++) {
            parsedPassedNodes.add(new ParsedStructure(passedNodes.get(i).viewNode.getAutofillId(),
                    passedNodes.get(i).autoFillHint, passedNodes.get(i).viewNode.getText().toString(),
                    passedNodes.get(i).viewNode.getWebScheme() + "://" + passedNodes.get(i).viewNode.getWebDomain(),
                    passedNodes.get(i).viewNode.getAutofillOptions()));
        }

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("passedNodes", parsedPassedNodes);
        bundle.putString("packageName", packageName);

        Intent saveIntent = new Intent(this, OnSaveAutoFillActivity.class);
        saveIntent.putExtra("Data", bundle);
        saveIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(saveIntent);
        saveCallback.onSuccess();
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

        if (String.valueOf(viewNode.getClassName()).contains("EditText")
                || (viewNode.getHtmlInfo() != null && viewNode.getHtmlInfo().getTag().contains("input"))) {

            String searchQuery = buildSearchQuery(viewNode);

            if (viewNode.getAutofillHints() != null && CompareStringBase(viewNode.getAutofillHints()[0],
                    GenericStringBase.autofillHints)) {
                passedNodes.add(new viewNodeDataClass(viewNode, viewNode.getAutofillHints()[0]));
            } else if (CompareStringBase(searchQuery, GenericStringBase.password)) {
                passedNodes.add(new viewNodeDataClass(viewNode, View.AUTOFILL_HINT_PASSWORD));
            } else if (CompareStringBase(searchQuery, GenericStringBase.username)) {
                passedNodes.add(new viewNodeDataClass(viewNode, View.AUTOFILL_HINT_USERNAME));
            } else if (CompareStringBase(searchQuery, GenericStringBase.email)) {
                passedNodes.add(new viewNodeDataClass(viewNode, View.AUTOFILL_HINT_EMAIL_ADDRESS));
            } else if (CompareStringBase(searchQuery, GenericStringBase.phone)) {
                passedNodes.add(new viewNodeDataClass(viewNode, View.AUTOFILL_HINT_PHONE));
            } else if (CompareStringBase(searchQuery, GenericStringBase.expiryMonth)) {
                passedNodes.add(new viewNodeDataClass(viewNode, View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH));
            } else if (CompareStringBase(searchQuery, GenericStringBase.expiryYear)) {
                passedNodes.add(new viewNodeDataClass(viewNode, View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR));
            } else if (CompareStringBase(searchQuery, GenericStringBase.holderName)) {
                passedNodes.add(new viewNodeDataClass(viewNode, View.AUTOFILL_HINT_NAME));
            } else if (CompareStringBase(searchQuery, GenericStringBase.cvv)) {
                passedNodes.add(new viewNodeDataClass(viewNode, View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE));
            } else if (CompareStringBase(searchQuery, GenericStringBase.cardNo)) {
                passedNodes.add(new viewNodeDataClass(viewNode, View.AUTOFILL_HINT_CREDIT_CARD_NUMBER));
            } else if (viewNode.getInputType() == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                    viewNode.getInputType() == InputType.TYPE_NUMBER_VARIATION_PASSWORD ||
                    viewNode.getInputType() == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD) {
                passedNodes.add(new viewNodeDataClass(viewNode, View.AUTOFILL_HINT_PASSWORD));
            } else if (viewNode.getInputType() == InputType.TYPE_CLASS_PHONE ||
                    viewNode.getInputType() == InputType.TYPE_TEXT_VARIATION_PHONETIC) {
                passedNodes.add(new viewNodeDataClass(viewNode, View.AUTOFILL_HINT_PHONE));
            }
        }

        for (int i = 0; i < viewNode.getChildCount(); i++) {
            AssistStructure.ViewNode childNode = viewNode.getChildAt(i);
            passedNodes.addAll(traverseNode(childNode));
        }
        return passedNodes;
    }

    private String buildSearchQuery(AssistStructure.ViewNode viewNode) {
        String searchQuery = viewNode.getIdEntry()+"|"+viewNode.getHint();
        if (viewNode.getHtmlInfo() != null) {
            List<Pair<String, String>> HtmlValues = viewNode.getHtmlInfo().getAttributes();
            for (Pair<String, String> hv : HtmlValues) {
                if (hv.first.toLowerCase().trim().contains("label")) {
                    searchQuery = searchQuery +"|"+ hv.second;
                } else if (hv.first.toLowerCase().trim().contains("hints")) {
                    searchQuery = searchQuery +"|"+ hv.second;
                }else if (hv.first.toLowerCase().trim().contains("name")){
                    searchQuery = searchQuery +"|"+ hv.second;
                }else if (hv.first.toLowerCase().trim().contains("type")){
                    if (!CompareStringBase(hv.second,GenericStringBase.allowedHtmlInputTypes)){
                        return "";
                    }
                }
            }
        }
        return searchQuery;
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

    class viewNodeDataClass {
        AssistStructure.ViewNode viewNode;
        String autoFillHint;

        public viewNodeDataClass(AssistStructure.ViewNode viewNode, String autoFillHint) {
            this.autoFillHint = autoFillHint;
            this.viewNode = viewNode;
        }
    }

}
