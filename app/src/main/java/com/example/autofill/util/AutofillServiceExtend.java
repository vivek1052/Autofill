package com.example.autofill.util;

import android.app.assist.AssistStructure;
import android.os.CancellationSignal;
import android.service.autofill.AutofillService;
import android.service.autofill.Dataset;
import android.service.autofill.FillCallback;
import android.service.autofill.FillRequest;
import android.service.autofill.FillContext;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveRequest;
import android.text.InputType;
import android.view.View;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.widget.RemoteViews;

import com.example.autofill.dataClass.PasswordDataClass;


import java.util.ArrayList;
import java.util.List;

public class AutofillServiceExtend extends AutofillService {
    private AutofillId username_nodeid, password_nodeid;
    @Override
    public void onFillRequest(FillRequest fillRequest, CancellationSignal cancellationSignal, FillCallback fillCallback) {
        // Get the structure from the request
        List<FillContext> context = fillRequest.getFillContexts();
        AssistStructure structure = context.get(context.size() - 1).getStructure();

        // Traverse the structure looking for nodes to fill out.
         List<ParsedStructure> passedNodes = traverseStructure(structure);
         for (int i=0;i<passedNodes.size();i++){
             switch (passedNodes.get(i).autofillhint){
                 case View.AUTOFILL_HINT_USERNAME:
                     username_nodeid = passedNodes.get(i).viewNode.getAutofillId();
                     break;
                 case View.AUTOFILL_HINT_PASSWORD:
                     password_nodeid = passedNodes.get(i).viewNode.getAutofillId();
                     break;
             }
         }

//        // Fetch user data that matches the fields.
        List<PasswordDataClass> loginData = new ArrayList<PasswordDataClass>();

        //Generate Fill repsonse

        FillResponse.Builder fillResponse = new FillResponse.Builder();

        // Add data set based on user data;
        for (int i = 0; i<loginData.size(); i++){
            fillResponse.addDataset(new Dataset.Builder()
                        .setValue(username_nodeid,
                                AutofillValue.forText(loginData.get(i).username),CreatePresentation(loginData.get(i).serviceName,
                                        loginData.get(i).subText, loginData.get(i).username))
                        .setValue(password_nodeid,
                                AutofillValue.forText(loginData.get(i).password), CreatePresentation(loginData.get(i).serviceName,
                                        loginData.get(i).subText, loginData.get(i).username))
                        .build());
        }

//        // If there are no errors, call onSuccess() and pass the response
        fillCallback.onSuccess(fillResponse.build());

    }

    @Override
    public void onSaveRequest(SaveRequest saveRequest, SaveCallback saveCallback) {

    }

    public RemoteViews CreatePresentation(String displayText, String subText1, String subText2){
        RemoteViews presentation = new RemoteViews(getPackageName(), android.R.layout.simple_list_item_1);
        presentation.setTextViewText(android.R.id.text1, displayText);
        presentation.setTextViewText(android.R.id.text2, subText1);
        presentation.setTextViewText(android.R.id.textAssist, subText2);
        return presentation;
    }

    public List<ParsedStructure> traverseStructure(AssistStructure structure) {
        List<ParsedStructure> passedNode = new ArrayList<ParsedStructure>();
        int nodes = structure.getWindowNodeCount();

        for (int i = 0; i < nodes; i++) {
            AssistStructure.WindowNode windowNode = structure.getWindowNodeAt(i);
            AssistStructure.ViewNode viewNode = windowNode.getRootViewNode();
            passedNode.addAll(traverseNode(viewNode));
        }
        return passedNode;
    }

    public List<ParsedStructure> traverseNode(AssistStructure.ViewNode viewNode) {
        List<ParsedStructure> passedNodes = new ArrayList<ParsedStructure>();
        if (viewNode.getClassName().contains("EditText")){
        if(viewNode.getAutofillHints() != null && viewNode.getAutofillHints().length > 0) {
            // If the client app provides auto fill hints, you can obtain them using:
            passedNodes.add(new ParsedStructure(viewNode, viewNode.getAutofillHints()[0]));
        } else if (viewNode.getText()  != null && viewNode.getText().toString().length() > 0){
                if(CompareStringBase(viewNode.getText().toString(),GenericStringBase.username)){
                    passedNodes.add(new ParsedStructure(viewNode, View.AUTOFILL_HINT_USERNAME));
                }
                if (CompareStringBase(viewNode.getText().toString(),GenericStringBase.password)){
                    passedNodes.add(new ParsedStructure(viewNode, View.AUTOFILL_HINT_PASSWORD));
                }
            }else if (viewNode.getHint() !=null && viewNode.getHint().length() > 0){
                if(CompareStringBase(viewNode.getHint(),GenericStringBase.username)){
                    passedNodes.add(new ParsedStructure(viewNode, View.AUTOFILL_HINT_USERNAME));
                }
                if (CompareStringBase(viewNode.getHint(),GenericStringBase.password)){
                    passedNodes.add(new ParsedStructure(viewNode, View.AUTOFILL_HINT_PASSWORD));
                }
            }else if (viewNode.getInputType() == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                viewNode.getInputType() == InputType.TYPE_NUMBER_VARIATION_PASSWORD ||
                viewNode.getInputType() == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD){
                passedNodes.add(new ParsedStructure(viewNode, View.AUTOFILL_HINT_PASSWORD));
            }else if (viewNode.getInputType() == InputType.TYPE_CLASS_PHONE ||
                viewNode.getInputType() == InputType.TYPE_TEXT_VARIATION_PHONETIC){
            passedNodes.add(new ParsedStructure(viewNode, View.AUTOFILL_HINT_PHONE));
         }else {
            passedNodes.add(new ParsedStructure(viewNode,"UNKNOWN"));
        }
        }
        for(int i = 0; i < viewNode.getChildCount(); i++) {
            AssistStructure.ViewNode childNode = viewNode.getChildAt(i);
           passedNodes.addAll(traverseNode(childNode));
        }
        return passedNodes;
    }

    public boolean CompareStringBase(String source, String target[]){
        for (int i=0; i<target.length; i++){
            if (source.toLowerCase().contains(target[i].toLowerCase())){
                return true;
            }
        }
        return false;
    }

    class ParsedStructure {
        AssistStructure.ViewNode viewNode;
        String autofillhint;
        ParsedStructure(AssistStructure.ViewNode viewNode, String autofillhint){
            this.viewNode = viewNode;
            this.autofillhint = autofillhint;
        }
    }
}
