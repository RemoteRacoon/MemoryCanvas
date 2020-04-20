package com.example.memorycanvas;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    TilesView view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        view = findViewById(R.id.view);
    }

//    public void onNewGameClick(View v) {
//        view.newGame(); // запустить игру заново
//
//    }



}
