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
import android.view.View;
import android.view.autofill.AutofillId;
import android.widget.RemoteViews;

import com.example.autofill.AuthenticateAutoFillService;
import com.example.autofill.dataClass.ParsedStructure;

import java.util.ArrayList;
import java.util.List;


public class AutofillServiceExtend extends AutofillService {

    @Override
    public void onFillRequest(FillRequest fillRequest, CancellationSignal cancellationSignal, final FillCallback fillCallback) {
        List<FillContext> context = fillRequest.getFillContexts();
        AssistStructure structure = context.get(context.size() - 1).getStructure();
        String packageName = structure.getActivityComponent().getPackageName();
        ArrayList<ParsedStructure> passedNodes = traverseStructure(structure);
        AutofillId autofillId[] = new AutofillId[passedNodes.size()];
        for (int i=0; i<passedNodes.size();i++){
            autofillId[i] = passedNodes.get(i).nodeId;
        }
        RemoteViews authPresentation = new RemoteViews(getPackageName(), android.R.layout.simple_list_item_1);
        authPresentation.setTextViewText(android.R.id.text1, "Autofill Master Password");
        Intent authIntent = new Intent(this, AuthenticateAutoFillService.class);

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("passedNodes",passedNodes);
        bundle.putString("packageName",packageName);
//        authIntent.putExtra("packageName", packageName);
//        authIntent.putParcelableArrayListExtra("passedNodes",passedNodes);
        authIntent.putExtra("Data",bundle);
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
        ArrayList<ParsedStructure> passedNodes = traverseStructure(structure);
        saveCallback.onSuccess();
    }


    public ArrayList<ParsedStructure> traverseStructure(AssistStructure structure) {
        ArrayList<ParsedStructure> passedNode = new ArrayList<ParsedStructure>();
        int nodes = structure.getWindowNodeCount();

        for (int i = 0; i < nodes; i++) {
            AssistStructure.WindowNode windowNode = structure.getWindowNodeAt(i);
            AssistStructure.ViewNode viewNode = windowNode.getRootViewNode();
            passedNode.addAll(traverseNode(viewNode));
        }
        return passedNode;
    }

    public ArrayList<ParsedStructure> traverseNode(AssistStructure.ViewNode viewNode) {
        ArrayList<ParsedStructure> passedNodes = new ArrayList<ParsedStructure>();
        if (viewNode.getClassName().contains("EditText")) {
            if (viewNode.getAutofillHints() != null && viewNode.getAutofillHints().length > 0) {
                // If the client app provides auto fill hints, you can obtain them using:
                passedNodes.add(new ParsedStructure(viewNode.getAutofillId(), viewNode.getAutofillHints()[0]));
            } else if (viewNode.getText() != null && viewNode.getText().toString().length() > 0) {
                if (CompareStringBase(viewNode.getText().toString(), GenericStringBase.username)) {
                    passedNodes.add(new ParsedStructure(viewNode.getAutofillId(), View.AUTOFILL_HINT_USERNAME));
                }
                if (CompareStringBase(viewNode.getText().toString(), GenericStringBase.password)) {
                    passedNodes.add(new ParsedStructure(viewNode.getAutofillId(), View.AUTOFILL_HINT_PASSWORD));
                }
            } else if (viewNode.getHint() != null && viewNode.getHint().length() > 0) {
                if (CompareStringBase(viewNode.getHint(), GenericStringBase.username)) {
                    passedNodes.add(new ParsedStructure(viewNode.getAutofillId(), View.AUTOFILL_HINT_USERNAME));
                }
                if (CompareStringBase(viewNode.getHint(), GenericStringBase.password)) {
                    passedNodes.add(new ParsedStructure(viewNode.getAutofillId(), View.AUTOFILL_HINT_PASSWORD));
                }
            } else if (viewNode.getInputType() == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                    viewNode.getInputType() == InputType.TYPE_NUMBER_VARIATION_PASSWORD ||
                    viewNode.getInputType() == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD) {
                passedNodes.add(new ParsedStructure(viewNode.getAutofillId(), View.AUTOFILL_HINT_PASSWORD));
            } else if (viewNode.getInputType() == InputType.TYPE_CLASS_PHONE ||
                    viewNode.getInputType() == InputType.TYPE_TEXT_VARIATION_PHONETIC) {
                passedNodes.add(new ParsedStructure(viewNode.getAutofillId(), View.AUTOFILL_HINT_PHONE));
            } else {
                passedNodes.add(new ParsedStructure(viewNode.getAutofillId(), "UNKNOWN"));
            }
        }
        for (int i = 0; i < viewNode.getChildCount(); i++) {
            AssistStructure.ViewNode childNode = viewNode.getChildAt(i);
            passedNodes.addAll(traverseNode(childNode));
        }
        return passedNodes;
    }

    public boolean CompareStringBase(String source, String[] target) {
        for (int i = 0; i < target.length; i++) {
            if (source.toLowerCase().trim().contains(target[i].toLowerCase().trim())) {
                return true;
            }
        }
        return false;
    }

}
