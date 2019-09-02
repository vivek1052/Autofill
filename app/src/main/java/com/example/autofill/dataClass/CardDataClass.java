package com.example.autofill.dataClass;

import java.io.Serializable;

public class CardDataClass implements Serializable {
    public String bankName, cardType, name, cardNo1, cardNo2, cardNo3, cardNo4, month, year, cvv;
    public int id;
    public CardDataClass(int id, String bankName, String cardType, String name, String cardNo1, String cardNo2,
                         String cardNo3, String cardNo4, String month, String year, String cvv){
        this.id = id;
        this.bankName = bankName;
        this.cardType = cardType;
        this.name = name;
        this.cardNo1 = cardNo1;
        this.cardNo2 = cardNo2;
        this.cardNo3 = cardNo3;
        this.cardNo4 = cardNo4;
        this.month = month;
        this.year = year;
        this.cvv = cvv;
    }

    public CardDataClass(CardDataClass data){
        this.id = data.id;
        this.bankName = data.bankName;
        this.cardType = data.cardType;
        this.name = data.name;
        this.cardNo1 = data.cardNo1;
        this.cardNo2 = data.cardNo2;
        this.cardNo3 = data.cardNo3;
        this.cardNo4 = data.cardNo4;
        this.month = data.month;
        this.year = data.year;
        this.cvv = data.cvv;
    }
}
