package com.example.autofill.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.autofill.dataClass.CardDataClass;
import com.example.autofill.dataClass.PasswordDataClass;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private Context context;

    public DBHelper(Context context){
        super(context, Contract.DATABASE_NAME , null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "create table "+Contract.Password.TABLE_NAME+" ("+Contract.Password.ID+
                " integer primary key, "+Contract.Password.SERVICE+" text, "+Contract.Password.SUBTEXT+
                " text, "+Contract.Password.USERNAME+" text, "+Contract.Password.PASSWORD+" text)";
        sqLiteDatabase.execSQL(query);
        query = "create table "+Contract.Card.TABLE_NAME+" ("+Contract.Card.ID+" integer primary key, "+
                Contract.Card.BANK_NAME+" text, "+Contract.Card.CARD_TYPE+" text, "+Contract.Card.NAME
                +" text, "+Contract.Card.CARDNO_1+" text, "+Contract.Card.CARDNO_2+" text, "+
                Contract.Card.CARDNO_3+" text, "+Contract.Card.CARDNO_4+" text, "+Contract.Card.MONTH
                +" text, "+ Contract.Card.YEAR+" text, "+Contract.Card.CVV+" text)";
        sqLiteDatabase.execSQL(query);
        query = "create table "+Contract.Address.TABLE_NAME+" ("+Contract.Address.ID+" integer primary key, "+
                Contract.Address.NAME+" text, "+Contract.Address.FLAT_NO+" text, "+Contract.Address.BUILDING_NAME
                +" text, "+Contract.Address.STREET_NO+" text, "+Contract.Address.STREET_NAME+" text, "+
                Contract.Address.LOCALITY+" text, "+Contract.Address.CITY+" text, "+Contract.Address.STATE
                +" text, "+Contract.Address.COUNTRY+" text, "+Contract.Address.POSTAL+" text)";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+Contract.Password.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+Contract.Card.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void insertPassword(String service,String subtext, String username, String password){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Password.SERVICE,service);
        contentValues.put(Contract.Password.SUBTEXT,subtext);
        contentValues.put(Contract.Password.USERNAME,username);
        contentValues.put(Contract.Password.PASSWORD,password);
        sqLiteDatabase.insert(Contract.Password.TABLE_NAME,null, contentValues);
    }
    public void updatePassword(int id,String service,String subtext, String username, String password){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Password.SERVICE,service);
        contentValues.put(Contract.Password.SUBTEXT,subtext);
        contentValues.put(Contract.Password.USERNAME,username);
        contentValues.put(Contract.Password.PASSWORD,password);
        sqLiteDatabase.update(Contract.Password.TABLE_NAME,contentValues,
                Contract.Password.ID+" = ?", new String[]{String.valueOf(id)});
    }
    public List<PasswordDataClass> getPassword(String packageName){
        List<PasswordDataClass> passwordData = new ArrayList<PasswordDataClass>();
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor res = sqLiteDatabase.rawQuery("select * from "+Contract.Password.TABLE_NAME+
                " WHERE "+Contract.Password.SUBTEXT+" = ?",new String[]{packageName});
        res.moveToFirst();
        while (res.isAfterLast() == false){
            passwordData.add(new PasswordDataClass(res.getInt(res.getColumnIndex(Contract.Password.ID)),
                    res.getString(res.getColumnIndex(Contract.Password.SERVICE)),
                    res.getString(res.getColumnIndex(Contract.Password.SUBTEXT)),
                    res.getString(res.getColumnIndex(Contract.Password.USERNAME)),
                    res.getString(res.getColumnIndex(Contract.Password.PASSWORD))));
            res.moveToNext();
        }
        return passwordData;
    }

    public List<PasswordDataClass> getAllPassword(){
        List<PasswordDataClass> passwordData = new ArrayList<PasswordDataClass>();
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor res = sqLiteDatabase.rawQuery("select * from "+Contract.Password.TABLE_NAME,null);
        res.moveToFirst();
        while (res.isAfterLast() == false){
            passwordData.add(new PasswordDataClass(res.getInt(res.getColumnIndex(Contract.Password.ID)),
                    res.getString(res.getColumnIndex(Contract.Password.SERVICE)),
                    res.getString(res.getColumnIndex(Contract.Password.SUBTEXT)),
                    res.getString(res.getColumnIndex(Contract.Password.USERNAME)),
                    res.getString(res.getColumnIndex(Contract.Password.PASSWORD))));
            res.moveToNext();
        }
        return passwordData;
    }

    public int deletePassword(int id){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        return sqLiteDatabase.delete(Contract.Password.TABLE_NAME,"id = ?", new String[]{Integer.toString(id)});
    }

    public void insertCard(CardDataClass cardData){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Card.BANK_NAME,cardData.bankName);
        contentValues.put(Contract.Card.CARD_TYPE,cardData.cardType);
        contentValues.put(Contract.Card.NAME,cardData.name);
        contentValues.put(Contract.Card.CARDNO_1,cardData.cardNo1);
        contentValues.put(Contract.Card.CARDNO_2,cardData.cardNo2);
        contentValues.put(Contract.Card.CARDNO_3,cardData.cardNo3);
        contentValues.put(Contract.Card.CARDNO_4,cardData.cardNo4);
        contentValues.put(Contract.Card.MONTH,cardData.month);
        contentValues.put(Contract.Card.YEAR,cardData.year);
        contentValues.put(Contract.Card.CVV,cardData.cvv);
        sqLiteDatabase.insert(Contract.Card.TABLE_NAME,null, contentValues);
    }

    public List<CardDataClass> getAllCards(){
        List<CardDataClass> cardData = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor res = sqLiteDatabase.rawQuery("select * from "+Contract.Card.TABLE_NAME,null);
        res.moveToFirst();
        while (res.isAfterLast() == false){
            cardData.add(new CardDataClass(res.getInt(res.getColumnIndex(Contract.Card.ID)),
                    res.getString(res.getColumnIndex(Contract.Card.BANK_NAME)),
                    res.getString(res.getColumnIndex(Contract.Card.CARD_TYPE)),
                    res.getString(res.getColumnIndex(Contract.Card.NAME)),
                    res.getString(res.getColumnIndex(Contract.Card.CARDNO_1)),
                    res.getString(res.getColumnIndex(Contract.Card.CARDNO_2)),
                    res.getString(res.getColumnIndex(Contract.Card.CARDNO_3)),
                    res.getString(res.getColumnIndex(Contract.Card.CARDNO_4)),
                    res.getString(res.getColumnIndex(Contract.Card.MONTH)),
                    res.getString(res.getColumnIndex(Contract.Card.YEAR)),
                    res.getString(res.getColumnIndex(Contract.Card.CVV))));
            res.moveToNext();
        }
        return cardData;
    }
    public int deleteCard(int id){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        return sqLiteDatabase.delete(Contract.Card.TABLE_NAME,"id = ?", new String[]{Integer.toString(id)});
    }
}
