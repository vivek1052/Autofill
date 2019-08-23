package com.example.autofill.dataClass;

import java.io.Serializable;

public class PasswordDataClass implements Serializable{
    public String serviceName, subText,username,password;
    public int id;

    public PasswordDataClass(int id,String serviceName, String subText, String  username, String password){
        this.id = id;
        this.serviceName = serviceName;
        this.subText = subText;
        this.username= username;
        this.password = password;
    }
    public PasswordDataClass(PasswordDataClass data){
        this.id = data.id;
        this.serviceName = data.serviceName;
        this.subText = data.subText;
        this.username = data.username;
        this.password = data.password;
    }
}
