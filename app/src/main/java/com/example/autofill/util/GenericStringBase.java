package com.example.autofill.util;

import android.view.View;

public class GenericStringBase {
    private static final String HINT_CITY = "CITY";
    private static final String HINT_STATE = "STATE";
    private static final String HINT_LOCALITY ="LOCALITY" ;
    private static final String HINT_FLAT_NO = "FLAT_NO" ;
    private static final String HINT_BUILDING_NAME = "BUILDING_NAME";
    private static final String HINT_STREET_NO = "STREET_NO";
    private static final String HINT_STREET_NAME = "STREET_NAME";
    private static final String HINT_COUNTRY = "COUNTRY" ;
    public static String[] username = {"userid", "loginid", "user", "login"};
    public static String[] password = {"password", "pwd"};
    public static String[] phone = {"phone","mobile"};
    public static String[] email = {"email"};
    public static String[] cardNo = {"cardno","card number","card no","cardnum"};
    public static String[] holderName = {"name", "holder"};
    public static String[] expiryMonth = {"month","mm"};
    public static String[] expiryYear = {"year","yy"};
    public static String[] cvv = {"cvv"};
    public static String[] postal = {"pincode","postal code","postalcode","pin code"};
    public static String[] flatno = {"flat no","house no","h no","flat","house"};
    public static String[] buildingName = {"building name","building"};
    public static String[] streetNo = {"street number","streetno","street no","streetnum"};
    public static String[] streetName = {"street name","streetname","road name","road","street"};
    public static String[] locality = {"locality"};
    public static String[] city={"city"};
    public static String[] state={"state"};
    public static String[] country = {"country"};
    public static String[] restricted = {"otp","search","INCOMPATIBLE_TYPE","reply","comment","dummy",
    "amount"};
    public static String[] login_form = {View.AUTOFILL_HINT_USERNAME,View.AUTOFILL_HINT_PASSWORD,
            View.AUTOFILL_HINT_EMAIL_ADDRESS,View.AUTOFILL_HINT_PHONE};
    public static String[] card_form = {View.AUTOFILL_HINT_CREDIT_CARD_NUMBER,View.AUTOFILL_HINT_NAME,
    View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH,View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR,
    View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE};
    public static String[] addressForm = {View.AUTOFILL_HINT_POSTAL_CODE, View.AUTOFILL_HINT_NAME,
    HINT_BUILDING_NAME,HINT_CITY,HINT_COUNTRY,HINT_FLAT_NO,HINT_LOCALITY,HINT_STATE,HINT_STREET_NAME,
    HINT_STREET_NO};
    public static String[] autofillHints = {View.AUTOFILL_HINT_NAME,View.AUTOFILL_HINT_CREDIT_CARD_NUMBER,
    View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DATE,View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DAY,
    View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH,View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR,
    View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE,View.AUTOFILL_HINT_EMAIL_ADDRESS,View.AUTOFILL_HINT_PASSWORD,
    View.AUTOFILL_HINT_PHONE,View.AUTOFILL_HINT_POSTAL_ADDRESS,View.AUTOFILL_HINT_POSTAL_CODE,
    View.AUTOFILL_HINT_USERNAME};
    public static String[] allowedHtmlInputTypes = {"text","password","email","month","number","password", "tel","select"};
    static String[] restrictedPackages = {"com.example.autofill"};
    public static String[] browser = {"org.mozilla.firefox", "org.mozilla.firefox_beta", "com.microsoft.emmx",
            "com.android.chrome", "com.chrome.beta", "com.android.browser", "com.brave.browser",
            "com.opera.browser", "com.opera.browser.beta", "com.opera.mini.native", "com.chrome.dev",
            "com.chrome.canary", "com.google.android.apps.chrome", "com.google.android.apps.chrome_dev",
            "com.yandex.browser", "com.sec.android.app.sbrowser", "com.sec.android.app.sbrowser.beta",
            "org.codeaurora.swe.browser", "com.amazon.cloud9", "mark.via.gp", "org.bromite.bromite",
            "org.chromium.chrome", "com.kiwibrowser.browser", "com.ecosia.android", "com.opera.mini.native.beta",
            "org.mozilla.fennec_aurora", "org.mozilla.fennec_fdroid", "com.qwant.liberty", "com.opera.touch",
            "org.mozilla.fenix", "org.mozilla.fenix.nightly", "org.mozilla.reference.browser", "org.mozilla.rocket"};

}
