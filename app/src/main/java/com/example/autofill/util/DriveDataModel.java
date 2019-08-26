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
import com.google.api.services.drive.model.FileList;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;


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
                if (GoogleSignIn.getLastSignedInAccount(context) != null){
                    OutputStream driveOutputStream = new ByteArrayOutputStream();
                    Drive service = DriveServiceBuilder();
                    List<File> files;
                    try {
                        FileList result = service.files().list()
                               .setQ("mimeType = 'application/x-sqlite3' and name = '"+Contract.DATABASE_NAME+"'")
                               .setSpaces("appDataFolder")
                               .execute();
                        files = result.getFiles();
                        String id = files.get(0).getId();
                        service.files().get(id)
                                .executeMediaAndDownloadTo(driveOutputStream);
                        OutputStream outStream = new FileOutputStream(context.getDatabasePath(Contract.DATABASE_NAME));
                        ((ByteArrayOutputStream) driveOutputStream).writeTo(outStream);
                        outStream.flush();
                        outStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
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
                    fileMetadata.setName(Contract.DATABASE_NAME);
                    fileMetadata.setParents(Collections.singletonList("appDataFolder"));
                    java.io.File path = new java.io.File(String.valueOf(context.getDatabasePath(Contract.DATABASE_NAME)));
                    FileContent mediaContent = new FileContent("application/x-sqlite3", path);
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
