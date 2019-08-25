package com.example.autofill.dataClass;

import java.io.Serializable;

public class MasterPasswordEncrypted implements Serializable {
    public byte[] encryptedText;
    public byte[] iv;
    public MasterPasswordEncrypted(byte[] encryptedText, byte[] iv){
        this.encryptedText = encryptedText;
        this.iv = iv;
    }
}
