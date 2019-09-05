package com.example.autofill.util;

import android.app.assist.AssistStructure;
import android.app.assist.AssistStructure.ViewNode;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.autofill.AutofillId;

import com.example.autofill.dataClass.ParsedStructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


public class NodeParser {

    private static final String INCOMPATIBLE_TYPE = "INCOMPATIBLE_TYPE";
    private static final CharSequence EDIT_TEXT = "EditText";
    private static final CharSequence INPUT = "input";
    private static final String UNKNOWN_HINT = "UNKNOWN";
    private static final String LOGIN_FORM = "LOGIN_FORM";
    private static final String CARD_FORM = "CARD_FORM";
    private static final String RESTRICTED = "RESTRICTED";
    private List<stringBasePair> stringBaseList = new ArrayList<>();

    public ArrayList<ParsedStructure> TraverseStructure(AssistStructure structure) {

        stringBaseList.add(new stringBasePair(GenericStringBase.password, View.AUTOFILL_HINT_PASSWORD));
        stringBaseList.add(new stringBasePair(GenericStringBase.username, View.AUTOFILL_HINT_USERNAME));
        stringBaseList.add(new stringBasePair(GenericStringBase.email, View.AUTOFILL_HINT_EMAIL_ADDRESS));
        stringBaseList.add(new stringBasePair(GenericStringBase.phone, View.AUTOFILL_HINT_PHONE));
        stringBaseList.add(new stringBasePair(GenericStringBase.expiryMonth, View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH));
        stringBaseList.add(new stringBasePair(GenericStringBase.expiryYear, View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR));
        stringBaseList.add(new stringBasePair(GenericStringBase.holderName, View.AUTOFILL_HINT_NAME));
        stringBaseList.add(new stringBasePair(GenericStringBase.cvv, View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE));
        stringBaseList.add(new stringBasePair(GenericStringBase.cardNo, View.AUTOFILL_HINT_CREDIT_CARD_NUMBER));
        ArrayList<ParsedStructure> passedNodes = new ArrayList<>();
        int nodes = structure.getWindowNodeCount();

        for (int i = 0; i < nodes; i++) {
            AssistStructure.WindowNode windowNode = structure.getWindowNodeAt(i);
            ViewNode viewNode = windowNode.getRootViewNode();
            passedNodes.addAll(traverseNode(viewNode));
        }
        passedNodes = cleanseNode(passedNodes);
        for (ParsedStructure pn : passedNodes) {
            if (pn.autofillhint.contains(UNKNOWN_HINT)) {
                passedNodes.remove(pn);
            }
        }
        return passedNodes;
    }

    private ArrayList<ParsedStructure> cleanseNode(ArrayList<ParsedStructure> passedNodes) {
        ArrayList<ParsedStructure> retunPassedNodes = new ArrayList<>(passedNodes);
        List<String> passwordNodes = new ArrayList<>();
        List<String> cardNodes = new ArrayList<>();
        for (ParsedStructure pn : passedNodes) {
            passwordNodes.add(pn.autofillhint);
            cardNodes.add(pn.autofillhint);
        }
        String formType = determineFormType(passedNodes);
        if (formType.equals(LOGIN_FORM)) {
            List<String> validLoginNodes = new ArrayList<>(Arrays.asList(GenericStringBase.login_form));
            validLoginNodes.removeAll(passwordNodes);
            for (String vln : validLoginNodes) {
                for (ParsedStructure rpn : retunPassedNodes) {
                    if (rpn.autofillhint.equals(UNKNOWN_HINT)) {
                        rpn.autofillhint = vln;
                        break;
                    }
                }
            }
        } else if (formType.equals(CARD_FORM)) {
            List<String> validCardNodes = new ArrayList<>(Arrays.asList(GenericStringBase.card_form));
            validCardNodes.removeAll(cardNodes);
            for (String vcn : validCardNodes) {
                for (ParsedStructure rpn : retunPassedNodes) {
                    if (rpn.autofillhint.equals(UNKNOWN_HINT)) {
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
        for (ParsedStructure pn : passedNodes) {
            passwordNodes.add(pn.autofillhint);
            cardNodes.add(pn.autofillhint);
        }
        passwordNodes.removeAll(Arrays.asList(GenericStringBase.login_form));
        cardNodes.removeAll(Arrays.asList(GenericStringBase.card_form));
        if (passwordNodes.size() < cardNodes.size()) {
            return LOGIN_FORM;
        } else if (cardNodes.size() < passwordNodes.size()) {
            return CARD_FORM;
        }
        return "";
    }

    public ArrayList<ParsedStructure> traverseNode(ViewNode viewNode) {
        ArrayList<ParsedStructure> passedNodes = new ArrayList<>();

        if (String.valueOf(viewNode.getClassName()).contains(EDIT_TEXT)
                || (viewNode.getHtmlInfo() != null && viewNode.getHtmlInfo().getTag().contains(INPUT))) {

            if (viewNode.getAutofillHints() != null && CompareStringBase(viewNode.getAutofillHints()[0],
                    GenericStringBase.autofillHints)) {
                passedNodes.add(new ParsedStructure(viewNode.getAutofillId(), viewNode.getAutofillHints()[0]));
            } else {
                String searchQuery = buildSearchQuery(viewNode);
                if (TextUtils.isEmpty(searchQuery)) {
                    passedNodes.add(new ParsedStructure(viewNode.getAutofillId(), UNKNOWN_HINT));
                } else if (CompareStringBase(searchQuery, GenericStringBase.restricted)) {
                    passedNodes.add(new ParsedStructure(viewNode.getAutofillId(), RESTRICTED));
                } else {
                    Boolean isUnknown = true;
                    for (stringBasePair sbp : stringBaseList) {
                        if (CompareStringBase(searchQuery, sbp.stringBase)) {
                            passedNodes.add(new ParsedStructure(viewNode.getAutofillId(), sbp.hints));
                            stringBaseList.remove(sbp);
                            isUnknown = false;
                            break;
                        }
                    }
                    if (isUnknown){
                        passedNodes.add(new ParsedStructure(viewNode.getAutofillId(), UNKNOWN_HINT));
                    }
                }
            }
        }

        for (int i = 0; i < viewNode.getChildCount(); i++) {
            ViewNode childNode = viewNode.getChildAt(i);
            passedNodes.addAll(traverseNode(childNode));
        }
        return passedNodes;
    }

    private String buildSearchQuery(ViewNode viewNode) {
        String searchQuery = viewNode.getIdEntry() + " " + viewNode.getHint() + " " +
                viewNode.getContentDescription();
        if (viewNode.getHtmlInfo() != null) {
            List<Pair<String, String>> HtmlValues = viewNode.getHtmlInfo().getAttributes();
            for (Pair<String, String> hv : HtmlValues) {
                if (hv.first.toLowerCase().trim().contains("label")) {
                    searchQuery = searchQuery + " " + hv.second;
                } else if (hv.first.toLowerCase().trim().contains("hints")) {
                    searchQuery = searchQuery + " " + hv.second;
                } else if (hv.first.toLowerCase().trim().contains("name")) {
                    searchQuery = searchQuery + " " + hv.second;
                } else if (hv.first.toLowerCase().trim().contains("type")) {
                    if (!CompareStringBase(hv.second, GenericStringBase.allowedHtmlInputTypes)) {
                        return INCOMPATIBLE_TYPE;
                    }
                }
            }
        }
        return searchQuery.replaceAll("null", "").trim();
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

    public ViewNode TraverseStructure(AssistStructure structure, AutofillId searchId) {
        int nodes = structure.getWindowNodeCount();

        for (int i = 0; i < nodes; i++) {
            AssistStructure.WindowNode windowNode = structure.getWindowNodeAt(i);
            ViewNode viewNode = traverseNode(windowNode.getRootViewNode(), searchId);
            if (viewNode != null) {
                return viewNode;
            }
        }
        return null;
    }

    private ViewNode traverseNode(ViewNode viewNode, AutofillId searchId) {
        if (viewNode.getAutofillId().equals(searchId)) {
            return viewNode;
        }
        for (int i = 0; i < viewNode.getChildCount(); i++) {
            ViewNode childNode = viewNode.getChildAt(i);
            ViewNode resultNode = traverseNode(childNode, searchId);
            if (resultNode != null) {
                return resultNode;
            }
        }
        return null;
    }

    class stringBasePair {
        String[] stringBase;
        String hints;

        public stringBasePair(String[] stringBase, String hints) {
            this.stringBase = stringBase;
            this.hints = hints;
        }
    }

}
