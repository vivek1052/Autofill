package com.example.autofill.dataClass;

import java.io.Serializable;

public class IdentityDataClass implements Serializable {
    public String identityType, identityNumber;
    public int id;

    public IdentityDataClass(int id, String identityType, String identityNumber) {
        this.id = id;
        this.identityType = identityType;
        this.identityNumber = identityNumber;
    }
    public IdentityDataClass(IdentityDataClass data){
        this.id = data.id;
        this.identityType = data.identityType;
        this.identityNumber = data.identityNumber;
    }
}
