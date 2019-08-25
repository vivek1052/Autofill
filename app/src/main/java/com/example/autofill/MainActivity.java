package com.example.autofill;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.autofill.AutofillManager;

import com.example.autofill.util.CipherClass;
import com.example.autofill.util.DataModel;
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public Toolbar toolbar;
    public DrawerLayout drawer;
    public NavigationView navigationView;
    public NavController navController;
    public DataModel dataModel;
    public CipherClass cipherClass;

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
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController,appBarConfiguration);
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
}
