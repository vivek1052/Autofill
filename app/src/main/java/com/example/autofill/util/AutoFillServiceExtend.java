package com.example.autofill.util;

import android.app.PendingIntent;
import android.app.Service;
import android.app.assist.AssistStructure;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.service.autofill.AutofillService;
import android.service.autofill.FillCallback;
import android.service.autofill.FillEventHistory;
import android.service.autofill.FillRequest;
import android.service.autofill.FillContext;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveRequest;
import android.text.InputType;
import android.util.Pair;
import android.view.View;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillManager;
import android.widget.RemoteViews;

import com.example.autofill.FillResposeActivity;
import com.example.autofill.OnSaveAutoFillActivity;
import com.example.autofill.dataClass.ParsedStructure;

import java.util.ArrayList;
import java.util.List;


public class AutoFillServiceExtend extends AutofillService {

    NodeParser nodeParser = new NodeParser();

    @Override
    public void onFillRequest(FillRequest fillRequest, CancellationSignal cancellationSignal, final FillCallback fillCallback) {
        List<FillContext> context = fillRequest.getFillContexts();
        AssistStructure structure = context.get(context.size() - 1).getStructure();
        String packageName = structure.getActivityComponent().getPackageName();

        if (CompareStringBase(packageName, GenericStringBase.restrictedPackages)) {
            return;
        }

        ArrayList<ParsedStructure> passedNodes = nodeParser.TraverseStructure(structure);
        String formType = nodeParser.determineFormType(passedNodes);
        if (passedNodes.size()==0 || formType.equals("")){
            return;
        }
        AutofillId[] autofillId = new AutofillId[passedNodes.size()];
        for (int i=0;i<passedNodes.size();i++){
            autofillId[i] = passedNodes.get(i).nodeId;
        }

        RemoteViews authPresentation = new RemoteViews(getPackageName(), android.R.layout.simple_list_item_1);
        authPresentation.setTextViewText(android.R.id.text1, "Autofill Master Password");
        Intent authIntent = new Intent(this, FillResposeActivity.class);

        Bundle fillReqBundle = new Bundle();
        fillReqBundle.putParcelableArrayList("PassedNodes", passedNodes);
        fillReqBundle.putParcelable("FillRequest",fillRequest);
        fillReqBundle.putString("FormType",formType);

        authIntent.putExtra("fillReqBundle", fillReqBundle);
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

        Bundle saveReqBundle = new Bundle();
        saveReqBundle.putParcelable("saveRequest",saveRequest);
        Intent saveIntent = new Intent(this, OnSaveAutoFillActivity.class);
        saveIntent.putExtra("saveReqBundle", saveReqBundle);
        saveIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(saveIntent);
        saveCallback.onSuccess();
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
