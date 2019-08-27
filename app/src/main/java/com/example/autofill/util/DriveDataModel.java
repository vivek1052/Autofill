package com.example.autofill.util;


import android.content.Context;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.material.snackbar.Snackbar;
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
                    Drive service = DriveServiceBuilder();
                    List<File> files;
                    try {
                        FileList result = service.files().list()
                               .setQ("mimeType = 'application/x-sqlite3' and ( name = '"+Contract.DATABASE_NAME+"'"
                               + " or name = '"+Contract.DATABASE_NAME_SHM+"'"+" or name = '"+Contract.DATABASE_NAME_WAL+"' )")
                               .setSpaces("appDataFolder")
                               .execute();
                        files = result.getFiles();
                        for (File f:files){
                            service.files().get(f.getId())
                                    .executeMediaAndDownloadTo(new FileOutputStream(context.getDatabasePath(f.getName())));
                        }
                        if(files.size()>0){
                            Snackbar.make(new CoordinatorLayout(context),"Files Downloaded",Snackbar.LENGTH_LONG).show();
                        }
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
                    File DBMetadata = new File();
                    DBMetadata.setName(Contract.DATABASE_NAME);
                    DBMetadata.setParents(Collections.singletonList("appDataFolder"));

                    File SHMMetadata = new File();
                    SHMMetadata.setName(Contract.DATABASE_NAME_SHM);
                    SHMMetadata.setParents(Collections.singletonList("appDataFolder"));

                    File WALMetadata = new File();
                    WALMetadata.setName(Contract.DATABASE_NAME_WAL);
                    WALMetadata.setParents(Collections.singletonList("appDataFolder"));

                    FileContent DBContent = new FileContent("application/x-sqlite3",
                            new java.io.File(String.valueOf(context.getDatabasePath(Contract.DATABASE_NAME))));

                    FileContent SHMContent = new FileContent("application/x-sqlite3",
                            new java.io.File(String.valueOf(context.getDatabasePath(Contract.DATABASE_NAME_SHM))));

                    FileContent WALContent = new FileContent("application/x-sqlite3",
                            new java.io.File(String.valueOf(context.getDatabasePath(Contract.DATABASE_NAME_WAL))));
                    try {
                        service.files().create(DBMetadata,DBContent).setFields("id")
                                .execute();
                        service.files().create(SHMMetadata,SHMContent).setFields("id")
                                .execute();
                        service.files().create(WALMetadata,WALContent).setFields("id")
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
