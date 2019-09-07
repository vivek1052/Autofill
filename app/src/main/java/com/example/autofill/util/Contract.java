package com.example.autofill.util;

import android.provider.BaseColumns;

public final class Contract {
    static public final String DATABASE_NAME = "AUTOFILL.db";
    static public final String DATABASE_NAME_SHM = "AUTOFILL.db-shm";
    static public final String DATABASE_NAME_WAL = "AUTOFILL.db-wal";

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private Contract() {}

    /* Inner class that defines the table contents */
    public static class Password implements BaseColumns {
        public static final String TABLE_NAME = "passwordDB";
        public static final String ID = "id";
        public static final String SERVICE = "service";
        public static final String SUBTEXT = "subtext";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
    }

    public static class Card implements BaseColumns{
        public static final String TABLE_NAME = "cardDB";
        public static final String ID = "id";
        public static final String BANK_NAME = "bankname";
        public static final String CARD_TYPE = "cardtype";
        public static final String NAME = "name";
        public static final String CARDNO_1 = "cardno1";
        public static final String CARDNO_2 = "cardno2";
        public static final String CARDNO_3 = "cardno3";
        public static final String CARDNO_4 = "cardno4";
        public static final String MONTH = "month";
        public static final String YEAR = "year";
        public static final String CVV = "cvv";

    }

    public static class Address implements BaseColumns{
        public static final String TABLE_NAME = "addressDB";
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String FLAT_NO = "flatno";
        public static final String BUILDING_NAME = "buildingname";
        public static final String STREET_NO = "streetno";
        public static final String STREET_NAME = "streetname";
        public static final String LOCALITY = "localilty";
        public static final String CITY = "city";
        public static final String STATE = "state";
        public static final String POSTAL = "postalcode";
        public static final String COUNTRY = "country";
        public static final String PHONE = "phoneno";
    }
}
