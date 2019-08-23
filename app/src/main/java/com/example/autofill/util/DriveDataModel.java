package com.example.autofill.util;


import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.IOException;

import java.util.Collections;



public class DriveDataModel {
    private Context context;
    private static final String APPLICATION_NAME = "Autofill";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public DriveDataModel(Context context){
        this.context = context;
    }

    public void download(final String filePath) {
        Thread thread = new Thread(){
            @Override
            public void run() {
//                if (GoogleSignIn.getLastSignedInAccount(context) != null){
//                    Drive service = DriveServiceBuilder();
//                    try {
//                       FileList list = service.files().list().setQ("name="+filePath)
//                               .execute();
//                       List<File> files = list.getFiles();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                }
            }
        };
        thread.start();
    }
    public Drive DriveServiceBuilder(){
        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        context, Collections.singleton(DriveScopes.DRIVE_APPDATA));
        credential.setSelectedAccount(GoogleSignIn.getLastSignedInAccount(context).getAccount());

        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME).build();
    }

    public void upload(final String filePath) {
        Thread thread = new Thread(){
            @Override
            public void run() {
                if (GoogleSignIn.getLastSignedInAccount(context) != null){
                    Drive service = DriveServiceBuilder();
                    File fileMetadata = new File();
                    fileMetadata.setName(filePath);
                    fileMetadata.setParents(Collections.singletonList("appDataFolder"));
                    java.io.File path = new java.io.File(context.getFilesDir()+"/"+filePath);
                    FileContent mediaContent = new FileContent("", path);
                    try {
                        service.files().create(fileMetadata,mediaContent).setFields("id")
                                .execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        thread.start();

    }
}
