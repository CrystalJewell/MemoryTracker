package com.crystaljewell.memorytracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.crystaljewell.memorytracker.R;
import com.crystaljewell.memorytracker.ui.MapsActivity;
import com.crystaljewell.memorytracker.ui.MemoryActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

    }

    @OnClick(R.id.view_memories_card)
    protected void goToMemories() {
        Intent memoriesIntent = new Intent(this, MemoryActivity.class);
        startActivity(memoriesIntent);
    }

    @OnClick(R.id.view_map_card)
    protected void goToMap() {
       Intent mapIntent = new Intent(this, MapsActivity.class);
       startActivity(mapIntent);
    }
}
