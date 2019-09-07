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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NodeParser {

    private static final String INCOMPATIBLE_TYPE = "INCOMPATIBLE_TYPE";
    private static final CharSequence EDIT_TEXT = "EditText";
    private static final CharSequence INPUT = "input";
    private static final String UNKNOWN_HINT = "UNKNOWN";
    private static final String LOGIN_FORM = "LOGIN_FORM";
    private static final String CARD_FORM = "CARD_FORM";
    private static final String ADDRESS_FORM = "ADDRESS_FORM";
    private static final String RESTRICTED = "RESTRICTED";
    private static final String HINT_CITY = "CITY";
    private static final String HINT_STATE = "STATE";
    private static final String HINT_LOCALITY ="LOCALITY" ;
    private static final String HINT_FLAT_NO = "FLAT_NO" ;
    private static final String HINT_BUILDING_NAME = "BUILDING_NAME";
    private static final String HINT_STREET_NO = "STREET_NO";
    private static final String HINT_STREET_NAME = "STREET_NAME";
    private static final String HINT_COUNTRY = "COUNTRY" ;
    private List<String> addressFormComp = Arrays.asList(GenericStringBase.addressForm);

    private List<stringBasePair> stringBaseList = new ArrayList<>();

    public ArrayList<ParsedStructure> TraverseStructure(AssistStructure structure) {

        stringBaseList.add(new stringBasePair(GenericStringBase.password, View.AUTOFILL_HINT_PASSWORD));
        stringBaseList.add(new stringBasePair(GenericStringBase.username, View.AUTOFILL_HINT_USERNAME));
        stringBaseList.add(new stringBasePair(GenericStringBase.email, View.AUTOFILL_HINT_EMAIL_ADDRESS));
        stringBaseList.add(new stringBasePair(GenericStringBase.phone, View.AUTOFILL_HINT_PHONE));
        stringBaseList.add(new stringBasePair(GenericStringBase.expiryMonth, View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH));
        stringBaseList.add(new stringBasePair(GenericStringBase.expiryYear, View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR));
        stringBaseList.add(new stringBasePair(GenericStringBase.cvv, View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE));
        stringBaseList.add(new stringBasePair(GenericStringBase.cardNo, View.AUTOFILL_HINT_CREDIT_CARD_NUMBER));
        stringBaseList.add(new stringBasePair(GenericStringBase.postal, View.AUTOFILL_HINT_POSTAL_CODE));
        stringBaseList.add(new stringBasePair(GenericStringBase.city, HINT_CITY));
        stringBaseList.add(new stringBasePair(GenericStringBase.state, HINT_STATE));
        stringBaseList.add(new stringBasePair(GenericStringBase.locality, HINT_LOCALITY));
        stringBaseList.add(new stringBasePair(GenericStringBase.flatno, HINT_FLAT_NO));
        stringBaseList.add(new stringBasePair(GenericStringBase.buildingName, HINT_BUILDING_NAME));
        stringBaseList.add(new stringBasePair(GenericStringBase.streetNo, HINT_STREET_NO));
        stringBaseList.add(new stringBasePair(GenericStringBase.streetName, HINT_STREET_NAME));
        stringBaseList.add(new stringBasePair(GenericStringBase.country, HINT_COUNTRY));
        stringBaseList.add(new stringBasePair(GenericStringBase.holderName, View.AUTOFILL_HINT_NAME));
        ArrayList<ParsedStructure> passedNodes = new ArrayList<>();
        int nodes = structure.getWindowNodeCount();

        for (int i = 0; i < nodes; i++) {
            AssistStructure.WindowNode windowNode = structure.getWindowNodeAt(i);
            ViewNode viewNode = windowNode.getRootViewNode();
            passedNodes.addAll(traverseNode(viewNode));
        }
        passedNodes = cleanseNode(passedNodes);
        List<ParsedStructure> unknownNodes = new ArrayList<>();
        for (ParsedStructure pn : passedNodes) {
            if (pn.autofillhint.contains(UNKNOWN_HINT)) {
                unknownNodes.add(pn);
            }
        }
        passedNodes.removeAll(unknownNodes);
        return passedNodes;
    }

    private ArrayList<ParsedStructure> cleanseNode(ArrayList<ParsedStructure> passedNodes) {
        ArrayList<ParsedStructure> returnPassedNodes = new ArrayList<>(passedNodes);
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
                for (ParsedStructure rpn : returnPassedNodes) {
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
                for (ParsedStructure rpn : returnPassedNodes) {
                    if (rpn.autofillhint.equals(UNKNOWN_HINT)) {
                        rpn.autofillhint = vcn;
                        break;
                    }
                }
            }
        }
        return returnPassedNodes;
    }

    public String determineFormType(ArrayList<ParsedStructure> passedNodes) {
        HashSet<String> passwordNodes = new HashSet<>();
        HashSet<String> cardNodes = new HashSet<>();
        HashSet<String> addressNodes = new HashSet<>();
        for (ParsedStructure pn : passedNodes) {
            passwordNodes.add(pn.autofillhint);
            cardNodes.add(pn.autofillhint);
            addressNodes.add(pn.autofillhint);
        }
        passwordNodes.removeAll(Arrays.asList(GenericStringBase.login_form));
        cardNodes.removeAll(Arrays.asList(GenericStringBase.card_form));
        addressNodes.removeAll(Arrays.asList(GenericStringBase.addressForm));

        if (passwordNodes.size() < cardNodes.size() && passwordNodes.size() < addressNodes.size()) {
            return LOGIN_FORM;
        } else if (cardNodes.size() < passwordNodes.size() && cardNodes.size() < addressNodes.size()) {
            return CARD_FORM;
        } else if (addressNodes.size() < passwordNodes.size() && addressNodes.size() < cardNodes.size()){
            return ADDRESS_FORM;
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
                    List<stringBasePair> assignedPairs = new ArrayList<>();
                    for (stringBasePair sbp : stringBaseList) {
                        if (CompareStringBase(searchQuery, sbp.stringBase)) {
                            passedNodes.add(new ParsedStructure(viewNode.getAutofillId(), sbp.hints));
                            assignedPairs.add(sbp);
                            isUnknown = false;
                            if (!addressFormComp.contains(sbp.hints)){
                                break;
                            }
                        }
                    }
                    stringBaseList.removeAll(assignedPairs);
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
        String searchQuery = viewNode.getIdEntry()+" "+viewNode.getHint()+" "+viewNode.getContentDescription();
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
        searchQuery = searchQuery.toLowerCase().trim();
        searchQuery = searchQuery.replaceAll("null", "").trim();
        String specialChar = "[^a-zA-Z0-9]";
        Pattern p = Pattern.compile(specialChar);
        Matcher m = p.matcher(searchQuery);
        while(m.find())
        {
            String s= m.group();
            searchQuery = searchQuery.replaceAll("\\"+s, " ");
        }

        String duplicatePattern = "\\b([\\w\\s']+) \\1\\b";
        Pattern dupPatter = Pattern.compile(duplicatePattern);
        Matcher dupMatcher = dupPatter.matcher(searchQuery);
        if (dupMatcher.matches()) {
            return dupMatcher.group(1);
        } else {
            return searchQuery;
        }
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
