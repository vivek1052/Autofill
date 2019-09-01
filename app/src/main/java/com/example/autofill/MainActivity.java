package com.example.autofill;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.autofill.AutofillManager;

import com.example.autofill.util.CipherClass;
import com.example.autofill.util.DataModel;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.client.util.DateTime;

import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "AUTOFILL_CHANNEL_ID";
    public Toolbar toolbar;
    public DrawerLayout drawer;
    public NavigationView navigationView;
    public NavController navController;
    public DataModel dataModel;
    public CipherClass cipherClass;
    private onCallbacks callbackListener;
    private static final int RC_SIGN_IN = 1;

    private AppBarConfiguration appBarConfiguration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataModel = new DataModel(this);
        dataModel.retrieveAllData();
        cipherClass = new CipherClass();

        toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navController = Navigation.findNavController(this,R.id.nav_host_fragment);
        setSupportActionBar(toolbar);

        Set<Integer> topLevelDestinations = new HashSet<>();
        topLevelDestinations.add(R.id.home_menu);
        topLevelDestinations.add(R.id.password_menu);
        topLevelDestinations.add(R.id.card_menu);
        topLevelDestinations.add(R.id.address_menu);

        appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations)
                                    .setDrawerLayout(drawer).build();

        NavigationUI.setupActionBarWithNavController(this,navController,appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView,navController);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController,appBarConfiguration);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==RC_SIGN_IN){
            callbackListener.onGoogleSignIn(resultCode,data);
        }
    }

    public void setCallbackListener(onCallbacks listener){
        this.callbackListener = listener;
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }

    public interface onCallbacks{
        void onGoogleSignIn(int resultCode, Intent data);
    }
}
