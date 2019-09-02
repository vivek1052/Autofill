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

    NodeParser nodeParser = new NodeParser();

    @Override
    public void onFillRequest(FillRequest fillRequest, CancellationSignal cancellationSignal, final FillCallback fillCallback) {
        List<FillContext> context = fillRequest.getFillContexts();
        AssistStructure structure = context.get(context.size() - 1).getStructure();
        String packageName = structure.getActivityComponent().getPackageName();

        if (nodeParser.CompareStringBase(packageName, GenericStringBase.restrictedPackages)) {
            return;
        }

        ArrayList<ParsedStructure> passedNodes = nodeParser.TraverseStructure(structure);
        if (passedNodes.size()==0){
            return;
        }
        AutofillId[] autofillId = new AutofillId[passedNodes.size()];
        for (int i=0;i<passedNodes.size();i++){
            autofillId[i] = passedNodes.get(i).nodeId;
        }

        RemoteViews authPresentation = new RemoteViews(getPackageName(), android.R.layout.simple_list_item_1);
        authPresentation.setTextViewText(android.R.id.text1, "Autofill Master Password");
        Intent authIntent = new Intent(this, FillResposeActivity.class);

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("passedNodes", passedNodes);
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
        ArrayList<ParsedStructure> passedNodes = nodeParser.TraverseStructure(structure);
        if (passedNodes.size()==0){
            return;
        }
        String packageName = structure.getActivityComponent().getPackageName();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("passedNodes", passedNodes);
        bundle.putString("packageName", packageName);

        Intent saveIntent = new Intent(this, OnSaveAutoFillActivity.class);
        saveIntent.putExtra("Data", bundle);
        saveIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(saveIntent);
        saveCallback.onSuccess();
    }


}
