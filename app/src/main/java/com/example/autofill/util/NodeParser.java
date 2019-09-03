package com.example.autofill.util;

import android.app.assist.AssistStructure;
import android.text.InputType;
import android.util.Pair;
import android.view.View;

import com.example.autofill.dataClass.ParsedStructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;



public class NodeParser {

    private static final String INCOMPATIBLE_TYPE = "INCOMPATIBLE_TYPE";
    private static final CharSequence EDIT_TEXT = "EditText" ;
    private static final CharSequence INPUT = "input";
    private static final String UNKNOWN_HINT = "UNKNOWN";
    private static final String LOGIN_FORM = "LOGIN_FORM" ;
    private static final String CARD_FORM = "CARD_FORM" ;

    public ArrayList<ParsedStructure> TraverseStructure(AssistStructure structure) {
        List<viewNodeDataClass> passedNodes = new ArrayList<>();
        int nodes = structure.getWindowNodeCount();

        for (int i = 0; i < nodes; i++) {
            AssistStructure.WindowNode windowNode = structure.getWindowNodeAt(i);
            AssistStructure.ViewNode viewNode = windowNode.getRootViewNode();
            passedNodes.addAll(traverseNode(viewNode));
        }
        if (passedNodes.size()==0){
            return new ArrayList<>();
        }

        ArrayList<ParsedStructure> parsedPassedNodes = new ArrayList<>();
        for (int i = 0; i < passedNodes.size(); i++) {
            parsedPassedNodes.add(new ParsedStructure(passedNodes.get(i).viewNode.getAutofillId(),
                    passedNodes.get(i).autoFillHint, String.valueOf(passedNodes.get(i).viewNode.getText()),
                    passedNodes.get(i).viewNode.getWebScheme() + "://" + passedNodes.get(i).viewNode.getWebDomain(),
                    passedNodes.get(i).viewNode.getAutofillOptions()));
        }

        for (ParsedStructure pn:parsedPassedNodes){
            if (pn.autofillhint.contains(UNKNOWN_HINT)){
                parsedPassedNodes = cleanseNode(parsedPassedNodes);
                break;
            }
        }
        for (ParsedStructure pn:parsedPassedNodes){
            if (pn.autofillhint.contains(UNKNOWN_HINT)){
                parsedPassedNodes.remove(pn);
            }
        }

        return parsedPassedNodes;
    }

    private ArrayList<ParsedStructure> cleanseNode(ArrayList<ParsedStructure> passedNodes) {
        ArrayList<ParsedStructure> retunPassedNodes = new ArrayList<>(passedNodes);
        List<String> passwordNodes = new ArrayList<>();
        List<String> cardNodes = new ArrayList<>();
        for (ParsedStructure pn:passedNodes){
            passwordNodes.add(pn.autofillhint);
            cardNodes.add(pn.autofillhint);
        }
        String formType = determineFormType(passedNodes);
        if (formType.equals(LOGIN_FORM)){
            List<String> validLoginNodes = new ArrayList<>(Arrays.asList(GenericStringBase.login_form));
            validLoginNodes.removeAll(passwordNodes);
            for (String vln:validLoginNodes){
                for (ParsedStructure rpn: retunPassedNodes){
                    if (rpn.autofillhint.equals(UNKNOWN_HINT)){
                        rpn.autofillhint = vln;
                        break;
                    }
                }
            }
        }else if (formType.equals(CARD_FORM)){
            List<String> validCardNodes = new ArrayList<>(Arrays.asList(GenericStringBase.card_form));
            validCardNodes.removeAll(cardNodes);
            for (String vcn:validCardNodes){
                for (ParsedStructure rpn: retunPassedNodes){
                    if (rpn.autofillhint.equals(UNKNOWN_HINT)){
                        rpn.autofillhint = vcn;
                        break;
                    }
                }
            }
        }
        return retunPassedNodes;
    }

    public String determineFormType(ArrayList<ParsedStructure> passedNodes) {
        HashSet<String> passwordNodes = new HashSet<>();
        HashSet<String> cardNodes = new HashSet<>();
        for (ParsedStructure pn:passedNodes){
            passwordNodes.add(pn.autofillhint);
            cardNodes.add(pn.autofillhint);
        }
        passwordNodes.removeAll(Arrays.asList(GenericStringBase.login_form));
        cardNodes.removeAll(Arrays.asList(GenericStringBase.card_form));
        if (passwordNodes.size()<cardNodes.size()){
            return LOGIN_FORM;
        }else if (cardNodes.size()<passwordNodes.size()){
            return CARD_FORM;
        }
        return "";
    }

    public List<viewNodeDataClass> traverseNode(AssistStructure.ViewNode viewNode) {
        List<viewNodeDataClass> passedNodes = new ArrayList<>();

        if (String.valueOf(viewNode.getClassName()).contains(EDIT_TEXT)
                || (viewNode.getHtmlInfo() != null && viewNode.getHtmlInfo().getTag().contains(INPUT))) {

            String searchQuery = buildSearchQuery(viewNode);
            if (searchQuery.equals(INCOMPATIBLE_TYPE)){

            }else if (viewNode.getAutofillHints() != null && CompareStringBase(viewNode.getAutofillHints()[0],
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
            }else {
                passedNodes.add(new viewNodeDataClass(viewNode, UNKNOWN_HINT));
            }
        }

        for (int i = 0; i < viewNode.getChildCount(); i++) {
            AssistStructure.ViewNode childNode = viewNode.getChildAt(i);
            passedNodes.addAll(traverseNode(childNode));
        }
        return passedNodes;
    }

    private String buildSearchQuery(AssistStructure.ViewNode viewNode) {
        String searchQuery = viewNode.getIdEntry()+"|"+viewNode.getHint()+"|"+
                viewNode.getContentDescription();
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
                        return INCOMPATIBLE_TYPE;
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
