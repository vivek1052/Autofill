package com.example.autofill.dataClass;

import java.io.Serializable;

public class AddressDataClass implements Serializable {
    int id;
    public String name,flatNo, buildingName,streetNo, streetName,locality, city, state, postalCode, country, phoneNo;

    public AddressDataClass(int id, String name, String flatNo, String buildingName, String streetNo,
                            String streetName, String locality, String city, String state, String postalCode,
                            String country, String phoneNo) {
        this.id = id;
        this.name = name;
        this.flatNo = flatNo;
        this.buildingName = buildingName;
        this.streetNo = streetNo;
        this.streetName = streetName;
        this.locality = locality;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        this.phoneNo = phoneNo;
    }
}
