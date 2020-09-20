package com.helloworld.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class Driver_list extends AppCompatActivity implements rvAdapterForDriverList.ToInteractWithDriverList {

    ArrayList<UserProfile> drivers = new ArrayList<>();
    RecyclerView rv;
    RecyclerView.Adapter rvAdapter;
    RecyclerView.LayoutManager rvLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_list);

        Toolbar t = findViewById(R.id.toolbar_for_sidebar);
        t.setTitleTextColor(Color.WHITE);
        setSupportActionBar(t);
        setTitle("List of Drivers");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drivers = (ArrayList<UserProfile>) getIntent().getSerializableExtra("drivers");

        rv = findViewById(R.id.rv_in_Driver_list);
        rv.setHasFixedSize(true);
        rvLayoutManager =  new LinearLayoutManager(this);
        rv.setLayoutManager(rvLayoutManager);
        rvAdapter =  new rvAdapterForDriverList(this,drivers);
        rv.setAdapter(rvAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void DriverSelect(UserProfile u) {
        Toast.makeText(this, "driver "+u.firstName+" selected", Toast.LENGTH_SHORT).show();
        Intent data = new Intent();
        data.putExtra("driverProfile", u);
        setResult(250, data);
        finish();
    }
}