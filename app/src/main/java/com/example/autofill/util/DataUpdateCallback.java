package com.example.autofill.util;

import com.example.autofill.dataClass.AddressDataClass;
import com.example.autofill.dataClass.CardDataClass;
import com.example.autofill.dataClass.IdentityDataClass;
import com.example.autofill.dataClass.PasswordDataClass;

import java.util.List;

public interface DataUpdateCallback {
    void passwordDataUpdated(List<PasswordDataClass> updatedData);
    void cardDetailUpdated(List<CardDataClass> updatedData);
    void AddressDataUpdated(List<AddressDataClass> updatedData);
    void IdentityDataUpdated(List<IdentityDataClass> updatedData);
}
