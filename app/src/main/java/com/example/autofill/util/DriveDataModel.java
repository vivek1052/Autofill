package com.example.autofill.util;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.example.autofill.MainActivity;
import com.example.autofill.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.LogRecord;


public class DriveDataModel {
    private Context context;
    private static final String APPLICATION_NAME = "Autofill";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final int versions = 5;

    public DriveDataModel(Context context) {
        this.context = context;
    }

    private Handler handler=new Handler();

    public void selectVersion() {
        new Thread(){
            @Override
            public void run(){
                if (GoogleSignIn.getLastSignedInAccount(context) != null) {
                    final Drive service = DriveServiceBuilder();
                    List<File> files;
                    try {
                        files = service.files().list()
                                .setQ("mimeType = 'application/x-sqlite3' and name = '" + Contract.DATABASE_NAME + "'")
                                .setSpaces("appDataFolder")
                                .setFields("files(createdTime,id,name)")
                                .execute().getFiles();

                        final CharSequence[] recovery = new CharSequence[versions];
                        for (int i = 0; i < files.size(); i++) {
                            recovery[i] = files.get(i).getCreatedTime().toString();
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
                                builder.setTitle(R.string.version);
                                builder.setItems(recovery, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        download(service,recovery[i]);
                                    }
                                });
                                builder.create().show();
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }.start();

    }

    private void download(final Drive service, final CharSequence charSequence) {
        new Thread(){
            @Override
           public void run(){
                List<File> files;
                try {
                    files = service.files().list()
                            .setQ("mimeType = 'application/x-sqlite3' and ( name = '" + Contract.DATABASE_NAME + "'"
                                    + " or name = '" + Contract.DATABASE_NAME_SHM + "'" + " or name = '" + Contract.DATABASE_NAME_WAL + "' ) " +
                                    "and createdTime = '" + charSequence + "'")
                            .setSpaces("appDataFolder")
                            .setFields("files(createdTime,id,name)")
                            .execute().getFiles();

                    for (File f:files){
                        service.files().get(f.getId())
                                .executeMediaAndDownloadTo(new FileOutputStream(context.getDatabasePath(f.getName())));
                    }
                    if (files.size()>0){
                        MainActivity mainActivity = (MainActivity)context;
                        mainActivity.dataModel.retrieveAllData();
                        mainActivity.dataModel.triggerPasswordDataUpdated();
                        mainActivity.dataModel.triggerCardDataUpdated();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    public Drive DriveServiceBuilder() {
        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        context, Collections.singleton(DriveScopes.DRIVE_APPDATA));
        credential.setSelectedAccount(GoogleSignIn.getLastSignedInAccount(context).getAccount());

        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME).build();
    }

    public void upload() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                if (GoogleSignIn.getLastSignedInAccount(context) != null) {
                    Drive service = DriveServiceBuilder();

                    List<File> files = new ArrayList<>();
                    try {
                        files = service.files().list()
                                .setQ("mimeType = 'application/x-sqlite3' and name = '" + Contract.DATABASE_NAME + "'")
                                .setSpaces("appDataFolder")
                                .setFields("files(createdTime,id,name)")
                                .execute().getFiles();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    DateTime dateTime = new DateTime(new Date());

                    File DBMetadata = new File();
                    DBMetadata.setName(Contract.DATABASE_NAME);
                    DBMetadata.setParents(Collections.singletonList("appDataFolder"));
                    DBMetadata.setCreatedTime(dateTime);

                    File SHMMetadata = new File();
                    SHMMetadata.setName(Contract.DATABASE_NAME_SHM);
                    SHMMetadata.setParents(Collections.singletonList("appDataFolder"));
                    SHMMetadata.setCreatedTime(dateTime);

                    File WALMetadata = new File();
                    WALMetadata.setName(Contract.DATABASE_NAME_WAL);
                    WALMetadata.setParents(Collections.singletonList("appDataFolder"));
                    WALMetadata.setCreatedTime(dateTime);

                    FileContent DBContent = new FileContent("application/x-sqlite3",
                            new java.io.File(String.valueOf(context.getDatabasePath(Contract.DATABASE_NAME))));

                    FileContent SHMContent = new FileContent("application/x-sqlite3",
                            new java.io.File(String.valueOf(context.getDatabasePath(Contract.DATABASE_NAME_SHM))));

                    FileContent WALContent = new FileContent("application/x-sqlite3",
                            new java.io.File(String.valueOf(context.getDatabasePath(Contract.DATABASE_NAME_WAL))));

                    if (files.size() < versions) {
                        try {
                            service.files().create(DBMetadata, DBContent).setFields("id")
                                    .execute();
                            service.files().create(SHMMetadata, SHMContent).setFields("id")
                                    .execute();
                            service.files().create(WALMetadata, WALContent).setFields("id")
                                    .execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        DateTime oldest = new DateTime(new Date());
                        for (int i = 0; i < files.size(); i++) {
                            if (files.get(i).getCreatedTime().getValue() < oldest.getValue()) {
                                oldest = files.get(i).getCreatedTime();
                            }
                        }

                        try {
                            files = service.files().list()
                                    .setQ("mimeType = 'application/x-sqlite3' and ( name = '" + Contract.DATABASE_NAME + "'"
                                            + " or name = '" + Contract.DATABASE_NAME_SHM + "'" + " or name = '" + Contract.DATABASE_NAME_WAL + "' ) " +
                                            "and createdTime = '" + oldest + "'")
                                    .setSpaces("appDataFolder")
                                    .setFields("files(createdTime,id,name)")
                                    .execute().getFiles();
                            for (File f : files) {
                                if (f.getName().equals(Contract.DATABASE_NAME)) {
                                    service.files().create(DBMetadata, DBContent).setFields("id")
                                            .execute();
                                    service.files().delete(f.getId()).execute();
                                } else if (f.getName().equals(Contract.DATABASE_NAME_SHM)) {
                                    service.files().create(SHMMetadata, SHMContent).setFields("id")
                                            .execute();
                                    service.files().delete(f.getId()).execute();
                                } else if (f.getName().equals(Contract.DATABASE_NAME_WAL)) {
                                    service.files().create(WALMetadata, WALContent).setFields("id")
                                            .execute();
                                    service.files().delete(f.getId()).execute();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        };
        thread.start();

    }
}
